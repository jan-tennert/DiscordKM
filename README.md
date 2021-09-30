# DiscordKM

A Kotlin Multiplatform Discord API
Wrapper ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.jan-tennert.discordkm/DiscordKM/badge.svg?style=plastic?gav=true)

You can use this [trello board](https://trello.com/b/EQqz7hAY/discordkm) to see my progress

# Introduction

You can create a client easily by just using buildClient:

```kotlin
 val client = buildClient("TOKEN") {
    intents = mutableListOf( //set intents
        Intent.GUILD_MEMBERS,
        Intent.GUILD_PRESENCES,
        Intent.GUILD_WEBHOOKS,
        Intent.GUILD_MESSAGE_TYPING,
        Intent.DIRECT_MESSAGE_TYPING,
        Intent.GUILDS
    )

    loggingLevel = Logger.Level.WARN
    reconnectDelay = 10.seconds
    enabledCache -= Cache.VOICE_STATES //remove caches you don't need
}

client.on<MessageCreateEvent> {
    println("Received message: ${message.content}")
}

//slash commands:
client.on<ReadyEvent> {
    client.commands.createChatInputCommand {
        name = "test"
        description = "This is a chat input command!"

        options {
            string(name = "option1", description = "This is a string option", required = true) {
                choice("choice1", "choice1")
            }
            user("option2", "This is a user option", required = true)
        }
    }
}

client.on<SlashCommandEvent>(predicate = { it.commandName == "test" }) {
    interaction.reply("String Option: ${options["option1"].string}. User Name: ${options["option2"].user.name}")
}

client.login()
```

# Installation

You can just install DiscordKM using:

Kotlin Dsl:

```kotlin
implementation("io.github.jan-tennert.discordkm:DiscordKM:VERSION")
```

Maven:

```xml

<dependency>
    <groupId>io.github.jan-tennert.discordkm</groupId>
    <artifactId>DiscordKM</artifactId>
    <version>VERSION</version>
</dependency>
```

If you want a specific target add it to the artifactId like: DiscordKM-jvm or DiscordKM-mingwx64
