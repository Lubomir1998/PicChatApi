package com.example.routes

import com.example.data.getUserById
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
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

}