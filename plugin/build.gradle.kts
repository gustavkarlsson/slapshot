import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    `kotlin-dsl`
    `maven-publish`
}

group = "se.gustavkarlsson.slapshot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("slapshot") {
            id = "se.gustavkarlsson.slapshot"
            implementationClass = "se.gustavkarlsson.slapshot.plugin.SlapshotPlugin"
        }
    }
}
