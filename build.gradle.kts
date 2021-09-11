plugins {
    kotlin("multiplatform") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("maven-publish")
    signing
    id("org.jetbrains.dokka") version "1.4.20"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
}

group = "io.github.jan.discordkm"
version = "0.1-ALPHA"

repositories {
    mavenCentral()
}

val javadocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc JAR"
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaHtml"))
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri(Meta.release))
            snapshotRepositoryUrl.set(uri(Meta.snapshot))
            val ossrhUsername = providers
                .environmentVariable("OSSRH_USERNAME")
                .forUseAtConfigurationTime()
            val ossrhPassword = providers
                .environmentVariable("OSSRH_PASSWORD")
                .forUseAtConfigurationTime()
            if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
                username.set(ossrhUsername.get())
                password.set(ossrhPassword.get())
            }
        }
    }
}

object Meta {
    const val desc = "A Kotlin Multiplatform Discord API Wrapper "
    const val license = "GNU General Public License v3.0"
    const val githubRepo = "jan-tennert/DiscordKM"
    const val release = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set(project.name)
                description.set(Meta.desc)
                url.set("https://github.com/${Meta.githubRepo}")
                licenses {
                    license {
                        name.set(Meta.license)
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("TheRealJanGER")
                        name.set("Jan Tennert")
                        organization.set("Jan Tennert")
                        organizationUrl.set("https://developers.redhat.com/")
                    }
                }
                scm {
                    url.set(
                        "https://github.com/jan-tennert"
                    )
                    connection.set(
                        "scm:git:git://github.com/${Meta.githubRepo}.git"
                    )
                    developerConnection.set(
                        "scm:git:git://github.com/${Meta.githubRepo}.git"
                    )
                }
                issueManagement {
                    url.set("https://github.com/${Meta.githubRepo}/issues")
                }
            }
        }
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

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(LEGACY) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        nodejs()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
                implementation("com.soywiz.korlibs.klock:klock:2.2.2")
                implementation("com.soywiz.korlibs.korio:korio:2.2.1")
                implementation("com.soywiz.korlibs.klogger:klogger:2.2.0")
                implementation("io.ktor:ktor-client-core:1.6.3")
                implementation("com.github.ajalt.mordant:mordant:2.0.0-beta2")
                implementation("co.touchlab:stately-iso-collections:1.1.4-a1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:1.6.3")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:1.6.3")
            }
        }
        val jsTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:1.6.3")
            }
        }
        val nativeTest by getting
    }
}
