plugins {
    kotlin("jvm") version "1.9.22"
    id("se.gustavkarlsson.slapshot") version "latest.integration" // Replace version with a release version
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.test {
    useJUnitPlatform()
}
