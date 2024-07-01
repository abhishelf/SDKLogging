package com.abhishelf.plugin

import okhttp3.OkHttpClient
import org.gradle.api.Plugin
import org.gradle.api.Project
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// type=released
// type=closed
// type=open
open class MavenAutoReleasePlugin : Plugin<Project> {

    private val service by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(NexusOkHttpInterceptor())
            .build()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl("https://s01.oss.sonatype.org/service/local/staging/")
            .build()

        retrofit.create(NexusService::class.java)
    }

    override fun apply(project: Project) {
        print("MavenAutoReleasePlugin - will applying the plugin")

        project.tasks.register("autoPublishToMaven") {
            description = "Publishes repo it to MavenCentral"
            group = "publishing"
            dependsOn(project.tasks.named("publish"))
            doLast {
                closeAndRelease()
            }
        }
    }

    private fun closeAndRelease() {
        println("Will close and release repo")
        val response = service.getProfileRepository().execute()
        if (response.isSuccessful) {
            println(response.body())
            response.body()?.let {
                pendingItems.clear()
                pendingItems.addAll(it.data)
                moveToNextStep()
            }
        }
    }

    private val pendingItems = mutableListOf<Repository>()

    private fun moveToNextStep() {
        println("Moving to next step")
        for (repo in pendingItems) {
            if (repo.transitioning) continue
            else {
                if (repo.type == "open") {
                    println("Closing ${repo.repositoryId}")
                    val response =
                        service.finish(
                            TransitionRepositoryInput(
                                TransitionRepositoryInputData(
                                    repo.repositoryId,
                                    repo.repositoryId
                                )
                            )
                        ).execute()
                    println(response.code())
                    println(response)
                } else {
                    println("Promoting ${repo.repositoryId}")
                    val response =
                        service.promote(
                            TransitionRepositoryInput(
                                TransitionRepositoryInputData(
                                    repo.repositoryId,
                                    repo.repositoryId
                                )
                            )
                        ).execute()
                    println(response.code())
                    println(response)
                }
            }
        }
        Thread.sleep(5000)
        closeAndRelease()
    }
}