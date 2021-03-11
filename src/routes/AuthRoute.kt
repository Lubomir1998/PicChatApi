package com.example.routes

import com.example.data.*
import com.example.data.collections.Auth
import com.example.data.collections.User
import com.example.data.requests.AccountRequest
import com.example.data.requests.RemoveTokenRequest
import com.example.data.responses.SimpleResponse
import com.example.security.getHashWithSalt
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Route.authRoute() {

    route("/register/{username}") {
        post {
            withContext(Dispatchers.IO) {
                val auth = try {
                    call.receive<Auth>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@withContext
                }

                val userExists = checkIfUserExists(auth.email)
                val username = call.parameters["username"]!!

                if(!userExists) {
                    if(registerUser(Auth(auth.email, getHashWithSalt(auth.password), uid = auth.uid))) {
                        val user = User(auth.email, username, uid = auth.uid)
                        if(addUser(user)) {
                            call.respond(OK, SimpleResponse(true, "Successfully created account"))
                        }
                        else {
                            call.respond(OK, SimpleResponse(false, "An unknown error occurred"))
                        }
                    }
                    else {
                        call.respond(OK, SimpleResponse(false, "An unknown error occurred"))
                    }
                }
                else {
                    call.respond(OK, SimpleResponse(false, "User already exists"))
                }


            }
        }
    }

    route("/login/{token}") {
        post {
            withContext(Dispatchers.IO) {
                val request = try {
                    call.receive<AccountRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@withContext
                }

                val token = call.parameters["token"]!!
                val uid = getUidByEmail(request.email)

                val passwordIsCorrect = checkIfPasswordIsCorrect(request.email, request.password)


                if(passwordIsCorrect) {
                    uid?.let {
                        if(addTokenForUser(it, token)) {
                            call.respond(OK, SimpleResponse(true, "Successfully logged in"))
                        }
                        else {
                            call.respond(OK, SimpleResponse(false, "Error occurred"))
                        }
                    } ?: call.respond(OK, SimpleResponse(false, "Error occurred"))
                }
                else {
                    call.respond(OK, SimpleResponse(false, "Email or password incorrect"))
                }


            }
        }
    }

    route("/removeToken") {
        authenticate {
            post {
                withContext(Dispatchers.IO) {

                    val request = try {
                        call.receive<RemoveTokenRequest>()
                    } catch (e: ContentTransformationException) {
                        call.respond(BadRequest)
                        return@withContext
                    }

                    if (removeTokenForUser(request.uid, request.token)) {
                        call.respond(OK, SimpleResponse(true, "Successful"))
                    } else {
                        call.respond(OK, SimpleResponse(false, "Error"))
                    }


                }
            }
        }
    }

    route("/getTokens/{uid}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {

                    val uid = call.parameters["uid"]!!

                    val tokens = getTokens(uid)

                    call.respond(OK, tokens)


                }
            }
        }
    }

    route("/getUid") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name

                val uid = getUidByEmail(email)

                uid?.let {
                    call.respond(OK, it)
                }
            }
        }
    }

}