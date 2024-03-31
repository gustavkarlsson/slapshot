plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.slapshot)
}

group = extra["mavenGroup"]!!
version = libs.versions.slapshot.get()

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()
}
