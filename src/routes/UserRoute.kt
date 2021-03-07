package com.example.routes

import com.example.Constants
import com.example.Constants.DEFAULT_PROFILE_IMG_URL
import com.example.data.*
import com.example.data.requests.ToggleFollowRequest
import com.example.data.requests.UpdateUserRequest
import com.example.data.responses.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Route.userRoute() {

    route("getUser/{id}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {
                    val id = call.parameters["id"]!!

                    val user = getUserById(id)

                    user?.let {
                        call.respond(OK, it)
                    }

                }
            }
        }
    }

    route("/searchUsers/{query}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {

                    val query = call.parameters["query"]!!

                    val uid = call.principal<UserIdPrincipal>()!!.name

                    val users = searchUsers(query, uid)

                    call.respond(OK, users)

                }
            }
        }
    }

    route("/updateUser") {
        authenticate {
            post {
                withContext(Dispatchers.IO) {

                    val request = try {
                        call.receive<UpdateUserRequest>()
                    }
                    catch (e: ContentTransformationException) {
                        call.respond(BadRequest)
                        return@withContext
                    }

                    val email = call.principal<UserIdPrincipal>()!!.name

                    val uid = getUidByEmail(email)

                    uid?.let {
                        if (updateProfile(it, request.profileImgUrl, request.username, request.bio)) {
                            call.respond(OK, SimpleResponse(true, "Profile updated"))
                        } else {
                            call.respond(Conflict)
                        }
                    }

                }
            }
        }
    }

    route("/getFollowing/{uid}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {

                    val uid = call.parameters["uid"]!!

                    val list = getAllFollowing(uid)

                    call.respond(OK, list)

                }
            }
        }
    }

    route("/getFollowers/{uid}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {

                    val uid = call.parameters["uid"]!!

                    val list = getAllFollowers(uid)

                    call.respond(OK, list)

                }
            }
        }
    }

    route("/getUserByEmail/{email}") {
        get {
            withContext(Dispatchers.IO) {
                val email = call.parameters["email"]!!

                val user = getUserByEmail(email)

                user?.let {
                    call.respond(OK, it)
                }

            }
        }
    }

    route("/toggleFollow") {
        authenticate {
            post {
                withContext(Dispatchers.IO) {

                    val request = try {
                        call.receive<ToggleFollowRequest>()
                    }
                    catch (e: ContentTransformationException) {
                        call.respond(BadRequest)
                        return@withContext
                    }

                    val currentEmail = call.principal<UserIdPrincipal>()!!.name

                    if(toggleFollow(request.uid, currentEmail)) {
                        call.respond(OK)
                    }
                    else {
                        call.respond(Conflict)
                    }


                }
            }
        }
    }

}