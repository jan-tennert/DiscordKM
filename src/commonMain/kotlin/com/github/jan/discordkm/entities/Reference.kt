package com.github.jan.discordkm.entities

import kotlin.reflect.KProperty

interface Reference <T> {

    operator fun getValue(ref: Any?, property: KProperty<*>) : T

}