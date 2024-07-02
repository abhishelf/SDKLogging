package com.abhishelf.plugin

import okhttp3.OkHttpClient
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
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
        configurePublish(project)
        project.tasks.register("autoPublishToMaven") {
            description = "Publishes repo it to MavenCentral"
            group = "publishing"
            dependsOn(project.tasks.named("publish"))
            doLast {
                closeAndRelease()
            }
        }
    }

    private fun configurePublish(project: Project) {
        project.plugins.apply("maven-publish")
        project.plugins.apply("signing")

        project.extensions.configure(PublishingExtension::class.java) {
            publications {
                repositories {
                    maven {
                        url = project.uri("file://Users/abhishek/Desktop/Temp/publish/staging/12345")
                    }
                }

                register("release",  MavenPublication::class.java) {
                    project.afterEvaluate {
                        from(project.components.getByName("release"))
                    }

                    groupId = project.findProperty("GROUP") as String?
                    artifactId = project.findProperty("ARTIFACT_NAME") as String?
                    version = project.findProperty("VERSION_NAME") as String?

                    pom {
                        name.set(project.findProperty("NAME") as String?)
                        description.set(project.findProperty("POM_DESCRIPTION") as String?)
                        url.set(project.findProperty("POM_URL") as String?)
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("developerId")
                                name.set("Developer Name")
                                email.set("developer@example.com")
                            }
                        }
                        scm {
                            url.set("https://github.com/username/repository")
                            connection.set("scm:git:git://github.com/username/repository.git")
                            developerConnection.set("scm:git:ssh://github.com:username/repository.git")
                        }
                    }
                }
            }
        }

        project.extensions.configure(SigningExtension::class.java) {
            sign(project.extensions.getByType(PublishingExtension::class.java).publications.getByName("release"))
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