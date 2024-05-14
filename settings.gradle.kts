pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":app",
    ":sdk-logger"
)
rootProject.name = "SDKLogging"

nameBuildScriptsAfterProjectNames(rootProject.children)

fun nameBuildScriptsAfterProjectNames(projects: Set<ProjectDescriptor>) {
    for (project in projects) {
        project.buildFileName = project.name + ".gradle.kts"
        nameBuildScriptsAfterProjectNames(project.children)
    }
}

includeBuild("publishing-plugin")