plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    `kotlin-dsl`
    `maven-publish`
}

group = extra["mavenGroup"]!!
version = libs.versions.slapshot.get()

dependencies {
    implementation(libs.junit.jupiter.engine)
    testImplementation(libs.kotlin.test)
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
