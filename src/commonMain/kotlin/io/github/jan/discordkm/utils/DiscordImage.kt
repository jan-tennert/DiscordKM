package io.github.jan.discordkm.utils

import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Sticker

object DiscordImage {

    private val baseURL = "https://cdn.discordapp.com/"

    fun applicationImage(id: Snowflake, hash: String) = getImage("app-icons", id, hash)

    fun guildBanner(id: Snowflake, hash: String) = getImage("banners", id, hash)

    fun guildIcon(id: Snowflake, hash: String) = getImage("icons", id, hash, if(hash.startsWith("a_")) "gif" else "png")

    fun guildSplash(id: Snowflake, hash: String) = getImage("splashes", id, hash)

    fun guildDiscoverySplash(id: Snowflake, hash: String) = getImage("discovery-splashes", id, hash)

    fun userBanner(id: Snowflake, hash: String) = getImage("banners", id, hash, if(hash.startsWith("a_")) "gif" else "png")

    fun defaultUserAvatar(discriminator: Int) = "${baseURL}embed/avatars/${discriminator % 5}"

    fun userAvatar(id: Snowflake, hash: String) = getImage("avatars", id, hash, if(hash.startsWith("a_")) "gif" else "png")

    fun sticker(id: Snowflake, type: Sticker.FormatType) = "${baseURL}stickers/$id.${if(type == Sticker.FormatType.PNG || type == Sticker.FormatType.APNG) "png" else "json"}"

    private fun getImage(path: String, id: Snowflake, hash: String, extension: String = "png") = "$baseURL$path/$id/$hash.$extension"

}