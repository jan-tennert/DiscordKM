package io.github.jan.discordkm.websocket

enum class Encoding {
    ETF, JSON
}

enum class Compression(val key: String) {
    NONE(""), ZLIB("zlib-stream")
}