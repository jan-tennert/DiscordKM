package com.github.jan.discordkm.entities.guild

import com.github.jan.discordkm.entities.EnumSerializer
import com.github.jan.discordkm.entities.SerializableEnum
import com.github.jan.discordkm.entities.misc.EnumList

enum class Permission(override val offset: Int) : SerializableEnum<Permission> {

    CREATE_INSTANT_INVITE(0),
    KICK_MEMBERS(1),
    BAN_MEMBERS(2),
    ADMINISTRATOR(3),
    MANAGE_CHANNELS(4),
    MANAGE_GUILD(5),
    ADD_REACTIONS(6),
    VIEW_AUDIT_LOG(7),
    PRIORITY_SPEAKER(8),
    STREAM(9),
    VIEW_CHANNEL(10),
    SEND_MESSAGES(11),
    SEND_TTS_MESSAGES(12),
    MANAGE_MESSAGES(13),
    EMBED_LINKS(14),
    ATTACH_FILES(15),
    READ_MESSAGE_HISTORY(16),
    MENTION_EVERYONE(17),
    USE_EXTERNAL_EMOJIS(18),
    VIEW_GUILD_INSIGHTS(19),
    CONNECT(20),
    SPEAK(21),
    MUTE_MEMBERS(22),
    DEAFEN_MEMBERS(23),
    MOVE_MEMBERS(24),
    USE_VAD(25),
    CHANGE_NICKNAME(26),
    MANAGE_NICKNAMES(27),
    MANAGE_ROLES(28),
    MANAGE_WEBHOOKS(29),
    MANAGE_EMOJIS_AND_STICKERS(30),
    USE_APPLICATION_COMMANDS(31),
    REQUEST_TO_SPEAK(32),
    MANAGE_THREADS(33),
    USE_PUBLIC_THREADS(34),
    USE_PRIVATE_THREADS(35),
    USE_EXTERNAL_STICKERS(36),
    UNKNOWN(-1);

    companion object : EnumSerializer<Permission> {
        override val values = values().toList()
    }

}