/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.utils

import com.soywiz.klogger.Logger

object LoggerOutput : Logger.Output {

    override fun output(logger: Logger, level: Logger.Level, msg: Any?) {
        val message = when(level) {
            Logger.Level.NONE -> msg.toString()
            Logger.Level.FATAL -> ConsoleColors.RED_BRIGHT + msg.toString() + ConsoleColors.RESET
            Logger.Level.ERROR -> ConsoleColors.RED + msg.toString() + ConsoleColors.RESET
            Logger.Level.WARN -> ConsoleColors.YELLOW + msg.toString() + ConsoleColors.RESET
            Logger.Level.INFO -> msg.toString()
            Logger.Level.DEBUG -> msg.toString()
            Logger.Level.TRACE -> msg.toString()
        }
        println("${ConsoleColors.BLUE_BRIGHT + ("[${level.name}]") + ConsoleColors.RESET} ${ConsoleColors.GREEN_BRIGHT + ("(${logger.name})") + ConsoleColors.RESET} $message")
    }
}

object ConsoleColors {

    const val RED = "\u001b[0;31m"
    const val RED_BRIGHT = "\u001b[0;91m"
    const val YELLOW = "\u001b[0;33m"
    const val BLUE_BRIGHT = "\u001b[0;94m"
    const val GREEN_BRIGHT = "\u001b[0;92m"
    const val RESET = "\u001b[0m"

}