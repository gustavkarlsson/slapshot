import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
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
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    // FIXME Add pom
}
