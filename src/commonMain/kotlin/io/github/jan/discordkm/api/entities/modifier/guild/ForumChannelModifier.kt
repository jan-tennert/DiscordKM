package io.github.jan.discordkm.api.entities.modifier.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.internal.utils.modify
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject

class ForumChannelModifier : ParentalModifier() {

    var topic: String? = null
    var threadCreationTimeout: TimeSpan? = null

    override val data: JsonObject
        get() = super.data.modify {
            putOptional("topic", topic)
            putOptional("rate_limit_per_user", threadCreationTimeout?.seconds)
        }

}