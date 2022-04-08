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
import io.github.jan.discordkm.api.events.CommandEvent
import io.github.jan.discordkm.api.events.EventListener
import io.github.jan.discordkm.api.events.SlashCommandEvent
import java.lang.reflect.Modifier

@Target(AnnotationTarget.FUNCTION)
annotation class CommandExecutor(
    val name: String,
    val subCommand: String = "",
    val subCommandGroup: String = "",
)

fun WSDiscordClient.importCommands(packageName: String, subPackages: Boolean = false, args: Map<String, Any> = emptyMap()) {
    ClassPath.from(ClassLoader.getSystemClassLoader()).allClasses.filter { it.packageName == packageName || (subPackages && it.packageName.startsWith(packageName)) }.map(
        ClassPath.ClassInfo::load).forEach {
        it.methods.filter { m -> Modifier.isStatic(m.modifiers) && m.isAnnotationPresent(CommandExecutor::class.java) && m.parameterCount >= 2 }.forEach { method ->
            val annotation = method.getAnnotation(CommandExecutor::class.java)
            val arguments = method.extractArguments(args)
            eventListeners += EventListener { e ->
                if(e is CommandEvent) {
                    if(e.commandName != annotation.name) return@EventListener
                    if(e is SlashCommandEvent) {
                        if (annotation.subCommand.isNotBlank() && e.subCommand != annotation.subCommand) return@EventListener
                        if (annotation.subCommandGroup.isNotBlank() && e.subCommandGroup != annotation.subCommandGroup) return@EventListener
                    }
                    method.invokeSuspend(null, listOf(e).let { mArgs ->
                        if(arguments != null) {
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