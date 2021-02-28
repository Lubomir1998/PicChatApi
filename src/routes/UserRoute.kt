package com.example.routes

import com.example.Constants
import com.example.Constants.DEFAULT_PROFILE_IMG_URL
import com.example.data.getUidByEmail
import com.example.data.getUserById
import com.example.data.requests.UpdateUserRequest
import com.example.data.responses.SimpleResponse
import com.example.data.searchUsers
import com.example.data.updateProfile
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
                        if (updateProfile(it, request.profileImgUrl
                                        ?: DEFAULT_PROFILE_IMG_URL, request.username, request.bio)) {
                            call.respond(OK, SimpleResponse(true, "Profile updated"))
                        } else {
                            call.respond(Conflict)
                        }
                    }

                }
            }
        }
    }

}