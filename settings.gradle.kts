pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "slapshot"

include(
    "core",
    "junit4",
    "junit5",
    "plugin",
    "sample",
)
