import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    `maven-publish`
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extra["mavenGroup"] = "se.gustavkarlsson.slapshot"

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = libs.versions.jvmtarget.get()
    }
    tasks.withType<JavaCompile> {
        targetCompatibility = libs.versions.jvmtarget.get()
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
