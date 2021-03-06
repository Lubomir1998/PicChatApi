package com.example.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Notification (
        val senderUid: String,
        val recipientUid: String,
        val message: String,
        val postId: String? = null,
        val postImgUrl: String? = null,
        val timestamp: Long = System.currentTimeMillis(),
        @BsonId
        val id: String = ObjectId().toString()
)