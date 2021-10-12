package io.github.jan.discordkm.internal

@RequiresOptIn(
    "This is internal for DiscordKM and can remove any time",
    RequiresOptIn.Level.WARNING
)
annotation class DiscordKMInternal

@RequiresOptIn(
    "This feature is unstable or not tested",
    RequiresOptIn.Level.ERROR
)
annotation class DiscordKMUnstable