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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.FileUpload
import java.text.NumberFormat

open class Volume(laevitasService: LaevitasService) : LaevitasCommand(
    laevitasService,
    associatedPaths = mapOf(
        "perpfuture" to "/analytics/futures/volume_breakdown/{currency}/{type}",
        "options" to "/analytics/options/v_strike/{market}/{currency}/{maturity}"
    ),
    botCommandOptions = BotCommandOptions(
        "volume",
        "Get the volume breakdown"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val subcommand = event.subcommandName
        val eb = EmbedBuilder()

        if (subcommand == "perpfuture") {
            val currency = event.getOption("currency")!!.asString.uppercase()
            val type = event.getOption("type")!!.asString.uppercase()
            val fileUpload = generatePerpAndFuture(eb, currency, type)

            return CommandResponse(isOnlyEphemeral, fileUpload, eb.build())
        } else {
            val market = event.getOption("market")!!.asString.uppercase()
            val currency = event.getOption("currency")!!.asString.uppercase()
            val maturity = event.getOption("maturity")?.asString

            generateOption(eb, market, currency, maturity)

            return CommandResponse(isOnlyEphemeral, eb.build())
        }
    }

    override fun updateCommand(jda: JDA) {
        val optionsSubcommand = SubcommandData("options", "Options")
            .addOption(OptionType.STRING, "market", "The exchange to view", true, true)
            .addOption(OptionType.STRING, "currency", "The underlying currency", true, true)
            .addOption(OptionType.STRING, "maturity", "The maturity of the option", false, true)
        val perpAndFutureSubcommand = SubcommandData("perpfuture", "Perpetual and Future")
            .addOption(OptionType.STRING, "currency", "The currency", true, true)
            .addOption(OptionType.STRING, "type", "The type of exchange", true, true)
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addSubcommands(optionsSubcommand, perpAndFutureSubcommand)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun generatePerpAndFuture(eb: EmbedBuilder, currency: String, type: String): FileUpload {
        val volumeBreakdown = laevitasService.futuresVolumeBreakdown(currency, type)
        val date = LaevitasDataHandler.getUTCTimeFromMilli(volumeBreakdown.date * 1000).plus(" UTC")
        val data = ArrayList<Pair<String, List<Pair<String, Double>>>>()
        val futureData = volumeBreakdown.data.future?.usd?.map { it.key to it.value / 1000000 } ?: emptyList()
        val perpetualData = volumeBreakdown.data.perpetual.usd.map { it.key to it.value / 1000000 }

        data.add(Pair("FUTURE", futureData))
        data.add(Pair("PERPETUAL", perpetualData))

        val chartBytes = ChartHandler.generateStackedBarGraph("Volume Breakdown of $currency", "Exchanges", "Volume (USD per $1M)", data)

        eb.setTitle("Volume Breakdown of $currency")
        eb.setFooter("Last Updated: $date")
        eb.setImage("attachment://volumeBreakdown.png")

        return FileUpload.fromData(chartBytes, "volumeBreakdown.png")
    }

    private fun generateOption(eb: EmbedBuilder, market: String, currency: String, maturity: String?) {
        val list = if (maturity != null) {
            val oiVolume = laevitasService.optionsVolumeByStrike(market, currency, maturity)
            val date = LaevitasDataHandler.getUTCTimeFromMilli(oiVolume.date * 1000).plus(" UTC")

            eb.setFooter("Last Updated: $date")

            oiVolume.data
        } else {
            val oiVolume = laevitasService.optionsVolumeByStrike(market, currency)
            val date = LaevitasDataHandler.getUTCTimeFromMilli(oiVolume.date * 1000).plus(" UTC")

            eb.setFooter("Last Updated: $date")

            oiVolume.data
        }
        val sortedAndFilteredList = list
            .sortedBy { it.callNotional + it.putNotional }
            .filter { it.callNotional + it.putNotional > 0 }
            .take(30)
        val nf = NumberFormat.getInstance()

        eb.setTitle("Volume for $market - ${currency.uppercase()}")
        eb.setDescription("**Note**: Some data might be omitted\n\nData is presented as: `Volume (Notional)`")

        sortedAndFilteredList
            .sortedBy { it.strike }
            .forEach { item ->
                eb.addField(
                    "$${item.strike}",
                    "- Call: ${nf.format(item.callVolume)} (${nf.format(item.callNotional)})\n- Put: ${nf.format(item.putVolume)} (${nf.format(item.putNotional)})",
                    true
                )
            }
    }

}