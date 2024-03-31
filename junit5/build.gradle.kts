import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = extra["mavenGroup"]!!
version = libs.versions.slapshot.get()

dependencies {
    implementation(project(":core"))
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter.api)
    implementation(libs.junit.jupiter.params)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.strikt)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-opt-in=se.gustavkarlsson.slapshot.core.InternalSlapshotApi"
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}
