package com.example.data.collections

import com.example.Constants.DEFAULT_PROFILE_IMG_URL
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val email: String,
    val username: String,
    val description: String = "",
    val profileImgUrl: String = DEFAULT_PROFILE_IMG_URL,
    var following: List<String> = listOf(),
    var followers: List<String> = listOf(),
    var posts: Int = 0,
    @BsonId
    val uid: String = ObjectId().toString()
)
