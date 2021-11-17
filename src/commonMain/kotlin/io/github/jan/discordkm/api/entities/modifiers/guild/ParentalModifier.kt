package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.utils.modify
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject

sealed class ParentalModifier : GuildChannelModifier() {

    var parentId: Snowflake? = null

    override val data: JsonObject
        get() = super.data.modify {
            putOptional("parent_id", parentId)
        }

}