package com.github.jan.discordkm

import com.soywiz.klogger.Logger
import com.github.jan.discordkm.entities.EnumSerializer
import com.github.jan.discordkm.entities.SerializableEnum
import com.github.jan.discordkm.entities.User
import com.github.jan.discordkm.entities.guild.Guild
import com.github.jan.discordkm.entities.misc.EnumList
import com.github.jan.discordkm.entities.misc.GuildList
import com.github.jan.discordkm.entities.misc.Image
import com.github.jan.discordkm.entities.misc.UserList
import com.github.jan.discordkm.events.Event
import com.github.jan.discordkm.events.EventListener
import com.github.jan.discordkm.restaction.RestAction
import com.github.jan.discordkm.restaction.RestClient
import com.github.jan.discordkm.restaction.buildRestAction
import com.github.jan.discordkm.utils.extractClientEntity
import com.github.jan.discordkm.utils.toJsonObject
import com.github.jan.discordkm.websocket.Compression
import com.github.jan.discordkm.websocket.DiscordGateway
import com.github.jan.discordkm.websocket.Encoding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmName

sealed class Client(val token: String, val loggingLevel: Logger.Level) : CoroutineScope {

    internal var userCache = com.github.jan.discordkm.Cache<User>()
    internal var guildCache = com.github.jan.discordkm.Cache<Guild>()
    val rest = RestClient(this)
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    lateinit var selfUser: User
        internal set
    val users: UserList
        get() = UserList(this, userCache.values.toList())
    val guilds: GuildList
        get() = GuildList(this, guildCache.values.toList())

    suspend fun editSelfUser(builder: suspend SelfUserEdit.() -> Unit) = buildRestAction<User> {
        val edit = SelfUserEdit()
        edit.builder()
        action = RestAction.Action.patch("/users/@me", buildJsonObject {
            edit.username?.let { put("username", it) }
            edit.image?.let { put("image", it.url) }
        })
        transform { it.toJsonObject().extractClientEntity(this@Client) }
        onFinish { selfUser = it }
    }

    data class SelfUserEdit(var username: String? = null, var image: Image? = null)

    enum class Intent(override val offset: Int) : SerializableEnum<Intent> {

        GUILDS(0),
        GUILD_MEMBERS(1),
        GUILD_BANS(2),
        GUILD_EMOJIS_AND_STICKERS(3),
        GUILD_INTEGRATIONS(4),
        GUILD_WEBHOOKS(5),
        GUILD_INVITES(6),
        GUILD_VOICE_STATES(7),
        GUILD_PRESENCES(8),
        GUILD_MESSAGES(9),
        GUILD_MESSAGE_REACTIONS(10),
        GUILD_MESSAGE_TYPING(11),
        DIRECT_MESSAGES(12),
        DIRECT_MESSAGE_REACTIONS(13),
        DIRECT_MESSAGE_TYPING(14);

        companion object : EnumSerializer<Intent> {
            override val values = values().toList()
        }

    }
}

class DiscordClient(token: String, encoding: Encoding, compression: Compression, val intents: EnumList<Intent>, loggingLevel: Logger.Level) : Client(token, loggingLevel) {

    internal val gateway = DiscordGateway(encoding, compression, this)
    @get:JvmName("isClosed")
    var isDisconnected = false
        private set
    var loggedIn = false
        private set
    val eventListeners = mutableListOf<EventListener>()

    inline fun <reified E : Event> on(crossinline predicate: (E) -> Boolean = { true }, crossinline onEvent: suspend E.() -> Unit) {
        eventListeners += EventListener {
            if(it is E && predicate(it)) {
                onEvent(it)
            }
        }
    }

    suspend fun login() {
        if(loggedIn) throw UnsupportedOperationException("Discord Client already connected to the discord gateway")
        gateway.start()
        loggedIn = true
    }

    fun disconnect() {
        if(isDisconnected) throw UnsupportedOperationException("Discord Client is already disconnected from the discord gateway")
        gateway.close()
        isDisconnected = true
    }

    internal suspend fun handleEvent(event: Event) = coroutineScope { eventListeners.forEach { launch { it(event) } } }

}

class RestOnlyClient @Deprecated("Use the method buildRestOnlyClient") constructor(token: String, loggingLevel: Logger.Level) : Client(token, loggingLevel)

//make these better

class DiscordClientBuilder @Deprecated("Use the method buildClient") constructor(var token: String) {

    var encoding = Encoding.JSON
    var compression = Compression.NONE
    var intents = mutableListOf<Client.Intent>()
    var loggingLevel = Logger.Level.DEBUG

    @PublishedApi
    internal fun build() = DiscordClient(token, encoding, compression, EnumList(Client.Intent, intents), loggingLevel)

}

inline fun buildRestOnlyClient(token: String, loggingLevel: Logger.Level) =  RestOnlyClient(token, loggingLevel)

inline fun buildClient(token: String, builder: DiscordClientBuilder.() -> Unit = {}) = DiscordClientBuilder(token).apply(builder).build()

