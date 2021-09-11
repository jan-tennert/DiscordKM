package io.github.jan.discordkm.utils

import io.github.jan.discordkm.entities.misc.Color

val Color.asJvmColor: java.awt.Color
    get() = java.awt.Color(value)

val java.awt.Color.asDiscordColor: Color
    get() = Color(rgb)