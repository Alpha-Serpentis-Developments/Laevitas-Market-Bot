package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
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

class VolRun(laevitasService: LaevitasService) : LaevitasCommand(
    laevitasService,
    overrideAutocompleteMapping = mapOf(
        "currency" to listOf("btc", "eth"),
        "data" to listOf("skew", "rr", "fly")
    ),
    botCommandOptions = BotCommandOptions(
        "volrun",
        "Get the volatility run for BTC or ETH"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()
        val currency = event.getOption("currency")!!.asString
        val data = event.getOption("data")!!.asString
        val chart = generateEmbed(eb, currency, data)
        val fileUpload = FileUpload.fromData(chart, "volRun.png")

        return CommandResponse(isOnlyEphemeral, fileUpload, eb.build())
    }

    override fun updateCommand(jda: JDA) {
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addOption(OptionType.STRING, "currency", "The currency", true, true)
            .addOption(OptionType.STRING, "data", "The data type to view", true, true)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun generateEmbed(eb: EmbedBuilder, currency: String, data: String?): ByteArray {
        val volRun = laevitasService.volRun(currency)
        val dataForChart = HashMap<String, List<Pair<String, Double>>>()
        val date = LaevitasDataHandler.getUTCTimeFromMilli(volRun.date * 1000).plus(" UTC")
        val yAxisName = when (data) {
            "skew" -> "Skew"
            "rr", "riskreversal" -> "Risk Reversal"
            "fly", "butterfly" -> "Butterfly"
            else -> throw IllegalArgumentException("Invalid data type")
        }

        when (data) {
            "skew" -> {
                dataForChart["Skew 10D"] = ArrayList()
                dataForChart["Skew 15D"] = ArrayList()
                dataForChart["Skew 25D"] = ArrayList()
                dataForChart["Skew 35D"] = ArrayList()

                volRun.data.forEach { value ->
                    (dataForChart["Skew 10D"]!! as ArrayList).add(Pair(value.expiry, value.skew10d))
                    (dataForChart["Skew 15D"]!! as ArrayList).add(Pair(value.expiry, value.skew15d))
                    (dataForChart["Skew 25D"]!! as ArrayList).add(Pair(value.expiry, value.skew25d))
                    (dataForChart["Skew 35D"]!! as ArrayList).add(Pair(value.expiry, value.skew35d))
                }
            }
            "rr", "riskreversal" -> {
                dataForChart["Risk Reversal 10D"] = ArrayList()
                dataForChart["Risk Reversal 15D"] = ArrayList()
                dataForChart["Risk Reversal 25D"] = ArrayList()
                dataForChart["Risk Reversal 35D"] = ArrayList()

                volRun.data.forEach { value ->
                    (dataForChart["Risk Reversal 10D"]!! as ArrayList).add(Pair(value.expiry, value.riskReversal10d))
                    (dataForChart["Risk Reversal 15D"]!! as ArrayList).add(Pair(value.expiry, value.riskReversal15d))
                    (dataForChart["Risk Reversal 25D"]!! as ArrayList).add(Pair(value.expiry, value.riskReversal25d))
                    (dataForChart["Risk Reversal 35D"]!! as ArrayList).add(Pair(value.expiry, value.riskReversal35d))
                }
            }
            "fly", "butterfly" -> {
                dataForChart["Butterfly 10D"] = ArrayList()
                dataForChart["Butterfly 15D"] = ArrayList()
                dataForChart["Butterfly 25D"] = ArrayList()
                dataForChart["Butterfly 35D"] = ArrayList()

                volRun.data.forEach { value ->
                    (dataForChart["Butterfly 10D"]!! as ArrayList).add(Pair(value.expiry, value.butterfly10d))
                    (dataForChart["Butterfly 15D"]!! as ArrayList).add(Pair(value.expiry, value.butterfly15d))
                    (dataForChart["Butterfly 25D"]!! as ArrayList).add(Pair(value.expiry, value.butterfly25d))
                    (dataForChart["Butterfly 35D"]!! as ArrayList).add(Pair(value.expiry, value.butterfly35d))
                }
            }
        }

        eb.setTitle("Volatility Run of $currency")
        eb.setFooter("Last Updated: $date")
        eb.setImage("attachment://volRun.png")

        return ChartHandler.generateXYTimedSeriesChart("Volatility Run of $currency ($yAxisName)", "Expiry", yAxisName, dataForChart)
    }
}