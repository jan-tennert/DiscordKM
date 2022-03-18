/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.misc.TranslationManager
import io.github.jan.discordkm.api.events.CommandEvent
import io.github.jan.discordkm.internal.DiscordKMUnstable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class ApplicationCommandBuilder(val type: ApplicationCommandType, var name: String, var description: String, val client: DiscordWebSocketClient? = null) {

    var defaultMemberPermissions: MutableSet<Permission> = mutableSetOf()
    val translations = TranslationBuilder()
    internal var translationManager: TranslationManager? = null

    /**
     * Imports translations from the translation manager.
     *
     * The translation keys can be changed like this in the translation files:
     *
     * **application.command.[name].name**
     *
     * **application.command.[name].description**
     *
     * To change the options, use the following keys:
     *
     * **application.command.[name].options.*optionName*.name**
     *
     * **application.command.[name].options.*optionName*.description**
     *
     * And if you use sub commands or sub commands groups
     *
     * **application.command.[name].options.*subCommandName*.options.*optionName*.description**
     *
     * **application.command.[name].options.*subCommandGroupName*.options.*subCommandName*.options.*optionName*.description**
     */
    @DiscordKMUnstable
    open fun useTranslationManager(manager: TranslationManager) {
        translations {
            name.putAll(manager.getAll("application.command.${this@ApplicationCommandBuilder.name}.name"))
            description.putAll(manager.getAll("application.command.${this@ApplicationCommandBuilder.name}.description"))
        }
        translationManager = manager
    }

    @DiscordKMUnstable
    fun useDefaultTranslationManager() { client?.config?.translationManager?.let { useTranslationManager(it) } }

    @DiscordKMUnstable
    fun defaultPermissions(vararg permissions: Permission) { defaultMemberPermissions.addAll(permissions) }

    @CommandBuilder
    inline fun <reified E : CommandEvent> onCommand(
        crossinline action: suspend E.() -> Unit
    ) {
        client?.let { c -> c.on<E>(predicate = { it.commandName == name }) { action(this) } }
    }

    internal open fun build() = buildJsonObject {
        put("name", name)
        put("description", description)
        put("type", type.ordinal + 1)
        put("default_member_permissions", Permission.encode(defaultMemberPermissions))
        put("name_localizations", JsonObject(translations.name.map { it.key.value to JsonPrimitive(it.value) }.toMap()))
        put("description_localizations", JsonObject(translations.description.map { it.key.value to JsonPrimitive(it.value) }.toMap()))
    }

}

inline fun messageCommand(client: DiscordWebSocketClient? = null, builder: ApplicationCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ApplicationCommandBuilder(ApplicationCommandType.MESSAGE, "", "", client)
    commandBuilder.builder()
    return commandBuilder
}

inline fun userCommand(client: DiscordWebSocketClient? = null, builder: ApplicationCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ApplicationCommandBuilder(ApplicationCommandType.USER, "", "", client)
    commandBuilder.builder()
    return commandBuilder
}