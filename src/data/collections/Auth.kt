package com.example.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Auth(
    val email: String,
    val password: String,
    var tokens: List<String> = listOf(),
    @BsonId
    val uid: String = ObjectId().toString()
)
