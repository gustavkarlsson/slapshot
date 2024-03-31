import se.gustavkarlsson.slapshot.plugin.TestFramework

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.slapshot)
}

group = extra["mavenGroup"]!!
version = libs.versions.slapshot.get()

dependencies {
    testImplementation(libs.junit.four)
}

slapshot {
    testFramework.set(TestFramework.JUnit4)
}
