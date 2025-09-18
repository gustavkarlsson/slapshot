plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.3"
    id("se.gustavkarlsson.slapshot") version "dev" // Replace version with a release version
}

group = "se.gustavkarlsson.slapshot.sample.ktor"
version = "0.0.1"

application {
    mainClass = "se.gustavkarlsson.slapshot.sample.ktor.ApplicationKt"
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("se.gustavkarlsson.slapshot:slapshot-ktor3") // Plugin sets version automatically
}
