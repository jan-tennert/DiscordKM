package io.github.jan.discordkm.internal.utils

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.webhooks.Webhook
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction

/**
 * Retrieves a webhook from its **id**
 */
suspend fun Client.retrieveWebhook(id: Snowflake) = buildRestAction<Webhook> {
    route = Route.Webhook.GET_WEBHOOK(id).get()
    transform { Webhook(this@retrieveWebhook, it.toJsonObject()) }
}

/**
 * Retrieves a webhook from its **id** and **token**
 */
suspend fun Client.retrieveWebhook(id: Snowflake, token: String) = buildRestAction<Webhook> {
    route = Route.Webhook.GET_WEBHOOK_WITH_TOKEN(id, token).get()
    transform { Webhook(this@retrieveWebhook, it.toJsonObject()) }
}

/**
 * Retrieves a webhook from a url
 */
suspend fun Client.retrieveWebhook(url: String) = Webhook.WEBHOOK_PATTERN.matchEntire(url)?.let {
    retrieveWebhook(Snowflake.fromId(it.groups[1]!!.value), it.groups[2]!!.value)
} ?: throw IllegalArgumentException("Invalid webhook url: $url")

/**
 * Retrieves all rtc regions
 */
suspend fun Client.retrieveRTCRegions() = buildRestAction<String> {
    route = Route.Voice.GET_VOICE_REGIONS.get()
    transform { it }
}

/**
 * Retrieves an invite from its code
 */
suspend fun Client.retrieveInvite(code: String) = client.buildRestAction<Invite> {
    route = Route.Invite.GET_INVITE(code).get()
    transform { Invite(client, it.toJsonObject()) }
}