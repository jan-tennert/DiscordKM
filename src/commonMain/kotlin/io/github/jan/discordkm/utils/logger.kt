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