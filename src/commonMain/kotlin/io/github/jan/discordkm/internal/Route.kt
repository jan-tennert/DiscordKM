package io.github.jan.discordkm.internal

import com.soywiz.korio.lang.format
import io.github.jan.discordkm.internal.restaction.QueryParameterBuilder
import io.github.jan.discordkm.internal.restaction.RestAction

object Route {

    object Emoji {

        const val GET_GUILD_EMOJIS = "/guilds/%s/emojis"
        const val GET_GUILD_EMOJI = "/guilds/%s/emojis/%s"
        const val CREATE_EMOJI = "/guilds/%s/emojis"
        const val MODIFY_EMOJI = "/guilds/%s/emojis/%s"
        const val DELETE_EMOJI = "/guilds/%s/emojis/%s"

    }

    object Webhook {

        const val EXECUTE_WEBHOOK = "/webhooks/%s/%s"
        const val DELETE_WITH_TOKEN = "/webhooks/%s/%s"
        const val DELETE = "/webhooks/%s"
        const val MODIFY = "/webhooks/%s"
        const val MODIFY_WITH_TOKEN = "/webhooks/%s/%s"
        const val CREATE_WEBHOOK = "/channels/%s/webhooks"
        const val GET_CHANNEL_WEBHOOKS = "/channels/%s/webhooks"
        const val GET_WEBHOOK = "/webhooks/%s"
        const val GET_WEBHOOK_WITH_TOKEN = "/webhooks/%s/%s"

    }

    object Template {

        const val DELETE_GUILD_TEMPLATE = "/guilds/%s/templates/%s"
        const val MODIFY_GUILD_TEMPLATE = "/guilds/%s/templates/%s"
        const val SYNC_GUILD_TEMPLATE = "/guilds/%s/templates/%s"
        const val CREATE_GUILD_TEMPLATE = "/guilds/%s/templates"
        const val GET_GUILD_TEMPLATES = "/guilds/%s/templates"
        const val CREATE_GUILD_FROM_TEMPLATE = "/guilds/templates/%s"
        const val GET_GUILD_TEMPLATE = "/guilds/templates/%s"

    }

    object User {

        const val GET_USER = "/users/%s"
        const val MODIFY_SELF_USER = "/users/@me"
        const val GET_GUILDS = "/users/@me/guilds"
        const val LEAVE_GUILD = "/users/@me/guilds/%s"
        const val CREATE_DM = "/users/@me/channels"

    }

    object StageInstance {

        const val CREATE_INSTANCE = "/stage-instances"
        const val GET_INSTANCE = "/stage-instances/%s"
        const val MODIFY_INSTANCE = "/stage-instances/%s"
        const val DELETE_INSTANCE = "/stage-instances/%s"

    }

    object Invite {

        const val GET_INVITE = "/invites/%s"
        const val DELETE_INVITE = "/invites/%s"
        const val GET_GUILD_INVITES = "/guilds/%s/invites"
        const val GET_CHANNEL_INVITES = "/channels/%s/invites"
        const val CREATE_CHANNEL_INVITE = "/channels/%s/invites"

    }

    object Channel {

        const val GET_CHANNELS = "/guilds/%s/channels"
        const val CREATE_CHANNEL = "/guilds/%s/channels"
        const val MODIFY_CHANNEL_POSITION = "/guilds/%s/channels"
        const val GET_CHANNEL = "/channels/%s"
        const val MODIFY_CHANNEL = "/channels/%s"
        const val DELETE_CHANNEL = "/channels/%s"
        const val EDIT_PERMISSION = "/channels/%s/permissions/%s"
        const val FOLLOW_CHANNEL = "/channels/%s/followers"
        const val START_TYPING = "/channels/%s/typing"

    }

    object Message {

        const val GET_MESSAGES = "/channels/%s/messages"
        const val GET_MESSAGE = "/channels/%s/messages/%s"
        const val CREATE_MESSAGE = "/channels/%s/messages"
        const val CROSSPOST_MESSAGE = "/channels/%s/messages/%s/crosspost"
        const val CREATE_REACTION = "/channels/%s/messages/%s/reactions/%s/@me"
        const val DELETE_OWN_REACTION = "/channels/%s/messages/%s/reactions/%s/@me"
        const val DELETE_USER_REACTION = "/channels/%s/messages/%s/reactions/%s/%s"
        const val GET_REACTIONS = "/channels/%s/messages/%s/reactions/%s"
        const val DELETE_REACTIONS = "/channels/%s/messages/%s/reactions"
        const val DELETE_REACTION_EMOJI = "/channels/%s/messages/%s/reactions/%s"
        const val EDIT_MESSAGE = "/channels/%s/messages/%s"
        const val DELETE_MESSAGE = "/channels/%s/messages/%s"
        const val BULK_DELETE = "/channels/%s/messages/bulk-delete"
        const val GET_PINNED_MESSAGES = "/channels/%s/pins"
        const val PIN_MESSAGE = "/channels/%s/pins/%s"
        const val UNPIN_MESSAGE = "/channels/%s/pins/%s"

    }

    object Thread {

