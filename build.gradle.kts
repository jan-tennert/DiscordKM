plugins {
    kotlin("multiplatform") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
    id("maven-publish")
    signing
    id("org.jetbrains.dokka") version Versions.DOKKA
    id("io.codearte.nexus-staging") version Versions.NEXUS_STAGING
}

subprojects {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
}

group = "io.github.jan-tennert.discordkm"
version = Versions.DISCORDKM
description = "A Kotlin Multiplatform Discord API Wrapper"

nexusStaging {
    stagingProfileId = Publishing.PROFILE_ID
    stagingRepositoryId.set(Publishing.REPOSITORY_ID)
    username = Publishing.SONATYPE_USERNAME
    password = Publishing.SONATYPE_PASSWORD
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
            name = "ktor-eap"
        }
    }

    signing {
        val signingKey = providers
            .environmentVariable("GPG_SIGNING_KEY")
            .forUseAtConfigurationTime()
        val signingPassphrase = providers
            .environmentVariable("GPG_SIGNING_PASSPHRASE")
            .forUseAtConfigurationTime()

        if (signingKey.isPresent && signingPassphrase.isPresent) {
            useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
            val extension = extensions
                .getByName("publishing") as PublishingExtension
            sign(extension.publications)
        }
    }
    publishing {
        repositories {
            maven {
                name = "Oss"
                setUrl {
                    "https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/${Publishing.REPOSITORY_ID}"
                }
                credentials {
                    username = Publishing.SONATYPE_USERNAME
                    password = Publishing.SONATYPE_PASSWORD
                }
            }
            maven {
                name = "Snapshot"
                setUrl { "https://s01.oss.sonatype.org/content/repositories/snapshots/" }
                credentials {
                    username = Publishing.SONATYPE_USERNAME
                    password = Publishing.SONATYPE_PASSWORD
                }
            }
        }
//val dokkaOutputDir = "H:/Programming/Other/DiscordKMDocs"
        val dokkaOutputDir = "$buildDir/dokka/${this@allprojects.name}"

        tasks.dokkaHtml {
            outputDirectory.set(file(dokkaOutputDir))
        }

        val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
            delete(dokkaOutputDir)
        }

        val javadocJar = tasks.register<Jar>("javadocJar") {
            dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
            archiveClassifier.set("javadoc")
            from(dokkaOutputDir)
        }

        publications {
            withType<MavenPublication> {
                artifact(javadocJar)
                pom {
                    name.set(this@allprojects.name)
                    description.set(this@allprojects.description ?: "A Kotlin Multiplatform Discord API Wrapper")
                    url.set("https://github.com/jan-tennert/DiscordKM")
                    licenses {
                        license {
                            name.set("GPL-3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }
                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/jan-tennert/DiscordKM/issues")
                    }
                    scm {
                        connection.set("https://github.com/jan-tennert/DiscordKM.git")
                        url.set("https://github.com/jan-tennert/DiscordKM")
                    }
                    developers {
                        developer {
                            name.set("TheRealJanGER")
                            email.set("jan.m.tennert@gmail.com")
                        }
                    }
                }
            }
        }
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            //kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"
        }
        withJava()
    }
    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        all { languageSettings { optIn("kotlin.RequiresOptIn") } }
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.SERIALIZATION}")
                api("com.soywiz.korlibs.klock:klock:${Versions.KORLIBS}")
                api("com.soywiz.korlibs.klogger:klogger:${Versions.KORLIBS}")
                api("io.ktor:ktor-client-core:${Versions.KTOR}")
                api("io.ktor:ktor-client-websockets:${Versions.KTOR}")
                api("co.touchlab:stately-iso-collections:${Versions.STATELY}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}")
                api("com.soywiz.korlibs.korio:korio:${Versions.KORLIBS}")
                api("io.arrow-kt:arrow-core:${Versions.ARROW}")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.google.guava:guava:${Versions.GUAVA}")
                implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:${Versions.KTOR}")
            }
        }
    }
}