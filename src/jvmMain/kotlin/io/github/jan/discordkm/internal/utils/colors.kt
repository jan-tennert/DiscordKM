package io.github.jan.discordkm.internal.utils

import io.github.jan.discordkm.api.entities.misc.Color

fun Color.toJvmColor() = java.awt.Color(extract().first, extract().second, extract().third)

fun java.awt.Color.toColor() = Color(rgb)

fun Color.Companion.fromJvmColor(color: java.awt.Color) = Color(color.rgb)
