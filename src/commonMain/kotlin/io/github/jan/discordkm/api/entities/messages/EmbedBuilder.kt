package io.github.jan.discordkm.api.entities.messages

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.misc.Color

class EmbedBuilder @Deprecated("Use buildEmbed instead") constructor() {

    var title: String? = null
    var description: String? = null
    var url: String? = null
    var timestamp: DateTimeTz? = null
    var color: Color? = null
    var author = AuthorBuilder()
    var image = ImageBuilder()
    var thumbnail = ImageBuilder()
    var fields = mutableListOf<FieldBuilder>()
    var footer = FooterBuilder()

    fun field(builder: FieldBuilder.() -> Unit) {
        fields.add(FieldBuilder().apply(builder))
    }

    data class ImageBuilder(var url: String? = null) {

        operator fun invoke(builder: ImageBuilder.() -> Unit): ImageBuilder {
            val newImage = ImageBuilder().apply(builder)
            this.url = newImage.url
            return this
        }

        fun asMedia() = url?.let { MessageEmbed.Media(it) }

    }

    data class FooterBuilder(var text: String? = null, var iconUrl: String? = null) {

        operator fun invoke(builder: FooterBuilder.() -> Unit): FooterBuilder {
            val newFooter = FooterBuilder().apply(builder)
            this.text = newFooter.text
            this.iconUrl = newFooter.iconUrl
            return this
        }

        fun asFooter() = text?.let { MessageEmbed.Footer(it, iconUrl) }

    }

    data class AuthorBuilder(var text: String? = null, var url: String? = null, var iconUrl: String? = null) {

        operator fun invoke(builder: AuthorBuilder.() -> Unit): AuthorBuilder {
            val newFooter = AuthorBuilder().apply(builder)
            this.text = newFooter.text
            this.iconUrl = newFooter.iconUrl
            this.url = newFooter.url
            return this
        }

        fun asAuthor() = text?.let { MessageEmbed.Author(it, iconUrl) }

    }

    data class FieldBuilder(var name: String? = null, var value: String? = null, var inline: Boolean = false) {

        operator fun invoke(builder: FieldBuilder.() -> Unit): FieldBuilder {
            val newField = FieldBuilder().apply(builder)
            this.name = newField.name
            this.value = newField.value
            this.inline = newField.inline
            return this
        }

        fun asField() = MessageEmbed.Field(name!!, value!!, inline)

    }

    fun build() = MessageEmbed(title, EmbedType.RICH, description, url, timestamp, color, footer.asFooter(), image.asMedia(), thumbnail.asMedia(), null, null, author.asAuthor(), fields.map(FieldBuilder::asField))

}

@Suppress("DEPRECATION")
inline fun buildEmbed(builder: EmbedBuilder.() -> Unit) = EmbedBuilder().apply(builder).build()