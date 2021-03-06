package com.example.routes

import com.example.data.addNotification
import com.example.data.collections.Notification
import com.example.data.getNotifications
import com.example.data.responses.SimpleResponse
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Route.notificationRoute() {

    route("/addNotification") {
        authenticate {
            post {
                withContext(Dispatchers.IO) {

                    val notification = try {
                        call.receive<Notification>()
                    }
                    catch (e: ContentTransformationException) {
                        call.respond(BadRequest)
                        return@withContext
                    }

                    if(addNotification(notification)) {
                        call.respond(OK, SimpleResponse(true, "Added successfully"))
                    }
                    else {
                        call.respond(Conflict)
                    }


                }
            }
        }
    }

    route("/getActivity/{uid}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {

                    val uid = call.parameters["uid"]!!

                    val notifications = getNotifications(uid)

                    call.respond(OK, notifications)

                }
            }
        }
    }

}