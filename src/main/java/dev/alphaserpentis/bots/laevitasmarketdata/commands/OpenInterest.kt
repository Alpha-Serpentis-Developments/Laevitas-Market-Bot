package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.bots.laevitasmarketdata.data.api.OptionsOiByStrike
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.text.NumberFormat

open class OpenInterest(laevitasService: LaevitasService) : LaevitasCommand(
    laevitasService,
    associatedPaths = mapOf(
        "futures" to "/analytics/futures/oi_breakdown/{currency}/{type}",
        "options" to "/analytics/options/oi_strike/{market}/{currency}/{maturity}",
        "change" to "/analytics/futures/markets_oi_gainers_and_losers/{currency}/{option}/{param}"
    ),
    botCommandOptions = BotCommandOptions(
        "oi",
        "Obtain the open interest for futures and options"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        return CommandResponse(isOnlyEphemeral, configureEmbedBuilder(event).build())
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
            .addOption(OptionType.STRING, "maturity", "The maturity to get OI for", false, true)
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

    private fun configureEmbedBuilder(event: SlashCommandInteractionEvent): EmbedBuilder {
        val eb = EmbedBuilder()
        val subcommandName = event.subcommandName!!

        when(subcommandName) {
            "futures" -> {
                val currency = event.getOption("currency")!!.asString
                val type = event.getOption("type")!!.asString

                generateFuturesOi(eb, currency, type)
            }
            "options" -> {
                val currency = event.getOption("currency")!!.asString
                val market = event.getOption("market")!!.asString
                val maturity = event.getOption("maturity")?.asString

                generateOptionsOi(eb, currency, market, maturity)
            }
            "change" -> {
                val currency = event.getOption("currency")!!.asString
                val type = event.getOption("type")!!.asString
                val period = event.getOption("period")!!.asLong

                generateTopGainersAndLosers(eb, currency, type, period)
            }
        }

        if (eb.fields.isEmpty()) handleInvalidArgument(eb)

        return eb
    }

    private fun generateFuturesOi(eb: EmbedBuilder, currency: String, type: String) {
        val nf = NumberFormat.getCurrencyInstance()
        val oiBreakdown = laevitasService.futuresOiBreakdown(currency, type)
        val sb = StringBuilder()
        val date = LaevitasDataHandler.getUTCTimeFromMilli(oiBreakdown.date * 1000).plus(" UTC")
        val typeName = if (type == "C") "Centralized" else "Decentralized"
        val data = oiBreakdown.data

        eb.setTitle("Open Interest for $$currency ($typeName)")

        sb.append("Data formatted as:\n\n**Market**\n")
        sb.append("  - All: Open Interest (Notional)\n")
        sb.append("  - Futures: Open Interest (Notional)\n")
        sb.append("  - Perpetual: Open Interest (Notional)\n\n")

        for (exchange in data.all.usd.keys) {
            val allOi = data.all.usd[exchange] ?: continue
            val allNotional = data.all.notional[exchange] ?: continue
            val futuresOi = data.future?.usd?.get(exchange)
            val futuresNotional = data.future?.notional?.get(exchange)
            val perpetualOi = data.perpetual?.usd?.get(exchange)
            val perpetualNotional = data.perpetual?.notional?.get(exchange)

            sb.append("**$exchange**").append("\n")
            sb.append(" - All: ${nf.format(allOi)} ($allNotional)").append("\n")
            if (futuresOi != null && futuresNotional != null)
                sb.append("  - Futures: ${nf.format(futuresOi)} ($futuresNotional)").append("\n")
            if (perpetualOi != null && perpetualNotional != null)
                sb.append("  - Perpetual: ${nf.format(perpetualOi)} ($perpetualNotional)").append("\n")
        }

        eb.setDescription(sb.toString())
        eb.setFooter("Last Updated: $date")
    }

    private fun generateOptionsOi(eb: EmbedBuilder, currency: String, market: String, maturity: String?) {
        val nf = NumberFormat.getCurrencyInstance()
        val sb = StringBuilder()
        val oiStrike: OptionsOiByStrike

        if (maturity == null) { // Call the endpoint at /analytics/options/oi_strike_all/{market}/{currency}
            oiStrike = laevitasService.optionsOiByStrike(market, currency)

            eb.setTitle("Open Interest for $$currency ($market)")
        } else { // Call the endpoint at /analytics/options/oi_strike/{market}/{currency}/{maturity}
            oiStrike = laevitasService.optionsOiByStrike(market, currency, maturity)

            eb.setTitle("Open Interest for $$currency-$maturity ($market)")
        }

        val date = LaevitasDataHandler.getUTCTimeFromMilli(oiStrike.date * 1000).plus(" UTC")
        val sortedList = oiStrike.data.sortedBy { it.strike }

        sb.append("Data formatted as:\n\n**Strike**\n")
        sb.append(" - Call Notional (Call OI) / Put Notional (Put OI)\n\n")

        for (item in sortedList) {
            val strike = item.strike
            val callV = item.callNotional
            val putV = item.putNotional
            val callText = "${nf.format(callV)} (${item.callOi})"
            val putText = "${nf.format(putV)} (${item.putOi})"

            if (callV + putV == 0.0) continue

            sb.append("**${nf.format(strike)}**").append("\n")
            sb.append(" - $callText / $putText").append("\n")
        }

        eb.setDescription(sb.toString())
        eb.setFooter("Last Updated: $date")
    }

    private fun generateTopGainersAndLosers(eb: EmbedBuilder, currency: String, type: String, period: Long) {
        val oiChange = laevitasService.futuresMarketsOiGainersAndLosers(currency, type, period.toString())
        val date = LaevitasDataHandler.getUTCTimeFromMilli(oiChange.date * 1000).plus(" UTC")
        val sortedList = oiChange.data.sortedByDescending { it.oiChange }
        val sb = StringBuilder()
        val nf = NumberFormat.getCurrencyInstance()

        eb.setTitle("Top OI Gainers and Losers for $$currency ($type)")

        if (sortedList.isEmpty()) {
            sb.append("No data found")
        } else if (sortedList.size > 10) {
            val topFive = sortedList.subList(0, 5)
            val bottomFive = sortedList.subList(sortedList.size - 5, sortedList.size)

            sb.append("Sorted by OI change").append("\n")
            sb.append("## Top 5 OI Gainers and Losers").append("\n")

            for (i in 0..4) {
                val item = topFive[i]

                sb.append("$i. **${item.market}**").append("\n")
                sb.append(" - **Change**: ${nf.format(item.oiChange)} (${item.oiChangePercent}%)").append("\n")
                sb.append(" - **Notional Change**: ${nf.format(item.oiNotionalChange)} (${item.oiNotionalChangePercent}%)").append("\n")
            }

            sb.append("\n")
            sb.append("## Bottom 5 OI Gainers and Losers").append("\n")

            for (i in 0..4) {
                val item = bottomFive[i]

                sb.append("${i + 5}. **${item.market}**").append("\n")
                sb.append(" - **Change**: ${nf.format(item.oiChange)} (${item.oiChangePercent}%)").append("\n")
                sb.append(" - **Notional Change**: ${nf.format(item.oiNotionalChange)} (${item.oiNotionalChangePercent}%)").append("\n")
            }
        } else {
            sb.append("Sorted by OI change").append("\n")

            for (i in sortedList.indices) {
                val item = sortedList[i]

                sb.append("$i. **${item.market}**").append("\n")
                sb.append(" - **Change**: ${nf.format(item.oiChange)} (${item.oiChangePercent}%)").append("\n")
                sb.append(" - **Notional Change**: ${nf.format(item.oiNotionalChange)} (${item.oiNotionalChangePercent}%)").append("\n")
            }
        }

        eb.setDescription(sb.toString())
        eb.setFooter("Last Updated: $date")
    }
}