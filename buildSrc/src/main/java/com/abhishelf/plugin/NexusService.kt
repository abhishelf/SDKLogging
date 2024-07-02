package com.abhishelf.plugin

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface NexusService {

    @GET("profile_repositories/{HARD_CODED_STAGING_PROFILE_ID}")
    fun getProfileRepository(): Call<ProfileRepositoriesResponse>

    @POST("profiles/{HARD_CODED_STAGING_PROFILE_ID}/finish")
    fun finish(
        @Body input: TransitionRepositoryInput,
    ): Call<Unit>

    @POST("profiles/{HARD_CODED_STAGING_PROFILE_ID}/drop")
    fun drop(
        @Body input: TransitionRepositoryInput,
    ): Call<Unit>

    @POST("profiles/{HARD_CODED_STAGING_PROFILE_ID}/promote")
    fun promote(
        @Body input: TransitionRepositoryInput,
    ): Call<Unit>
}