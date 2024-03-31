import se.gustavkarlsson.slapshot.plugin.TestFramework

plugins {
    kotlin("jvm") version "1.9.22"
    id("se.gustavkarlsson.slapshot") version "latest.integration" // Replace version with a release version
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

slapshot {
    testFramework.set(TestFramework.JUnit4)
}
