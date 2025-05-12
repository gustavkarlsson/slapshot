import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extra["mavenGroup"] = "se.gustavkarlsson.slapshot"

    tasks.withType<KotlinCompile> {
        kotlinExtension.jvmToolchain(libs.versions.java.get().toInt())
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
