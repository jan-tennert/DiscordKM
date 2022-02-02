/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
object Versions {

    const val KOTLIN = "1.6.10"
    const val DOKKA = "1.6.10"
    const val KTOR = "1.6.7"
    const val SERIALIZATION = "1.3.2"
    const val KORLIBS = "2.4.10"
    const val COROUTINES = "1.6.0"
    const val STATELY = "1.2.1"
    const val DISCORDKM = "0.7-alpha"
    const val ARROW = "1.0.1"
    const val NEXUS_STAGING = "0.30.0"

}

object Publishing {

    val REPOSITORY_ID = System.getenv("SONATYPE_REPOSITORY_ID")
    val SONATYPE_USERNAME = System.getenv("OSSRH_USERNAME")
    val SONATYPE_PASSWORD = System.getenv("OSSRH_PASSWORD")

}
