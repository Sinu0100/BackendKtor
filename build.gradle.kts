plugins {
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

group = ""
version = "0.0.1"

application {
    mainClass.set("ApplicationKt")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "ApplicationKt"
    }
}

extra["mainClassName"] = "ApplicationKt"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    // Supabase
    val supabaseVersion = "3.0.3"
    implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:auth-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:storage-kt:$supabaseVersion")
    implementation("io.ktor:ktor-client-cio:3.0.3")

    // Ktor Server
    val ktorVersion = "3.0.3"
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
    
    // Plugins
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-request-validation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-rate-limit-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-compression-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-caching-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-conditional-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-hsts-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-http-redirect-jvm:$ktorVersion")
    
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // Kotlinx Serialization Core (Penting!)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
}
