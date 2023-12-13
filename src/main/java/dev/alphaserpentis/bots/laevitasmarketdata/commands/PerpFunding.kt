package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.bots.laevitasmarketdata.data.api.PerpetualFunding
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

open class PerpFunding(laevitasService: LaevitasService) : LaevitasCommand(
    laevitasService,
    associatedPaths = mapOf(
        "funding" to "/analytics/futures/perpetual_funding/{currency}"
    ),
    botCommandOptions = BotCommandOptions(
        "funding",
        "Obtains the funding rates for a given currency"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    private val tableHeader = """
        ```
        ╒═══════════════════════╤════════════╕
        │ Exchange Pair         | Rate       |
        ╞═══════════════════════╪════════════╡
    """.trimIndent()
    private val tableFooter = "╘═══════════════════════╧════════════╛```"

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
            true,
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
        val funding = getFunding(currency)
        val date = LaevitasDataHandler.getUTCTimeFromMilli(funding.date * 1000).plus(" UTC")
        val sb = StringBuilder()

        sb.append("# Funding Rates for $$currency").append("\n")
        sb.append(tableHeader).append("\n")

        if (averaged)
            generateAveragedTable(sb, funding.data)
        else
            generateNonAveragedTable(sb, funding.data)

        sb.append(tableFooter)
        eb.setDescription(sb.toString())
        eb.setFooter("Last Updated: $date")
    }

    private fun generateNonAveragedTable(sb: StringBuilder, list: List<PerpetualFunding.PerpetualFundingEntry>) {
        list.sortedByDescending { it.funding }.forEach { entry ->
            var exchangePair: String = entry.market.plus(" - ").plus(entry.symbol)
            val fundingRate: String = entry.funding.toString().plus('%')

            if(exchangePair.length > 21) exchangePair = exchangePair.substring(0, 18).plus("...")

            sb.append("| ${exchangePair.padEnd(21)} | ${fundingRate.padEnd(10)} |\n")
        }
    }

    private fun generateAveragedTable(sb: StringBuilder, list: List<PerpetualFunding.PerpetualFundingEntry>) {
        var currentExchange = ""
        var currentValue = 0.0
        var currentCount = 0
        var totalValue = 0.0
        var totalCount = 0

        list.sortedByDescending { it.funding }.forEach { entry ->
            if(currentExchange == entry.market) {
                currentValue += entry.funding
                currentCount++
            } else {
                if(currentExchange.isNotEmpty()) {
                    val average = currentValue / currentCount
                    var exchangePair: String = currentExchange

                    if(exchangePair.length > 21) exchangePair = exchangePair.substring(0, 18).plus("...")

                    sb.append("| ${exchangePair.padEnd(21)} | ${"%.5f".format(average).plus('%').padEnd(10)} |\n")
                }

                currentExchange = entry.market
                currentValue = entry.funding
                currentCount = 1
            }

            totalValue += entry.funding
            totalCount++
        }

        if(totalValue > 0 && totalCount > 0) {
            val average = totalValue / totalCount

            sb.append("| ${"AVERAGED".padEnd(21)} | ${"%.5f".format(average).plus('%').padEnd(10)} |\n")
        }
    }

    private fun getFunding(currency: String) = laevitasService.perpetualFunding(currency)
}
