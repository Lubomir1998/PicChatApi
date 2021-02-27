package com.example.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Post(
    val imgUrl: String,
    val authorUid: String,
    val description: String,
    val date: Long,
    var likes: List<String> = listOf(),
    var comments: Int = 0,
    @BsonId
    val id: String = ObjectId().toString()
)
