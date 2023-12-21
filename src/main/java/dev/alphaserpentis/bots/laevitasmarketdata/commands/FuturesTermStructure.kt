package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.bots.laevitasmarketdata.data.api.FuturesCurve
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.ChartHandler
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload

open class FuturesTermStructure(laevitasService: LaevitasService) : LaevitasCommand(
    laevitasService,
    associatedPaths = mapOf(
        "curve" to "/analytics/futures/futures_curve/{currency}/{market}"
    ),
    botCommandOptions = BotCommandOptions(
        "curve",
        "Obtains the futures curve for a given currency, and optionally a market"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val currency = event.getOption("currency")!!.asString
        val market = event.getOption("market")?.asString
        val eb = EmbedBuilder()
        val chart = generateEmbed(eb, currency, market)

        if (chart.isNotEmpty()) {
            val fileUpload = FileUpload.fromData(chart, "futuresCurve.png")

            return CommandResponse(isOnlyEphemeral, fileUpload, eb.build())
        }

        handleInvalidArgument(eb)

        return CommandResponse(isOnlyEphemeral, eb.build())
    }

    override fun updateCommand(jda: JDA) {
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addOption(OptionType.STRING, "currency", "The currency", true, true)
            .addOption(OptionType.STRING, "market", "The market", false, true)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun generateEmbed(eb: EmbedBuilder, currency: String, market: String?): ByteArray {
        val futuresCurve = laevitasService.futuresCurve(currency, market)
        val futuresCurveChart = generateChart(futuresCurve, currency, market)
        val date = LaevitasDataHandler.getUTCTimeFromMilli(futuresCurve.date * 1000).plus(" UTC")

        eb.setTitle("Futures Term Structure of $currency")
        eb.setImage("attachment://futuresCurve.png")
        eb.setFooter("Last Updated: $date")

        return futuresCurveChart
    }

    private fun generateChart(futuresCurve: FuturesCurve, currency: String, market: String?): ByteArray {
        if (market != null) {
            val data = ArrayList<Pair<String, Double>>()

            futuresCurve.data.forEach {
                data.add(Pair(it.maturity, it.value))
            }

            return ChartHandler.generateXYTimedSeriesChart(
                "Futures Term Structure of $currency (${market})",
                "Maturity",
                "Price (USD)",
                data
            )
        } else {
            val data = HashMap<String, List<Pair<String, Double>>>()

            futuresCurve.data.forEach {
                val list: ArrayList<Pair<String, Double>> = (data[it.market] ?: ArrayList()) as ArrayList<Pair<String, Double>>

                list.add(Pair(it.maturity, it.value))
                data[it.market] = list
            }

            return ChartHandler.generateXYTimedSeriesChart(
                "Futures Term Structure of $currency (All)",
                "Maturity",
                "Price (USD)",
                data
            )
        }
    }

}