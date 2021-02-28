package com.example.data.requests

data class UpdateUserRequest(
        var profileImgUrl: String?,
        val username: String,
        val bio: String
)
