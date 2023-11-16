package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.coffeecore.commands.BotCommand
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

open class GainersAndLosers : BotCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions(
        "winners",
        "Get the winners and losers for a given currency"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()

        return CommandResponse(isOnlyEphemeral, eb.build())
    }
}