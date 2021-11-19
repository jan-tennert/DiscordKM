package io.github.jan.discordkm.api.entities.modifiers.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.modifiers.BaseModifier
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.internal.utils.ifNotEmpty
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GuildModifier : BaseModifier {

    var name: String? = null
    var verificationLevel: Guild.VerificationLevel? = null
    var defaultMessageNotificationLevel: Guild.NotificationLevel? = null
    var explicitContentFilter: Guild.ExplicitContentFilter? = null
    private var afkChannelId: Snowflake? = null
    private var afkTimeout: TimeSpan? = null
    var icon: Image? = null
    private var ownerId: Snowflake? = null
    var splashImage: Image? = null
    var discoverySplashImage: Image? = null
    var bannerImage: Image? = null
    private var systemChannelId: Snowflake? = null
    private var systemChannelFlags: MutableSet<Guild.SystemChannelFlag> = mutableSetOf()
    var rulesChannelId: Snowflake? = null
    var publicUpdatesChannelId: Snowflake? = null
    var preferredLocale: String? = null
    var features: MutableSet<Guild.Feature> = mutableSetOf()
    var description: String? = null
    var enablePremiumProgressBar: Boolean? = null

    override val data
        get() = buildJsonObject {
            putOptional("name", name)
            putOptional("verification_level", verificationLevel?.ordinal)
            putOptional("default_message_notifications", defaultMessageNotificationLevel?.ordinal)
            putOptional("explicit_content_filter", explicitContentFilter?.ordinal)
            putOptional("afk_channel_id", afkChannelId)
            putOptional("afk_timeout", afkTimeout)
            putOptional("icon", icon?.encodedData)
            putOptional("owner_id", ownerId)
            putOptional("splash", splashImage?.encodedData)
            putOptional("discovery_splash", discoverySplashImage?.encodedData)
            putOptional("banner", bannerImage?.encodedData)
            putOptional("system_channel_id", systemChannelId)
            systemChannelFlags.ifNotEmpty { put("system_channel_flags", systemChannelFlags.rawValue()) }
            putOptional("rules_channel_id", rulesChannelId)
            putOptional("public_updates_channel_id", publicUpdatesChannelId)
            putOptional("preferred_locale", preferredLocale)
            putOptional("name", name)
            features.ifNotEmpty { put("features", JsonArray(features.map { JsonPrimitive(it.name) })) }
            putOptional("description", description)
            putOptional("premium_progress_bar_enabled", enablePremiumProgressBar)
        }

    fun afkChannel(modifier: AfkChannelModifier.() -> Unit) {
        val newModifier = AfkChannelModifier().apply(modifier)
        afkChannelId = newModifier.id
        afkTimeout = newModifier.timeout
    }

    fun systemChannel(modifier: SystemChannelModifier.() -> Unit) {
        val newModifier = SystemChannelModifier().apply(modifier)
        afkChannelId = newModifier.id
        systemChannelFlags = newModifier.flags
    }

    fun transferOwnershipTo(memberId: Snowflake) {
        ownerId = memberId
    }

    fun transferOwnershipTo(member: Member) = transferOwnershipTo(member.id)

    inner class AfkChannelModifier(var id: Snowflake? = null, var timeout: TimeSpan? = null)
    inner class SystemChannelModifier(
        var id: Snowflake? = null,
        var flags: MutableSet<Guild.SystemChannelFlag> = mutableSetOf()
    )

}