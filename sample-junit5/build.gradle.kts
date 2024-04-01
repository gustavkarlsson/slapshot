plugins {
    kotlin("jvm") version "1.9.22"
    id("se.gustavkarlsson.slapshot") version "dev" // Replace version with a release version
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
