/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.message

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