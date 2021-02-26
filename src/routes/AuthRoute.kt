package com.example.routes

import com.example.data.*
import com.example.data.collections.Auth
import com.example.data.collections.User
import com.example.data.requests.AccountRequest
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
                    if(registerUser(Auth(auth.email, getHashWithSalt(auth.password), auth.uid))) {
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

    route("/login") {
        post {
            withContext(Dispatchers.IO) {
                val request = try {
                    call.receive<AccountRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@withContext
                }

                val passwordIsCorrect = checkIfPasswordIsCorrect(request.email, request.password)

                if(passwordIsCorrect) {
                    call.respond(OK, SimpleResponse(true, "Successfully logged in"))
                }
                else {
                    call.respond(OK, SimpleResponse(false, "Email or password incorrect"))
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