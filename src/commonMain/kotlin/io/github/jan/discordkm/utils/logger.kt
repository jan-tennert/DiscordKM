/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.utils

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.soywiz.klogger.Logger

object LoggerOutput : Logger.Output {

    private val terminal = Terminal()

    override fun output(logger: Logger, level: Logger.Level, msg: Any?) {
        val message = when(level) {
            Logger.Level.NONE -> msg.toString()
            Logger.Level.FATAL -> TextColors.brightRed(msg.toString())
            Logger.Level.ERROR -> TextColors.red(msg.toString())
            Logger.Level.WARN -> TextColors.yellow(msg.toString())
            Logger.Level.INFO -> msg.toString()
            Logger.Level.DEBUG -> msg.toString()
            Logger.Level.TRACE -> msg.toString()
        }
        terminal.println("${TextColors.brightBlue("[${level.name}]")} ${TextColors.brightGreen("(${logger.name})")} $message")
    }
}