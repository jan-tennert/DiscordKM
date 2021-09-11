package io.github.jan.discordkm.clients

import com.soywiz.klogger.Logger

class RestOnlyClient @Deprecated("Use the method buildRestOnlyClient") constructor(token: String, loggingLevel: Logger.Level) : Client(token, loggingLevel)

inline fun buildRestOnlyClient(token: String, loggingLevel: Logger.Level) =  RestOnlyClient(token, loggingLevel)
