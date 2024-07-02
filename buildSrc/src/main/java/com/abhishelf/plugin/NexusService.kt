package com.abhishelf.plugin

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface NexusService {

    @GET("profile_repositories/13968d1425dd30")
    fun getProfileRepository(): Call<ProfileRepositoriesResponse>

    @POST("profiles/13968d1425dd30/finish")
    fun finish(
        @Body input: TransitionRepositoryInput,
    ): Call<Unit>

    @POST("profiles/13968d1425dd30/promote")
    fun promote(
        @Body input: TransitionRepositoryInput,
    ): Call<Unit>
}