plugins {
    kotlin("jvm") version "1.7.0" apply false
    `maven-publish`
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}
