import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    configure(KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGenerate")))
    pom {
        name.set("Slapshot JUnit 4")
        description.set("A snapshot testing library for Kotlin")
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
