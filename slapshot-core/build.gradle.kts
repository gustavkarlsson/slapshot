import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    signing
}

group = extra["mavenGroup"]!!
version = findProperty("releaseVersion") as String

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.serialization.json)

    testImplementation(libs.junit.jupiter.aggregator)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.strikt)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
    compilerOptions {
        freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    configure(KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGenerate")))
    pom {
        name.set("Slapshot Core")
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
