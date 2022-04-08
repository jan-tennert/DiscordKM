/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.utils

import com.google.common.reflect.ClassPath
import com.soywiz.korio.async.invokeSuspend
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.events.EventListener
import java.lang.reflect.Modifier

@Target(AnnotationTarget.FUNCTION)
annotation class EventListener

fun WSDiscordClient.importListeners(packageName: String, subPackages: Boolean = false, args: Map<String, Any> = emptyMap()) {
    ClassPath.from(ClassLoader.getSystemClassLoader()).allClasses.filter { it.packageName == packageName || (subPackages && it.packageName.startsWith(packageName)) }.map(ClassPath.ClassInfo::load).forEach {
        it.methods.filter { m -> Modifier.isStatic(m.modifiers) && m.isAnnotationPresent(io.github.jan.discordkm.internal.utils.EventListener::class.java) && m.parameterCount >= 2 }.forEach { method ->
            val event = method.parameterTypes[0]
            val arguments = method.extractArguments(args)
            eventListeners += EventListener { e ->
                if(event.isInstance(e)) {
                    method.invokeSuspend(null, listOf(e).let { mArgs ->
                        if (arguments != null) {
                            (mArgs + arguments).flatten()
                        } else {
                            mArgs
                        }
                    })
                }
            }
        }
    }
}