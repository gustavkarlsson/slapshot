import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = extra["mavenGroup"]!!
version = libs.versions.slapshot.get()

dependencies {
    implementation(project(":core"))
    implementation(libs.junit.jupiter.engine)
    implementation(libs.junit.jupiter.params)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-opt-in=se.gustavkarlsson.slapshot.core.InternalSlapshotApi"
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}
