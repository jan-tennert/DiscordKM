package io.github.jan.discordkm.api.entities.activity

enum class PresenceStatus(val status: String) {
    ONLINE("online"),
    DO_NOT_DISTURB("dnd"),
    IDLE("idle"),
    INVISIBLE("invisible"),
    OFFLINE("offline")
}