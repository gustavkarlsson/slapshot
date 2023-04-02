import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    `maven-publish`
}

subprojects {
    extra["mavenGroup"] = "se.gustavkarlsson.slapshot"

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = libs.versions.jvmtarget.get()
    }
    tasks.withType<JavaCompile> {
        targetCompatibility = libs.versions.jvmtarget.get()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
