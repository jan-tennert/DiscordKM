package io.github.jan.discordkm.entities.misc

import kotlin.reflect.KProperty

class LimitedInt(val min: Int = Int.MIN_VALUE, val max: Int = Int.MAX_VALUE) {

    var value: Int = 0

    operator fun getValue(ref: Any?, property: KProperty<*>) = value

    operator fun setValue(ref: Any?, property: KProperty<*>, value: Int) {
        if(value < min || value > max) throw IllegalArgumentException("${property.name} has to be between $min and $max")
        this.value = value
    }

}