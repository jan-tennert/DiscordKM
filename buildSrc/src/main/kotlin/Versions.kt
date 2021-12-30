/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
object Versions {

    const val KOTLIN = "1.6.0"
    const val DOKKA = "1.6.0"
    const val KTOR = "2.0.0-beta-1"
    const val SERIALIZATION = "1.3.1"
    const val KORLIBS = "2.4.8"
    const val COROUTINES = "1.6.0-RC2"
    const val STATELY = "1.2.0"
    const val DISCORDKM = "0.6.12"

}

object Publishing {

    val REPOSITORY_ID = System.getenv("SONATYPE_REPOSITORY_ID")
    val SONATYPE_USERNAME = System.getenv("OSSRH_USERNAME")
    val SONATYPE_PASSWORD = System.getenv("OSSRH_PASSWORD")

}
