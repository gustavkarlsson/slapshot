import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extra["mavenGroup"] = "se.gustavkarlsson.slapshot"

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(libs.versions.jvmtarget.map(JvmTarget::fromTarget))
    }
    tasks.withType<JavaCompile> {
        targetCompatibility = libs.versions.jvmtarget.get()
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
