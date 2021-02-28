package com.example.data

import com.example.data.collections.Auth
import com.example.data.collections.Comment
import com.example.data.collections.Post
import com.example.data.collections.User
import com.example.security.checkHashForPassword
import kotlinx.css.map
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue
import java.util.*

val client = KMongo.createClient().coroutine
val database = client.getDatabase("PicChatDB")

val users = database.getCollection<User>()
val auths = database.getCollection<Auth>()
val posts = database.getCollection<Post>()
val comments = database.getCollection<Comment>()



// Auth & Users

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

suspend fun getUidByEmail(email: String): String? {
    return auths.findOne(Auth::email eq email)?.uid
}

suspend fun getUserByEmail(email: String): User? {
    return users.findOne(User::email eq email)
}

suspend fun getUserById(uid: String): User? {
    return users.findOneById(uid)
}

suspend fun searchUsers(query: String, uid: String): List<User> {
    val users = users.find().toList()
    return users.filter { user ->
        user.email != uid && user.username.toLowerCase().contains(query.toLowerCase())
    }
}

suspend fun updateProfile(uid: String, profileImgUrl: String?, username: String, bio: String): Boolean {
    val user = users.findOneById(uid) ?: return false
    val newUser = User(user.email, username, bio, profileImgUrl ?: user.profileImgUrl, user.following, user.followers, user.posts, uid)
    return users.updateOneById(uid, newUser).wasAcknowledged()
}


// Posts

suspend fun createPost(post: Post, uid: String): Boolean {
    val user = users.findOneById(uid) ?: return false
    return posts.insertOne(post).wasAcknowledged().also {
        if(it) users.updateOneById(uid, setValue(User::posts, user.posts + 1))
    }
}

suspend fun deletePost(postId: String, uid: String): Boolean {
    val user = users.findOneById(uid) ?: return false
    return posts.deleteOneById(postId).wasAcknowledged().also {
        if(it) users.updateOneById(uid, setValue(User::posts, user.posts - 1))
    }
}

suspend fun getPostsOfFollowing(following: List<String>): List<Post> {
    val allPosts = posts.find().toList()
    return allPosts.filter {
        following.contains(it.authorUid)
    }.toList()
}

suspend fun toggleLike(postId: String, uid: String): Boolean {
    val post = posts.findOneById(postId) ?: return false
    val isLiked = uid in post.likes
    val likes = post.likes
    return posts.updateOneById(postId, setValue(Post::likes, if(isLiked) likes - uid else likes + uid)).wasAcknowledged()
}

suspend fun getPostsForProfile(uid: String): List<Post> {
    return posts.find(Post::authorUid eq uid).toList()
}


// Comments

suspend fun addComment(comment: Comment): Boolean {
    return comments.insertOne(comment).wasAcknowledged()
}

suspend fun deleteComment(id: String): Boolean {
    return comments.deleteOneById(id).wasAcknowledged()
}










