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
    "plugin",
    "sample",
)
