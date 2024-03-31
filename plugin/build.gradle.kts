plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    `kotlin-dsl`
    `maven-publish`
}

group = extra["mavenGroup"]!!
version = libs.versions.slapshot.get()

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.strikt)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
}

gradlePlugin {
    plugins {
        create("slapshot") {
            id = "se.gustavkarlsson.slapshot"
            implementationClass = "se.gustavkarlsson.slapshot.plugin.SlapshotPlugin"
        }
    }
}
