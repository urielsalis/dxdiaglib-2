import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "com.urielsalis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jsoup:jsoup:1.10.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.4")
}

tasks.withType<ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "com.urielsalis.driverparser.MainKt"
    }
}
