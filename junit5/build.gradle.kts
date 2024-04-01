import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = extra["mavenGroup"]!!
version = findProperty("releaseVersion") as String

dependencies {
    implementation(project(":core"))
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter.api)
    implementation(libs.junit.jupiter.params)

    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.strikt)
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.add("-opt-in=se.gustavkarlsson.slapshot.core.InternalSlapshotApi")
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
