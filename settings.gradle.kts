plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "ktor-afel"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://packages.confluent.io/maven/")
    }
}
