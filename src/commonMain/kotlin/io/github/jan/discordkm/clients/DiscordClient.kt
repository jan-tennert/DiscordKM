package io.github.jan.discordkm.clients

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.entities.misc.EnumList
import io.github.jan.discordkm.events.Event
import io.github.jan.discordkm.events.EventListener
import io.github.jan.discordkm.websocket.Compression
import io.github.jan.discordkm.websocket.DiscordGateway
import io.github.jan.discordkm.websocket.Encoding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName

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

class DiscordClientBuilder @Deprecated("Use the method buildClient") constructor(var token: String) {

    var encoding = Encoding.JSON
    var compression = Compression.NONE
    var intents = mutableListOf<Intent>()
    var loggingLevel = Logger.Level.DEBUG

    @PublishedApi
    internal fun build() = DiscordClient(token, encoding, compression, EnumList(Intent, intents), loggingLevel)

}

inline fun buildClient(token: String, builder: DiscordClientBuilder.() -> Unit = {}) = DiscordClientBuilder(token).apply(builder).build()
