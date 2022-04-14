package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.message.DataMessage
import io.github.jan.discordkm.api.entities.message.MessageBuilder
import io.github.jan.discordkm.api.entities.message.buildMessage
import io.github.jan.discordkm.internal.DiscordKMUnstable
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.put

@OptIn(DiscordKMUnstable::class)
sealed interface ForumChannelCacheEntry : ForumChannel, GuildChannelCacheEntry, ParentChannel, IPositionable {

    /**
     * The topic of this forum channel
     */
    val topic: String

    /**
     * The last thread created in this forum channel
     */
    val lastThread: Thread?

    /**
     * The timeout you have to wait before creating a new thread in this forum channel
     */
    val threadCreationTimeout: TimeSpan

}

class ForumChannelCacheEntryImpl(
    override val id: Snowflake,
    override val guild: Guild,
    override val parent: Category?,
    override val name: String,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val position: Int,
    override val topic: String,
    override val threadCreationTimeout: TimeSpan
) : ForumChannelCacheEntry {

    override val lastThread: Thread?
        get() = (client as? WSDiscordClientImpl)?.lastThreads?.get(id)

    /**
     * Creates a new thread in this forum channel
     */
    override suspend fun createThread(builder: ForumChannelThreadBuilder.() -> Unit) = client.buildRestAction<ThreadCacheEntry> {
        route = Route.Thread.START_THREAD(id).post(ForumChannelThreadBuilder(client).apply(builder).build())
        transform { Thread(it.toJsonObject(), guild) }
    }

    /**
     * Creates a new thread in this forum channel
     * @param name the name of the thread
     * @param autoArchiveDuration the duration after the thread will be achieved
     * @param slowModeTime the timeout for sending messages
     * @param message The first message to be sent in the thread
     */
    override suspend fun createThread(name: String, autoArchiveDuration: Thread.ThreadDuration?, slowModeTime: TimeSpan?, message: MessageBuilder.() -> Unit) = createThread {
        this.name = name
        this.autoArchiveDuration = autoArchiveDuration
        this.slowModeTime = slowModeTime
        message(message)
    }

}

class ForumChannelThreadBuilder internal constructor(private val client: DiscordClient) {

    lateinit var message: DataMessage

    var name = ""
    var autoArchiveDuration: Thread.ThreadDuration? = null
    var slowModeTime: TimeSpan? = null

    fun message(builder: MessageBuilder.() -> Unit) {
        message = buildMessage(client, builder)
    }

    fun build() = message.build {
        put("name", name)
        putOptional("auto_archive_duration", autoArchiveDuration?.duration?.seconds)
        putOptional("rate_limit_per_user", slowModeTime?.seconds)
    }

}