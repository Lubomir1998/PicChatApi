package com.example.data

import com.example.data.collections.Auth
import com.example.data.collections.Post
import com.example.data.collections.User
import com.example.security.checkHashForPassword
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

val client = KMongo.createClient().coroutine
val database = client.getDatabase("PicChatDB")

val users = database.getCollection<User>()
val auths = database.getCollection<Auth>()
val posts = database.getCollection<Post>()


suspend fun registerUser(auth: Auth): Boolean {
    return auths.insertOne(auth).wasAcknowledged()
}

suspend fun addUser(user: User): Boolean {
    return users.insertOne(user).wasAcknowledged()
}

suspend fun checkIfUserExists(email: String): Boolean {
    return auths.findOne(Auth::email eq email) != null
}

suspend fun checkIfPasswordIsCorrect(email: String, password: String): Boolean {
    val actualPassword = auths.findOne(Auth::email eq email)?.password ?: return false
    return checkHashForPassword(password, actualPassword)
}

suspend fun createPost(post: Post): Boolean {
    return posts.insertOne(post).wasAcknowledged()
}

suspend fun getPostsOfFollowing(following: List<String>): List<Post> {
    val allPosts = posts.find().toList()
    return allPosts.filter {
        following.contains(it.authorUid)
    }.toList()
}

suspend fun getUidByEmail(email: String): String? {
    return auths.findOne(Auth::email eq email)?.uid
}

suspend fun getUserByEmail(email: String): User? {
    return users.findOne(User::email eq email)
}

suspend fun getUserById(uid: String): User? {
    return users.findOneById(uid)
}
