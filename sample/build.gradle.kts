import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("se.gustavkarlsson.slapshot") version "1.0-SNAPSHOT"
}

group = "se.gustavkarlsson.slapshot"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

slapshot {
    logger.info("setting snapshots dir")
    snapshotRootDir.set("snapshotts")
}