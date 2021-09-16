package io.github.jan.discordkm.entities.guild

import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class StageInstance(override val guild: Guild, override val data: JsonObject) : GuildEntity, Reference<StageInstance>, SnowflakeEntity {

    override val id = data.getId()
    val guildId = data.getOrThrow<Snowflake>("guild_id")

    /**
     * The stage channel where the stage instance is in
     */
    val stageChannelId = data.getOrThrow<Snowflake>("channel_id")

    /**
     * The topic of the stage instance
     */
    val topic = data.getOrThrow<String>("topic")

    /**
     * The [PrivacyLevel] of the stage instance
     */
    val privacyLevel = PrivacyLevel.values().first { (it.ordinal + 1 == data.getOrThrow<Int>("privacy_level")) }

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
        action = RestAction.Action.delete("/stage-instances/$stageChannelId")
        transform {  }
    }

    /**
     * Modifies this stage instance. All fields are optional
     */
    suspend fun modify(topic: String? = null, privacyLevel: PrivacyLevel? = null) = client.buildRestAction<StageInstance> {
        action = RestAction.Action.patch("/stage-instances/$stageChannelId")
        transform { StageInstance(guild, it.toJsonObject()) }
    }

    override suspend fun retrieve() = client.buildRestAction<StageInstance> {
        action = RestAction.Action.get("/stage-instances/$stageChannelId")
        transform { StageInstance(guild, it.toJsonObject()) }
    }

    enum class PrivacyLevel {
        PUBLIC,
        GUILD_ONLY
    }

}