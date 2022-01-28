# DiscordKM

A Kotlin Multiplatform Discord API 
Wrapper [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.discordkm/DiscordKM)](https://search.maven.org/artifact/io.github.jan-tennert.discordkm/DiscordKM)

Note: currently **JS** and **JVM** are supported due to issues with Kotlin/Native

You can use this [trello board](https://trello.com/b/EQqz7hAY/discordkm) to see my progress

Discord: [Click here](https://discord.gg/mxRqJa4pHe) \

#### Warning: This project is in Alpha and can change without notice

# Introduction

You can create a client easily by just using buildClient:

```kotlin
 val client = buildClient("TOKEN") {
    intents = mutableSetOf( //set intents
        Intent.GUILD_MEMBERS,
        Intent.GUILD_PRESENCES,
        Intent.GUILD_WEBHOOKS,
        Intent.GUILD_MESSAGE_TYPING,
        Intent.DIRECT_MESSAGE_TYPING,
        Intent.GUILDS
    )

    loggingLevel = Logger.Level.WARN
    reconnectDelay = 10.seconds
    enabledCache -= CacheFlag.VOICE_STATES //remove caches you don't need
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
If you want a specific target add it to the artifactId like: DiscordKM-jvm and DiscordKM-js

# JVM-Only
If you want to easily add event listeners without having to use the Kotlin DSL you can use the following:
```kotlin
client.importCommands("io.github.jan.bot.commands", subPackages = true, mapOf(
   "someValue" to 1.0 
) /* Map for argument injection */)

//then you can add top level functions in the commands package and in all its subpackages
@CommandExecutor(name = "test", subCommand = "subCommand") //You can optionally use subCommand and subCommandGroup
fun SlashCommandEvent.testCommand(@Inject("someValue") value: Double) {
    interaction.reply("injected value: $value")
}

//same with events:
client.importEvents("io.github.jan.bot.events", subPackages = true, mapOf(
    "node" to lavalinkNode
))

@EventListener
fun MessageCreateEvent.play(@Inject("node") lavalinkNode: LavalinkNode) {
    //play music
}

```

# Addons

- [DiscordKM-Lavalink](https://github.com/jan-tennert/DiscordKM-Lavalink) - Lavalink client for DiscordKM