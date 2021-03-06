plugins {
    kotlin("multiplatform")
}

group = "io.github.jan-tennert.discordkm"
version = Versions.DISCORDKM
description = "A Http Interaction Client for DiscordKM if you want to receive interaction via HTTP rather than a websocket"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs = listOf(
                "-Xjvm-default=all",  // use default methods in interfaces,
                "-Xlambdas=indy"      // use invokedynamic lambdas instead of synthetic classes
            )
        }
        withJava()
    }

    sourceSets {
        all { languageSettings { optIn("kotlin.RequiresOptIn") } }
        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}")
                implementation(project(":"))
                api("io.ktor:ktor-server-core:${Versions.KTOR}")
                api("io.ktor:ktor-server-cio:${Versions.KTOR}")
            }
        }
    }
}