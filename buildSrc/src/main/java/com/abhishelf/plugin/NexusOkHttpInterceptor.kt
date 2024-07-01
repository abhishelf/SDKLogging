package com.abhishelf.plugin

import okhttp3.Interceptor
import okhttp3.Response


internal class NexusOkHttpInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        requestBuilder.addHeader("Accept", "application/json")
        requestBuilder.addHeader(
            "Authorization",
            "{HARD_CODED_TOKEN}"
        )

        return chain.proceed(requestBuilder.build())
    }
}
