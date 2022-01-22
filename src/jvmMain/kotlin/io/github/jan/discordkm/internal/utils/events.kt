package io.github.jan.discordkm.internal.utils

import com.google.common.reflect.ClassPath
import com.soywiz.korio.async.invokeSuspend
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.EventListener
import java.lang.reflect.Modifier

annotation class EventListener

fun DiscordWebSocketClient.loadListenersFromPackage(packageName: String) {
    ClassPath.from(ClassLoader.getSystemClassLoader()).allClasses.filter { it.packageName == packageName }.map(ClassPath.ClassInfo::load).forEach {
        it.methods.filter { m -> Modifier.isStatic(m.modifiers) && m.annotations.contains(EventListener()) && m.parameterCount == 2 }.forEach { method ->
            val event = method.parameterTypes[0]
            eventListeners += EventListener { e ->
                if(event.isInstance(e)) {
                    method.invokeSuspend(null, listOf(e))
                }
            }
        }
    }
}