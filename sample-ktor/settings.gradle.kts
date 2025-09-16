@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal() // Only needed for the slapshot plugin development
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        mavenLocal() // Only needed for the slapshot plugin development
        gradlePluginPortal()
    }
}

rootProject.name = "slapshot-sample-ktor"
