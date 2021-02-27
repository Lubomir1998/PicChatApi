package com.example.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Comment(
        val authorUid: String,
        val postId: String,
        val text: String,
        @BsonId
        val id: String = ObjectId().toString()
)
