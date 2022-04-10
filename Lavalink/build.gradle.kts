plugins {
    kotlin("multiplatform")
}

group = "io.github.jan-tennert.discordkm"
version = Versions.DISCORDKM
description = "Extends DiscordKM with a Lavalink Client"

repositories {
    mavenCentral()
}

kotlin {
    /* Targets configuration omitted. 
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
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
    js(IR) {
        browser()
        nodejs()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":"))
            }
        }
    }
}