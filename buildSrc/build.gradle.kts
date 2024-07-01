plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("mavenAutoReleasePlugin") {
            id = "com.abhishelf.plugin"
            implementationClass = "com.abhishelf.plugin.MavenAutoReleasePlugin"
        }
    }
}

dependencies {
    api(gradleApi())
    api("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}