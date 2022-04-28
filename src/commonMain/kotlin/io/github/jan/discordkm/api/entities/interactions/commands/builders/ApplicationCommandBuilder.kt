/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.on
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.misc.TranslationManager
import io.github.jan.discordkm.api.events.MessageCommandEvent
import io.github.jan.discordkm.api.events.UserCommandEvent
import io.github.jan.discordkm.internal.DiscordKMUnstable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class ApplicationCommandBuilder(val type: ApplicationCommandType, val client: WSDiscordClient? = null) {

    var defaultMemberPermissions: MutableSet<Permission> = mutableSetOf()
    var name: String = ""
    var description: String = ""
    var enabledInDirectMessages = true
    val translations = TranslationBuilder()
    private var disabledByDefault = false
    internal var translationManager: TranslationManager? = null

    /*
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
    open fun useTranslationManager(manager: TranslationManager) {
        translations {
            name.putAll(manager.getAll("application.command.${this@ApplicationCommandBuilder.name}.name"))
            description.putAll(manager.getAll("application.command.${this@ApplicationCommandBuilder.name}.description"))
        }
        translationManager = manager
    }

    fun useDefaultTranslationManager() { client?.config?.map<TranslationManager>("translationManager")?.let { useTranslationManager(it) } }

    fun defaultPermissions(vararg permissions: Permission) { defaultMemberPermissions.addAll(permissions) }

    fun disabledByDefault() {
        disabledByDefault = true
    }

    internal open fun build() = buildJsonObject {
        put("name", name)
        put("description", description)
        put("type", type.ordinal + 1)
        if(defaultMemberPermissions.isNotEmpty()) put("default_member_permissions", Permission.encode(defaultMemberPermissions))
        if(disabledByDefault) put("default_member_permissions", 0)
        put("name_localizations", JsonObject(translations.name.map { it.key.value to JsonPrimitive(it.value) }.toMap()))
        put("description_localizations", JsonObject(translations.description.map { it.key.value to JsonPrimitive(it.value) }.toMap()))
    }

}

class MessageCommandBuilder(client: WSDiscordClient? = null) : ApplicationCommandBuilder(ApplicationCommandType.MESSAGE, client) {

    @CommandBuilder
    inline fun onCommand(crossinline action: suspend MessageCommandEvent.() -> Unit) {
        client?.let { c -> c.on<MessageCommandEvent>(predicate = { it.commandName == name }) { action(this) } }
    }

}

class UserCommandBuilder(client: WSDiscordClient? = null) : ApplicationCommandBuilder(ApplicationCommandType.USER, client) {

    @CommandBuilder
    inline fun onCommand(crossinline action: suspend UserCommandEvent.() -> Unit) {
        client?.let { c -> c.on<UserCommandEvent>(predicate = { it.commandName == name }) { action(this) } }
    }

}

inline fun messageCommand(client: WSDiscordClient? = null, builder: MessageCommandBuilder.() -> Unit) : MessageCommandBuilder {
    val commandBuilder = MessageCommandBuilder(client)
    commandBuilder.builder()
    return commandBuilder
}

inline fun userCommand(client: WSDiscordClient? = null, builder: UserCommandBuilder.() -> Unit) : UserCommandBuilder {
    val commandBuilder = UserCommandBuilder(client)
    commandBuilder.builder()
    return commandBuilder
}