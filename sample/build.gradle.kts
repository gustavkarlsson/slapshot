plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.slapshot)
}

group = extra["mavenGroup"]!!
version = libs.versions.slapshot.get()

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

slapshot {
    snapshotRootDir.set("snapshots")
}
