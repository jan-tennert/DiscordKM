/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.utils

import java.lang.reflect.Method

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Inject(val name: String)

fun Method.extractArguments(args: Map<String, Any>): MutableList<Any>? {
    return if(parameterCount > 2) {
        val list = mutableListOf<Any>()
        parameters.toList().subList(1, parameterCount - 1).forEach { parameter ->
            if(parameter.isAnnotationPresent(Inject::class.java)) {
                val name = parameter.getAnnotation(Inject::class.java).name
                if(name in args) {
                    list.add(args[name]!!)
                }
            }
        }
        list.ifEmpty { null }
    } else {
        null
    }
}