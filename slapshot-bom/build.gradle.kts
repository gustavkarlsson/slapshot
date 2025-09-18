import com.vanniktech.maven.publish.JavaPlatform

plugins {
    `java-platform`
    alias(libs.plugins.maven.publish)
    signing
}

group = extra["mavenGroup"]!!
version = findProperty("releaseVersion") as String

dependencies {
    constraints {
        for (subProject in rootProject.subprojects) {
            if (subProject != project) {
                api(subProject)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    configure(JavaPlatform())
    pom {
        name.set("Slapshot BOM")
        description.set("The slapshot Bill Of Materials")
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
