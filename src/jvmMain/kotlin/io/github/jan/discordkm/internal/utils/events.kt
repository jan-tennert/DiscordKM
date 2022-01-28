package io.github.jan.discordkm.internal.utils

import com.google.common.reflect.ClassPath
import com.soywiz.korio.async.invokeSuspend
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.EventListener
import java.lang.reflect.Modifier

@Target(AnnotationTarget.FUNCTION)
annotation class EventListener

fun DiscordWebSocketClient.importListeners(packageName: String, subPackages: Boolean = false) {
    ClassPath.from(ClassLoader.getSystemClassLoader()).allClasses.filter { it.packageName == packageName || (subPackages && it.packageName.startsWith(packageName)) }.map(ClassPath.ClassInfo::load).forEach {
        it.methods.filter { m -> Modifier.isStatic(m.modifiers) && m.isAnnotationPresent(io.github.jan.discordkm.internal.utils.EventListener::class.java) && m.parameterCount == 2 }.forEach { method ->
            val event = method.parameterTypes[0]
            eventListeners += EventListener { e ->
                if(event.isInstance(e)) {
                    method.invokeSuspend(null, listOf(e))
                }
            }
        }
    }
}