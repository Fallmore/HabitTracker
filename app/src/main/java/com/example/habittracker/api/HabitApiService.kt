package com.example.habittracker.api

import com.example.habittracker.model.RemoteHabit
import retrofit2.Response
import retrofit2.http.*

interface HabitApiService {

    // JSONPlaceholder имеет endpoint /posts, используем его для имитации
    @GET("posts")
    suspend fun getHabits(): Response<List<RemoteHabit>>

    @GET("posts/{id}")
    suspend fun getHabit(@Path("id") id: Int): Response<RemoteHabit>

    @POST("posts")
    suspend fun createHabit(@Body habit: RemoteHabit): Response<RemoteHabit>

    @PUT("posts/{id}")
    suspend fun updateHabit(@Path("id") id: Int, @Body habit: RemoteHabit): Response<RemoteHabit>

    @DELETE("posts/{id}")
    suspend fun deleteHabit(@Path("id") id: Int): Response<Unit>
}