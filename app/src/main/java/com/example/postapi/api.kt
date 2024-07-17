package com.example.postapi

import retrofit2.http.Body
import retrofit2.http.POST

interface api {

    @POST("postme/")
    suspend fun sendName(@Body data: NameRequest): ApiResponse
}