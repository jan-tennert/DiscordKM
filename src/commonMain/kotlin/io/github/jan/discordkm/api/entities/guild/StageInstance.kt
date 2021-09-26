package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.channels.StageChannel
import io.github.jan.discordkm.api.entities.lists.getGuildChannel
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class StageInstance(override val guild: Guild, override val data: JsonObject) : GuildEntity, Reference<StageInstance>, SnowflakeEntity {

    override val id = data.getId()
    val guildId = data.getOrThrow<Snowflake>("guild_id")

    /**
     * The stage channel where the stage instance is in
     */
    val stageChannelId = data.getOrThrow<Snowflake>("channel_id")
    val stageChannel
        get() = guild.channels.getGuildChannel<StageChannel>(stageChannelId)

    /**
     * The topic of the stage instance
     */
    val topic = data.getOrThrow<String>("topic")

    /**
     * The [PrivacyLevel] of the stage instance
     */
    val privacyLevel = valueOfIndex<PrivacyLevel>(data.getOrThrow("privacy_level"), 1)

    /**
     * Whether stage discovery is enabled
     */
    @get:JvmName("isDiscovery")
    val isDiscovery = data.getOrThrow<Boolean>("discoverable_disabled")

    override fun getValue(ref: Any?, property: KProperty<*>): StageInstance {
        throw UnsupportedOperationException("Stage instances aren't cached currently")
    }

    /**
     * Deletes this stage instance
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.StageInstance.DELETE_INSTANCE(stageChannelId).delete()
        transform {  }
    }

    /**
     * Modifies this stage instance. All fields are optional
     */
    suspend fun modify(topic: String? = null, privacyLevel: PrivacyLevel? = null) = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.MODIFY_INSTANCE(stageChannelId).patch(buildJsonObject {
            putOptional("topic", topic)
            putOptional("privacy_level", privacyLevel?.ordinal)
        })
        transform { StageInstance(guild, it.toJsonObject()) }
    }

    override suspend fun retrieve() = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.GET_INSTANCE(stageChannelId).get()
        transform { StageInstance(guild, it.toJsonObject()) }
    }

    enum class PrivacyLevel {
        PUBLIC,
        GUILD_ONLY
    }

}