        const val GET_ACTIVE_THREADS = "/guilds/%s/threads/active"
        const val START_THREAD_WITH_MESSAGE = "/channels/%s/messages/%s/threads"
        const val START_THREAD = "/channels/%s/threads"
        const val JOIN_THREAD = "/channels/%s/thread-members/@me"
        const val ADD_THREAD_MEMBER = "/channels/%s/thread-members/%s"
        const val LEAVE_THREAD = "/channels/%s/thread-members/@me"
        const val REMOVE_THREAD_MEMBER = "/channels/%s/thread-members/%s"
        const val GET_THREAD_MEMBERS = "/channels/%s/thread-members"
        const val GET_ACTIVE_CHANNEL_THREADS = "/channels/%s/threads/active"
        const val GET_PUBLIC_ARCHIVED_THREADS = "/channels/{channel.id}/threads/archived/public"
        const val GET_PRIVATE_ARCHIVED_THREADS = "/channels/%s/threads/archived/private"
        const val GET_JOINED_PRIVATE_ARCHIVED_THREADS = "/channels/%s/users/@me/threads/archived/private"

    }

    object Member {

        const val GET_MEMBER = "/guilds/%s/members/%s"
        const val GET_MEMBERS = "/guilds/%s/members"
        const val SEARCH_MEMBERS = "/guilds/%s/members/search"
        const val MODIFY_MEMBER = "/guilds/%s/members/%s"
        const val MODIFY_SELF_MEMBER_NICK = "/guilds/%s/members/@me/nick"
        const val ADD_ROLE_TO_MEMBER = "/guilds/%s/members/%s/roles/%s"
        const val REMOVE_ROLE_FROM_MEMBER = "/guilds/%s/members/%s/roles/%s"
        const val KICK_MEMBER = "/guilds/%s/members/%s"

    }

    object Ban {

        const val GET_BANS = "/guilds/%s/bans"
        const val GET_BAN = "/guilds/%s/bans/%s"
        const val CREATE_BAN = "/guilds/%s/bans/%s"
        const val REMOVE_BAN = "/guilds/%s/bans/%s"

    }

    object Sticker {

        const val GET_STICKER = "/stickers/%s"
        const val GET_STICKER_PACKS = "/sticker-packs"
        const val GET_GUILD_STICKERS = "/guilds/%s/stickers"
        const val GET_GUILD_STICKER = "/guilds/%s/stickers/%s"
        const val CREATE_GUILD_STICKER = "/guilds/%s/stickers"
        const val MODIFY_GUILD_STICKER = "/guilds/%s/stickers/%s"
        const val DELETE_GUILD_STICKER = "/guilds/%s/stickers/%s"

    }

    object Voice {

        const val MODIFY_VOICE_STATE = "/guilds/%s/voice-states/%s"
        const val GET_VOICE_REGIONS = "/voice/regions"

    }

    object Role {

        const val GET_ROLES = "/guilds/%s/roles"
        const val CREATE_ROLE = "/guilds/%s/roles"
        const val MODIFY_ROLE_POSITION = "/guilds/%s/roles"
        const val MODIFY_ROLE = "/guilds/%s/roles/%s"
        const val DELETE_ROLE = "/guilds/%s/roles/%s"

    }

    object Guild {

        const val GET_GUILD = "/guilds/%s"
        const val MODIFY_GUILD = "/guilds/%s"
        const val DELETE_GUILD = "/guilds/%s"
        const val GET_VOICE_REGIONS = "/guilds/%s/regions"
        const val GET_VANITY_URL = "/guilds/%s/vanity-url"
        const val GET_WELCOME_SCREEN = "/guilds/%s/welcome-screen"
        const val MODIFY_WELCOME_SCREEN = "/guilds/%s/welcome-screen"
        const val GET_AUDIT_LOGS = "/guilds/%s/audit-logs"

        //integrations
        //prune ?
        //widgets

    }

    object Interaction {

        const val CALLBACK = "/interactions/%s/%s/callback"
        const val EDIT_ORIGINAL = "/webhooks/%s/%s/messages/@original"
        const val GET_ORIGINAL = "/webhooks/%s/%s/messages/@original"
        const val DELETE_ORIGINAL = "DELETE /webhooks/%s/%s/messages/@original"
        const val CREATE_FOLLOW_UP = "/webhooks/%s/%s"
        const val EDIT_FOLLOW_UP = "/webhooks/%s/%s/messages/%s"
        const val GET_FOLLOW_UP = "/webhooks/%s/%s/messages/%s"
        const val DELETE_FOLLOW_UP = "/webhooks/%s/%s/messages/%s"

    }


}

operator fun String.invoke(vararg args: Any) = format(*(args.map { it.toString() }.toTypedArray()))

fun String.get(parameters: QueryParameterBuilder.() -> Unit = {}) = RestAction.get(this + QueryParameterBuilder().apply(parameters).build())
fun String.get() = RestAction.get(this)
fun String.post(body: Any? = null) = RestAction.post(this, body)
fun String.patch(body: Any? = null) = RestAction.patch(this, body)
fun String.put(body: Any? = null) = RestAction.put(this, body)
fun String.delete() = RestAction.delete(this)