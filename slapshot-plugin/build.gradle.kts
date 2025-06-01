import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated

plugins {
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.dokka)
    signing
}

group = extra["mavenGroup"]!!
version = findProperty("releaseVersion") as String

dependencies {
    testImplementation(libs.junit.jupiter.aggregator)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.strikt)
}

tasks.test {
    useJUnitPlatform()
}

val versionPropertiesResources = file(project.layout.buildDirectory.file("generated/resources"))

val generateVersionPropertiesFileTask =
    tasks.register("generateVersionPropertiesFile") {
        val gradlePropertyKey = "releaseVersion"
        val releaseVersion = project.findProperty(gradlePropertyKey) as String
        inputs.property(gradlePropertyKey, releaseVersion)
        val versionPropertiesFile = versionPropertiesResources.resolve("plugin.properties")
        outputs.file(versionPropertiesFile)
        doLast {
            versionPropertiesFile.ensureParentDirsCreated()
            val text = "releaseVersion=$releaseVersion"
            versionPropertiesFile.writeText(text)
            logger.info("Wrote '$text' to $versionPropertiesFile")
        }
    }

tasks.processResources {
    dependsOn(generateVersionPropertiesFileTask)
}

tasks.withType<Jar> {
    dependsOn(generateVersionPropertiesFileTask)
}

kotlin {
    explicitApi()
    sourceSets.main.configure {
        resources.srcDir(versionPropertiesResources)
    }
}

gradlePlugin {
    website.set("https://github.com/gustavkarlsson/slapshot/")
    vcsUrl.set("https://github.com/gustavkarlsson/slapshot/")

    plugins {
        create("slapshot") {
            id = "se.gustavkarlsson.slapshot"
            displayName = "Slapshot"
            description = "Snapshot testing for Kotlin applications"
            tags = listOf("kotlin", "snapshot", "testing", "junit", "junit4", "junit5")
            implementationClass = "se.gustavkarlsson.slapshot.plugin.SlapshotPlugin"
        }
    }
}

signing {
    val signingKey = findProperty("signingKey") as? String
    val signingPassword = findProperty("signingPassword") as? String
    isRequired = signingKey != null && signingPassword != null
    useInMemoryPgpKeys(signingKey, signingPassword)
}
