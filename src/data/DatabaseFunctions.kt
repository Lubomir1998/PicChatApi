package com.example.data

import com.example.data.collections.*
import com.example.security.checkHashForPassword
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

val client = KMongo.createClient().coroutine
val database = client.getDatabase("PicChatDB")

val users = database.getCollection<User>()
val auths = database.getCollection<Auth>()
val posts = database.getCollection<Post>()
val comments = database.getCollection<Comment>()
val notifications = database.getCollection<Notification>()



// Auth & Users

suspend fun registerUser(auth: Auth): Boolean {
    return auths.insertOne(auth).wasAcknowledged()
}

suspend fun addTokenForUser(uid: String, token: String): Boolean {
    val auth = auths.findOneById(uid) ?: return false
    return auths.updateOneById(uid, setValue(Auth::tokens, auth.tokens + token)).wasAcknowledged()
}

suspend fun removeTokenForUser(uid: String, token: String): Boolean {
    val auth = auths.findOneById(uid) ?: return false
    return auths.updateOneById(uid, setValue(Auth::tokens, auth.tokens - token)).wasAcknowledged()
}

suspend fun getTokens(uid: String): List<String> {
    return auths.findOneById(uid)?.tokens ?: return emptyList()
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

suspend fun toggleFollow(uid: String, currentUserEmail: String): Boolean {
    val user = users.findOneById(uid) ?: return false
    val currentUser = users.findOne(User::email eq currentUserEmail) ?: return false
    return users
            .updateOneById(uid, setValue(User::followers, if(user.followers.contains(currentUser.uid)) user.followers - currentUser.uid else user.followers + currentUser.uid))
            .wasAcknowledged()
            .also {
                if(it) users
                        .updateOneById(currentUser.uid, setValue(User::following, if(currentUser.following.contains(uid)) currentUser.following - uid else currentUser.following + uid))
            }

}

suspend fun getAllFollowing(uid: String): List<String> {
    val user = users.findOneById(uid) ?: return emptyList()
    return user.following
}

suspend fun getAllFollowers(uid: String): List<String> {
    val user = users.findOneById(uid) ?: return emptyList()
    return user.followers
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
    val postComments = comments.find().toList().filter {
        it.postId == postId
    }
    val postNotifications = notifications.find().toList().filter {
        it.postId == postId
    }
    val user = users.findOneById(uid) ?: return false
    return posts.deleteOneById(postId).wasAcknowledged().also {
        if(it) users.updateOneById(uid, setValue(User::posts, user.posts - 1))
        for (comment in postComments) {
            comments.deleteOneById(comment.id)
        }
        for(notification in postNotifications) {
            notifications.deleteOneById(notification.id)
        }
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

suspend fun getPostById(postId: String): Post? {
    return posts.findOneById(postId)
}


// Comments

suspend fun addComment(comment: Comment, postId: String): Boolean {
    val post = posts.findOneById(postId) ?: return false
    return comments.insertOne(comment).wasAcknowledged()
            .also {
        if(it) posts.updateOneById(postId, setValue(Post::comments, post.comments + 1))
    }
}

suspend fun deleteComment(comment: Comment): Boolean {
    val post = posts.findOneById(comment.postId) ?: return false
    return comments.deleteOneById(comment.id).wasAcknowledged()
            .also {
                if (it) posts.updateOneById(comment.postId, setValue(Post::comments, post.comments - 1))
            }
}

suspend fun getCommentsForPost(postId: String): List<Comment> {
    return comments.find(Comment::postId eq postId).toList()
}


// Notifications

suspend fun addNotification(notification: Notification): Boolean {
    if(notification.senderUid == notification.recipientUid) return false
    return notifications.insertOne(notification).wasAcknowledged()
}

suspend fun getNotifications(uid: String): List<Notification> {
    return notifications.find(Notification::recipientUid eq uid).toList()
}









