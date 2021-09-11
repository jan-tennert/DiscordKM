package io.github.jan.discordkm.entities.messages

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.entities.misc.Color
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MessageEmbed(val title: String?,
                        val type: Type = Type.UNKNOWN,
                        val description: String? = null,
                        val url: String? = null,
                        @Contextual val timestamp: DateTimeTz? = null,
                        val color: Color? = null,
                        val footer: Footer? = null,
                        val image: Media? = null,
                        val thumbnail: Media? = null,
                        val video: Media? = null,
                        val provider: Provider? = null,
                        val author: Author? = null,
                        val fields: List<Field> = emptyList()) {

    @Serializable
    data class Author(val name: String? = null, val url: String? = null, val iconUrl: String? = null, val proxyIconUrl: String? = null)

    @Serializable
    data class Provider(val url: String? = null, val name: String? = null)

    @Serializable
    data class Media(val url: String = "", val proxyUrl: String? = null, val height: Int = 0, val width: Int = 0)

    @Serializable
    data class Footer(val text: String = "", val iconUrl: String? = null, val proxyIconUrl: String? = null)

    @Serializable
    data class Field(val name: String = "", val value: String = "", val inline: Boolean = false)

    enum class Type {
        UNKNOWN,
        RICH,
        IMAGE,
        VIDEO,
        GIFV,
        ARTICLE,
        LINK
    }

    companion object {

        fun fromJson(data: JsonObject) {

        }

    }

}

class EmbedBuilder @Deprecated("Use buildEmbed instead") constructor() {

    var title: String? = null
    var description: String? = null
    var url: String? = null
    var timestamp: DateTimeTz? = null
    var color: Color? = null
    var footer: Footer? = null
    var image: Media? = null
    var thumbnail: Media? = null
    var author: Author? = null
    val fields: MutableList<Field> = mutableListOf()

    fun field(name: String = "", value: String = "", inline: Boolean = false, builder: Field.() -> Unit = {}) { fields += Field(name, value ?: "", inline).apply(builder) }
    fun footer(text: String = "", iconUrl: String? = null, builder: Footer.() -> Unit = {}) {
        footer = Footer(text, iconUrl).apply(builder)
    }
    fun thumbnail(url: String = "", builder: Media.() -> Unit) {
        thumbnail = Media(url).apply(builder)
    }
    fun image(url: String = "", builder: Media.() -> Unit = {}) {
        image = Media(url).apply(builder)
    }
    fun author(name: String? = null, url: String? = null, iconUrl: String? = null, builder: Author.() -> Unit = {}) {
        author = Author(name, url, iconUrl).apply(builder)
    }

    fun mapFields() = fields.map { MessageEmbed.Field(it.name, it.value, it.inline) }

    data class Author(var name: String? = null, var url: String? = null, var iconUrl: String? = null) {

        fun asAuthor() = MessageEmbed.Author(name, url)

    }

    data class Media(var url: String = "") {

        fun asMedia() = MessageEmbed.Media(url)

    }

    data class Footer(var text: String = "", var iconUrl: String? = null) {

        fun asFooter() = MessageEmbed.Footer(text, iconUrl)

    }

    data class Field(var name: String = "", var value: String = "", var inline: Boolean = false) {

        fun asField() = MessageEmbed.Field(name, value, inline)

    }

    fun build() = MessageEmbed(title, MessageEmbed.Type.RICH, description, url, timestamp, color, footer?.asFooter(), image?.asMedia(), thumbnail?.asMedia(), null, null, author?.asAuthor(), mapFields())

}

inline fun buildEmbed(builder: EmbedBuilder.() -> Unit) = EmbedBuilder().apply(builder).build()