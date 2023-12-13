package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.coffeecore.commands.BotCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

abstract class LaevitasCommand(
    val laevitasService: LaevitasService,
    val overrideAutocompleteMapping: Map<String, List<String>> = emptyMap(),
    val associatedPaths: Map<String, String> = emptyMap(),
    botCommandOptions: BotCommandOptions
) : BotCommand<MessageEmbed, SlashCommandInteractionEvent>(botCommandOptions) {
    protected fun handleInvalidArgument(eb: EmbedBuilder) {
        eb.setDescription("The arguments you provided are invalid or not applicable to this command")
        eb.setColor(0xFF0000)
    }
}