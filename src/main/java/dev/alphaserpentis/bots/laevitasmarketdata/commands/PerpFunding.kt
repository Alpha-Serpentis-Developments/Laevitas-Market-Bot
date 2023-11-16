package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.commands.BotCommand
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

open class PerpFunding : BotCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions("funding", "Obtains the funding rates for a given currency")
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    private val laevitasService: LaevitasService = LaevitasDataHandler.service

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()
        val currency = event.getOption("currency")!!.asString
        val averaged = event.getOption("averaged")?.asBoolean ?: false

        configureEmbedBuilder(eb, currency, averaged)

        return CommandResponse(isOnlyEphemeral, eb.build())
    }

    override fun updateCommand(jda: JDA) {
        val currencyOption = OptionData(
            OptionType.STRING,
            "currency",
            "The currency to get funding rates for",
            true
        )
        val averagedOption = OptionData(
            OptionType.BOOLEAN,
            "averaged",
            "Whether to get the averaged funding rates"
        )
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addOptions(currencyOption, averagedOption)
        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun configureEmbedBuilder(eb: EmbedBuilder, currency: String, averaged: Boolean) {
        val funding = getFundingList(currency)
        val sb = StringBuilder()
        sb.append("# Funding rates for $currency").append("\n")
    }

    private fun getFundingList(currency: String) = laevitasService.perpetualFunding(currency).data
}
