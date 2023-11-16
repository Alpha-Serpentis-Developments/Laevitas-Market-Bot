package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.commands.BotCommand
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

open class FuturesCurve : BotCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions(
        "curve",
        "Obtains the futures curve for a given currency, and optionally a market"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    private val laevitasService: LaevitasService = LaevitasDataHandler.service

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()

        return CommandResponse(isOnlyEphemeral, eb.build())
    }

}