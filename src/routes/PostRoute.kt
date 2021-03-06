package com.example.routes

import com.example.data.*
import com.example.data.collections.Post
import com.example.data.requests.ToggleLikeRequest
import com.example.data.responses.SimpleResponse
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

fun Route.postRoute() {

    route("/createPost") {
        authenticate {
            post {
                withContext(Dispatchers.IO) {

                    val post = try {
                        call.receive<Post>()
                    }
                    catch (e: ContentTransformationException) {
                        call.respond(BadRequest)
                        return@withContext
                    }

                    if(createPost(post, post.authorUid)) {
                        call.respond(OK, SimpleResponse(true, "Posted successfully"))
                    }
                    else {
                        call.respond(Conflict)
                    }


                }
            }
        }
    }

    route("/deletePost") {
        authenticate {
            post {
                withContext(Dispatchers.IO) {

                    val post = try {
                        call.receive<Post>()
                    }
                    catch (e: ContentTransformationException) {
                        call.respond(BadRequest)
                        return@withContext
                    }

                    if (deletePost(post.id, post.authorUid)) {
                        call.respond(OK, SimpleResponse(true, "Post deleted"))
                    } else {
                        call.respond(Conflict)
                    }

                }
            }
        }
    }

    route("/postOfFollowing") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val user = getUserByEmail(email)

                user?.let {
                    val posts = getPostsOfFollowing(it.following)
                    call.respond(OK, posts)
                }


            }
        }
    }

    route("getPost/{id}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {
                    val postId = call.parameters["id"]!!

                    val post = getPostById(postId)

                    post?.let {
                        call.respond(OK, it)
                    }

                }
            }
        }
    }

    route("/toggleLike") {
        authenticate {
            post {
                withContext(Dispatchers.IO) {

                    val request = try {
                        call.receive<ToggleLikeRequest>()
                    } catch (e: ContentTransformationException) {
                        call.respond(BadRequest)
                        return@withContext
                    }

                    if (toggleLike(request.postId, request.uid)) {
                        call.respond(OK)
                    } else {
                        call.respond(Conflict)
                    }

                }
            }
        }
    }

    route("/getPostsForUser/{uid}") {
        authenticate {
            get {
                withContext(Dispatchers.IO) {

                    val uid = call.parameters["uid"]!!

                    val posts = getPostsForProfile(uid)

                    call.respond(OK, posts)

                }
            }
        }
    }

}