import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    signing
}

group = extra["mavenGroup"]!!
version = findProperty("releaseVersion") as String

dependencies {
    api(project(":slapshot-core"))
    api(project(":slapshot-json"))
    compileOnly(libs.ktor.client.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.junit.jupiter.api)

    testImplementation(project(":slapshot-junit5"))
    testImplementation(libs.junit.jupiter.aggregator)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.strikt)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.server.core)
    testImplementation(libs.ktor.server.test.host)
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.add("-opt-in=se.gustavkarlsson.slapshot.core.InternalSlapshotApi")
}

tasks.test {
    useJUnitPlatform()
    val snapshotsDir = project.layout.projectDirectory.dir("snapshots")
    inputs.files(snapshotsDir)
    outputs.files(fileTree(snapshotsDir))
}

kotlin {
    explicitApi()
}

mavenPublishing {
    publishToMavenCentral()
    configure(KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGenerate")))
    pom {
        name.set("Slapshot Ktor 3")
        description.set("A slapshot extension for Ktor 3")
        inceptionYear.set("2025")
        url.set("https://github.com/gustavkarlsson/slapshot/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
            }
        }
        developers {
            developer {
                id.set("gustavkarlsson")
                name.set("Gustav Karlsson")
                url.set("https://github.com/gustavkarlsson/")
            }
        }
        scm {
            url.set("https://github.com/gustavkarlsson/slapshot/")
            connection.set("scm:git:git://github.com/gustavkarlsson/slapshot.git")
            developerConnection.set("scm:git:ssh://git@github.com/gustavkarlsson/slapshot.git")
        }
    }
}

signing {
    val signingKey = findProperty("signingKey") as? String
    val signingPassword = findProperty("signingPassword") as? String
    isRequired = signingKey != null && signingPassword != null
    useInMemoryPgpKeys(signingKey, signingPassword)
}
