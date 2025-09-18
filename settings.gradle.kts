@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "slapshot"

include(
    "slapshot-core",
    "slapshot-junit4",
    "slapshot-junit5",
    "slapshot-json",
    "slapshot-ktor3",
    "slapshot-plugin",
    "slapshot-bom",
)
