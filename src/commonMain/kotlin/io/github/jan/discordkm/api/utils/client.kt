/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.utils

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.webhooks.Webhook
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonObject

/**
 * Retrieves a webhook from its [id]
 */
suspend fun Client.retrieveWebhook(id: Snowflake) = buildRestAction<Webhook> {
    route = Route.Webhook.GET_WEBHOOK(id).get()
    transform { Webhook(this@retrieveWebhook, it.toJsonObject()) }
}

/**
 * Retrieves a webhook from its [id] and [token]
 */
suspend fun Client.retrieveWebhook(id: Snowflake, token: String) = buildRestAction<Webhook> {
    route = Route.Webhook.GET_WEBHOOK_WITH_TOKEN(id, token).get()
    transform { Webhook(this@retrieveWebhook, it.toJsonObject()) }
}

/**
 * Retrieves a webhook from an [url]
 */
suspend fun Client.retrieveWebhook(url: String) = Webhook.WEBHOOK_PATTERN.matchEntire(url)?.let {
    retrieveWebhook(Snowflake(it.groups[1]!!.value), it.groups[2]!!.value)
} ?: throw IllegalArgumentException("Invalid webhook url: $url")

/**
 * Retrieves all rtc regions
 */
suspend fun Client.retrieveRTCRegions() = buildRestAction<String> {
    route = Route.Voice.GET_VOICE_REGIONS.get()
    transform { it }
}

/**
 * Retrieves an invite from its [code]
 */
suspend fun Client.retrieveInvite(code: String) = client.buildRestAction<Invite> {
    route = Route.Invite.GET_INVITE(code).get()
    transform { Invite(client, it.toJsonObject()) }
}