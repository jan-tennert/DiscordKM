package io.github.jan.discordkm.entities

import com.soywiz.klock.DateTimeTz
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SnowflakeSerializer::class)
data class Snowflake internal constructor(val long: Long) {

    val string: String
        get() = long.toString()
    val timestamp: DateTimeTz
        get() = DateTimeTz.fromUnixLocal((long shr 22) + 1420070400000)

    companion object {

        fun fromId(id: Long) = Snowflake(id)
        fun fromId(id: String) = Snowflake(id.toLong())
        fun empty() = Snowflake(0L)

    }

    override fun toString() = string

}

object SnowflakeSerializer : KSerializer<Snowflake> {
    override fun deserialize(decoder: Decoder) = Snowflake.fromId(decoder.decodeString())

    override val descriptor = PrimitiveSerialDescriptor("Snowflake", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Snowflake) {
        encoder.encodeString(value.string)
    }

}

interface SnowflakeEntity {
    
    val id: Snowflake
    val creationDate: DateTimeTz
        get() = id.timestamp
    
}

val Long.asSnowflake: Snowflake
    get() = Snowflake.fromId(this)

val String.asSnowflake: Snowflake
    get() = Snowflake.fromId(this)