import com.github.jan.discordkm.Client
import com.github.jan.discordkm.buildClient
import com.github.jan.discordkm.entities.guild.channels.TextChannel
import com.github.jan.discordkm.events.GuildCreateEvent

suspend fun main() {
    val client = buildClient("ODAwODIyMjg0ODg1NTU3MjQ5.YAXt3w.xG56d7pASzIQ-rO6mmWhDC317lY") {
        intents = mutableListOf(Client.Intent.GUILD_MEMBERS, Client.Intent.GUILDS, Client.Intent.GUILD_MESSAGES)
    }

    client.on<GuildCreateEvent> {
         guild.channels.retrieve<TextChannel>(879406150607589386).delete()
    }

    client.login()
}




