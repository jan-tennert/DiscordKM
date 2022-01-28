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