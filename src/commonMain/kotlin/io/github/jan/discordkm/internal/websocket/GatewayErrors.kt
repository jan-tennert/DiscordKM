/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.websocket

import io.github.jan.discordkm.internal.restaction.DiscordError

enum class GatewayErrors(override val code: Short, override val message: String) : DiscordError {
    UNKNOWN_ERROR(4000, "Unknown Error"),
    UNKNOWN_OPCODE(4001, "Unknown Opcode"),
    DECODE_ERROR(4002, "Decode Error"),
    NOT_AUTHENTICATED(4003, "Not Authenticated"),
    AUTHENTICATION_FAILED(4004, "Authentication Failed. Check your bot's token."),
    ALREADY_AUTHENTICATED(4005, "Already Authenticated"),
    INVALID_SEQUENCE(4007, "Invalid Sequence (Failed to resume)"),
    RATE_LIMITED(4008, "Rate Limited (Too many requests)"),
    SESSION_TIMED_OUT(4009, "Session Timed Out"),
    INVALID_SHARD(4010, "Invalid Shard"),
    SHARDING_REQUIRED(4011, "Sharding Required, the session would have handled too many guilds."),
    INVALID_API_VERSION(4012, "Invalid API Version"),
    INVALID_INTENTS(4013, "Invalid Intents"),
    DISALLOWED_INTENTS(4014, "Disallowed Intents. You may selected an intent that is not enabled on your application."),
}