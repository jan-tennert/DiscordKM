package io.github.jan.discordkm.http

import io.github.jan.discordkm.api.entities.clients.ClientConfig
import io.github.jan.discordkm.internal.utils.LoggerConfig

class HttpConfig(
    token: String,
    logging: LoggerConfig = LoggerConfig(),
    val port: Int = 20000,
    val host: String = "0.0.0.0",
    val route: String = "/interactions"
) : ClientConfig(token, logging = logging)
