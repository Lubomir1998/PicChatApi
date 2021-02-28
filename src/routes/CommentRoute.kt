package com.example.routes

import com.example.data.addComment
import com.example.data.collections.Comment
import com.example.data.getCommentsForPost
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Route.commentRoute() {

    route("/addComment") {
        authenticate {
            post {
                withContext(Dispatchers.IO) {
                    val request = try {
                        call.receive<Comment>()
                    }
                    catch (e: ContentTransformationException) {
                        call.respond(BadRequest)
                        return@withContext
                    }

                    if(addComment(request)) {
                        call.respond(OK)
                    }
                    else {
                        call.respond(Conflict)
                    }

                }
            }
        }
    }

    route("/getComments/{id}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {

                    val postId = call.parameters["id"]!!

                    val comments = getCommentsForPost(postId)

                    call.respond(OK, comments)

                }
            }
        }
    }

}