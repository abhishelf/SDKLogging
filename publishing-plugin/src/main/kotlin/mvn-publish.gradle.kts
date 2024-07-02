plugins {
    `maven-publish`
    signing
}

val mavenSnapshotUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
val mavenCentralUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

val libVersionName = project.findProperty("VERSION_NAME") as String
val group = project.findProperty("GROUP") as String
val artifactName = project.findProperty("ARTIFACT_NAME") as String

val pomName = project.findProperty("NAME") as String
val pomDescription = project.findProperty("POM_DESCRIPTION") as String
val projectUrl = project.findProperty("POM_URL") as String

val licenseName = project.findProperty("POM_LICENCE_NAME") as String
val licenseUrl = project.findProperty("POM_LICENCE_URL") as String

val developerId = project.findProperty("POM_DEVELOPER_ID") as String
val developerName = project.findProperty("POM_DEVELOPER_NAME") as String

val scmConnection = project.findProperty("POM_SCM_CONNECTION") as String
val scmDevConnection = project.findProperty("POM_SCM_DEV_CONNECTION") as String

val repositoryUsername = project.findProperty("mavenCentralUsername") as? String ?: ""
val repositoryPassword = project.findProperty("mavenCentralPassword") as? String ?: ""

//val repositoryUsername = System.getenv("OSS_USERNAME")
//val repositoryPassword = System.getenv("OSS_PASSWORD")

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = group
            artifactId = artifactName
            version = libVersionName
            afterEvaluate {
                from(components["release"])
            }
            pom {
                name.set(pomName)
                description.set(pomDescription)
                url.set(projectUrl)
                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(developerId)
                        name.set(developerName)
                    }
                }
                scm {
                    connection.set(scmConnection)
                    developerConnection.set(scmDevConnection)
                    url.set(projectUrl)
                }
            }
        }
        repositories {
            maven {
                credentials {
                    username = repositoryUsername
                    password = repositoryPassword
                }
                url = when {
                    libVersionName.endsWith("-SNAPSHOT") -> {
                        mavenSnapshotUrl
                    }
                    else -> {
                        mavenCentralUrl
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("OSS_SIGNING_KEY_ID"),
        System.getenv("OSS_SIGNING_KEY"),
        System.getenv("OSS_SIGNING_PASSWORD"),
    )
    sign(publishing.publications["release"])
}