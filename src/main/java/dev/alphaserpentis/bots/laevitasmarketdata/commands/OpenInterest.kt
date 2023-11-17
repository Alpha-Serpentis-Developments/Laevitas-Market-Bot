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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.text.NumberFormat

open class OpenInterest : BotCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions(
        "oi",
        "Obtain the open interest for futures and options"
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
        val futuresSubcommand = SubcommandData(
            "futures",
            "Obtains the OI for futures"
        )
            .addOption(OptionType.STRING, "currency", "The currency to get OI for", true, true)
            .addOption(OptionType.STRING, "type", "Type of exchange", true, true)
        val optionsSubcommand = SubcommandData(
            "options",
            "Obtains the OI for options"
        )
            .addOption(OptionType.STRING, "currency", "The currency to get OI for", true, true)
            .addOption(OptionType.STRING, "market", "The market to get OI for", true, true)
            .addOption(OptionType.STRING, "maturity", "The maturity to get OI for", true, true)
        val oiChangeSubcommand = SubcommandData(
            "change",
            "View the OI winners and losers for futures of the specified currency"
        )
            .addOption(OptionType.STRING, "currency", "The currency to get OI for", true, true)
            .addOption(OptionType.STRING, "type", "Type of future", true, true)
            .addOption(OptionType.INTEGER, "period", "The period to get OI for in hours", true, true)
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addSubcommands(futuresSubcommand, optionsSubcommand, oiChangeSubcommand)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun configureEmbedBuilder(eb: EmbedBuilder, event: SlashCommandInteractionEvent) {
        val subcommandName = event.subcommandName!!

        when(subcommandName) {
            "futures" -> {
                val currency = event.getOption("currency")!!.asString
                val type = event.getOption("type")!!.asString

//                generateFuturesOi(eb, currency, type)
            }
            "options" -> {
                val currency = event.getOption("currency")!!.asString
                val market = event.getOption("market")!!.asString
                val maturity = event.getOption("maturity")!!.asString

//                generateOptionsOi(eb, currency, market, maturity)
            }
            "change" -> {
                val currency = event.getOption("currency")!!.asString
                val type = event.getOption("type")!!.asString
                val period = event.getOption("period")!!.asLong

                generateTopGainersAndLosers(eb, currency, type, period)
            }
        }
    }

    private fun generateTopGainersAndLosers(eb: EmbedBuilder, currency: String, type: String, period: Long) {
        val oiChange = laevitasService!!.futuresMarketsOiGainersAndLosers(currency, type, period.toString())
        val date = LaevitasDataHandler.getUTCTimeFromMilliseconds(oiChange.date * 1000).plus(" UTC")
        val sortedList = oiChange.data.sortedByDescending { it.oiChangePercent }
        val sb = StringBuilder()
        val nf = NumberFormat.getCurrencyInstance()

        eb.setTitle("Top OI Gainers and Losers for $$currency ($type)")

        if (sortedList.isEmpty()) {
            sb.append("No data found")
        } else if (sortedList.size > 10) {
            val topFive = sortedList.subList(0, 5)
            val bottomFive = sortedList.subList(sortedList.size - 5, sortedList.size).reversed()

            sb.append("## Top 5 OI Gainers and Losers").append("\n")

            for (i in 0..4) {
                sb.append("$i. **${topFive[i].market}**").append("\n")
                sb.append(" - **Change**: ${nf.format(topFive[i].oiChange)} (${topFive[i].oiChangePercent}%)").append("\n")
                sb.append(" - **Notional Change**: ${nf.format(topFive[i].oiNotionalChange)} (${topFive[i].oiNotionalChangePercent}%)").append("\n")
            }

            sb.append("\n")
            sb.append("## Bottom 5 OI Gainers and Losers").append("\n")

            for (i in 0..4) {
                sb.append("${i + 5}. **${bottomFive[i].market}**").append("\n")
                sb.append(" - **Change**: ${nf.format(bottomFive[i].oiChange)} (${bottomFive[i].oiChangePercent}%)").append("\n")
                sb.append(" - **Notional Change**: ${nf.format(bottomFive[i].oiNotionalChange)} (${bottomFive[i].oiNotionalChangePercent}%)").append("\n")
            }
        } else {
            for (i in sortedList.indices) {
                sb.append("$i. **${sortedList[i].market}**").append("\n")
                sb.append(" - **Change**: ${nf.format(sortedList[i].oiChange)} (${sortedList[i].oiChangePercent}%)").append("\n")
                sb.append(" - **Notional Change**: ${nf.format(sortedList[i].oiNotionalChange)} (${sortedList[i].oiNotionalChangePercent}%)").append("\n")
            }
        }

        eb.setDescription(sb.toString())
        eb.setFooter("Last Updated: $date")
    }
}