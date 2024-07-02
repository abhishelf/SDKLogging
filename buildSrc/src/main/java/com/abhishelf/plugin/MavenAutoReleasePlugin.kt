package com.abhishelf.plugin

import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class MavenAutoReleasePlugin : Plugin<Project> {

    private var userName: String? = null
    private var userPassword: String? = null
    private var signingKey: String? = null
    private var signingKeyId: String? = null
    private var signingPassword: String? = null

    private val service by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(NexusOkHttpInterceptor(Credentials.basic(userName!!, userPassword!!)))
            .build()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl("https://s01.oss.sonatype.org/service/local/staging/")
            .build()

        retrofit.create(NexusService::class.java)
    }

    override fun apply(project: Project) {

        configurePublish(project)
        project.tasks.register("autoPublishToMaven") {
            description = "Publishes repo it to MavenCentral"
            group = "publishing"
            dependsOn(project.tasks.named("publish"))
            doLast {
                println("-------- START --------")
                println(project.providers.gradleProperty("mavenCentralUsername"))
                println(project.providers.gradleProperty("mavenCentralPassword"))
                println(project.providers.gradleProperty("signingInMemoryKey"))
                println(project.providers.gradleProperty("signingInMemoryKeyId"))
                println(project.providers.gradleProperty("signingInMemoryKeyPassword"))

                println("-------- CONTINUING --------")

                userName = project.providers.gradleProperty("mavenCentralUsername").get()
                userPassword = project.providers.gradleProperty("mavenCentralPassword").get()
                signingKey = project.providers.gradleProperty("signingInMemoryKey").get()
                signingKeyId = project.providers.gradleProperty("signingInMemoryKeyId").get()
                signingPassword =
                    project.providers.gradleProperty("signingInMemoryKeyPassword").get()
                println("-------- END --------")

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
                        url =
                            project.uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    }
                }

                register("release", MavenPublication::class.java) {
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
                                name.set(project.findProperty("POM_LICENCE_NAME") as String?)
                                url.set(project.findProperty("Add to standalone scripts") as String?)
                            }
                        }
                        developers {
                            developer {
                                id.set(project.findProperty("POM_DEVELOPER_ID") as String?)
                                name.set(project.findProperty("POM_DEVELOPER_NAME") as String?)
                                email.set(project.findProperty("POM_DEVELOPER_EMAIL") as String?)
                            }
                        }
                        scm {
                            url.set(project.findProperty("POM_SCM_URL") as String?)
                            connection.set(project.findProperty("POM_SCM_CONNECTION") as String?)
                            developerConnection.set(project.findProperty("POM_SCM_DEV_CONNECTION") as String?)
                        }
                    }
                }
            }
        }

        project.extensions.configure(SigningExtension::class.java) {
            useInMemoryPgpKeys(
                signingKeyId,
                signingKey,
                signingPassword,
            )

            sign(
                project.extensions.getByType(PublishingExtension::class.java)
                    .publications.getByName("release")
            )
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