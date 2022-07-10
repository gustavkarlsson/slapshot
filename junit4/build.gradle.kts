import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = extra["mavenGroup"]!!
version = libs.versions.slapshot.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.junit.four)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-opt-in=se.gustavkarlsson.slapshot.core.InternalSlapshotApi"
}

tasks.test {
    useJUnit()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}
