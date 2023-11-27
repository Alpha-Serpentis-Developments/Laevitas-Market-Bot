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
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.text.NumberFormat

open class GainersAndLosers : BotCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions(
        "winners",
        "Get the winners and losers for a given exchange, type, and period"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    private val laevitasService: LaevitasService? = LaevitasDataHandler.service

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()

        configureEmbedBuilder(eb, event)

        return CommandResponse(isOnlyEphemeral, eb.build())
    }

    override fun updateCommand(jda: JDA) {
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addOption(OptionType.STRING, "exchange", "The exchange", true, true)
            .addOption(OptionType.STRING, "type", "Type of derivatives", true, true)
            .addOption(OptionType.STRING, "period", "Period to get data for in hours", true, true)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun configureEmbedBuilder(eb: EmbedBuilder, event: SlashCommandInteractionEvent) {
        val exchange = event.getOption("exchange")!!.asString
        val type = event.getOption("type")!!.asString
        val period = event.getOption("period")!!.asString

        eb.setTitle("Top Winners and Losers for $exchange")

        generateWinnersAndLosers(eb, exchange, type, period)
    }

    private fun generateWinnersAndLosers(eb: EmbedBuilder, ex: String, type: String, period: String) {
        val winners = laevitasService!!.derivsPriceGainers(ex, type, period)
        val utcTime = LaevitasDataHandler.getUTCTimeFromMilli(winners.date * 1000).plus(" UTC")
        val list = winners.data.asReversed()

        if (list.isEmpty()) {
            eb.setDescription("No data found for the given exchange, type, and period")
        } else {
            val sb = StringBuilder()
            val specifiedList = if(list.size > 20) {
                val firstTen = list.subList(0, 10)
                val lastTen = list.subList(list.size - 10, list.size).asReversed()

                firstTen + lastTen
            } else {
                list
            }

            sb.append("**Notice**: Data only shows a maximum of 20 winners and losers\n\n")

            specifiedList.forEachIndexed { index, data ->
                val change = data.priceChange
                val changeStr = if(change > 0) {
                    "+${NumberFormat.getInstance().format(change)}"
                } else {
                    NumberFormat.getInstance().format(change)
                }

                sb.append("${index + 1}. ${data.symbol} - $changeStr%\n")
            }

            eb.setDescription(sb.toString())
        }

        eb.setFooter("Last Updated: $utcTime")
    }
}