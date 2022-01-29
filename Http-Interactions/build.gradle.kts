plugins {
    kotlin("jvm")
}

group = "io.github.jan-tennert.discordkm.http-interactions"
version = Versions.DISCORDKM

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}")
    implementation(project(":"))
    implementation("io.ktor:ktor-server-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-cio:${Versions.KTOR}")
}
