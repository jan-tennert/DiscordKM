package io.github.jan.discordkm.api.entities.modifiers

interface Modifiable<M : JsonModifier, T> {

    /**
     * Modifies this entity
     * @param reason The reason which will be displayed in the audit logs
     */
    suspend fun modify(reason: String? = null, modifier: M.() -> Unit): T

}