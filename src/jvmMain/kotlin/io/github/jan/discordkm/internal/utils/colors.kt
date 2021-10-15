package io.github.jan.discordkm.internal.utils

import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.model.RGBInt

fun Color.toJvmColor() = java.awt.Color(this.toSRGB().r, this.toSRGB().g, this.toSRGB().b)

fun java.awt.Color.toColormathColor() = RGBInt.fromRGBA(rgb.toUInt()) as Color