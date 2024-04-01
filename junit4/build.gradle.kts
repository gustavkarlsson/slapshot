import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = extra["mavenGroup"]!!
version = findProperty("releaseVersion") as String

dependencies {
    implementation(project(":core"))
    implementation(libs.junit.four)
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.add("-opt-in=se.gustavkarlsson.slapshot.core.InternalSlapshotApi")
}

tasks.test {
    useJUnit()
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
