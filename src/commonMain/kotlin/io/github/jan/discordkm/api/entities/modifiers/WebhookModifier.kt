package io.github.jan.discordkm.api.entities.modifiers

import io.github.jan.discordkm.api.entities.Modifier
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class WebhookModifier : BaseModifier {

    var name: String? = null
    var avatar: Image? = null
    var channelId: Snowflake? = null

    override val data: JsonObject
        get() = buildJsonObject {
        putOptional("name", name)
        putOptional("avatar", avatar?.encodedData)
        putOptional("channel_id", channelId?.string)
    }

}