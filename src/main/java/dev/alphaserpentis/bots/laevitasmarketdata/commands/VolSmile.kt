package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.bots.laevitasmarketdata.data.api.SkewData
import dev.alphaserpentis.bots.laevitasmarketdata.data.api.SkewDataAllMaturities
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

open class VolSmile(laevitasService: LaevitasService) : LaevitasCommand(
    laevitasService,
    associatedPaths = mapOf(
        "volsmile" to "/analytics/options/model_charts/skew/{currency}/{maturity}/{type}"
    ),
    botCommandOptions = BotCommandOptions(
        "volsmile",
        "Get the skew data for a given currency and maturity"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()
            .setTitle("Volatility Smile")
        val currency = event.getOption("currency")!!.asString.uppercase()
        val maturity = event.getOption("maturity")!!.asString
        val type = event.getOption("type")!!.asString
        val fileUpload = FileUpload.fromData(generateEmbed(eb, currency, maturity, type), "volsmile.png")

        return CommandResponse(isOnlyEphemeral, fileUpload, eb.build())
    }

    override fun updateCommand(jda: JDA) {
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addOption(OptionType.STRING, "currency", "The currency", true, true)
            .addOption(OptionType.STRING, "maturity", "The maturity", true, true)
            .addOption(OptionType.STRING, "type", "Type to view", true, true)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun generateEmbed(eb: EmbedBuilder, currency: String, maturity: String, type: String): ByteArray {
        if (maturity == "all") {
            val volSmile = laevitasService.skewData(currency, type)
            val chart = generateAllMaturitiesChart(volSmile, currency, type)
            val date = LaevitasDataHandler.getUTCTimeFromMilli(volSmile.date * 1000).plus(" UTC")

            eb.setImage("attachment://volsmile.png")
            eb.setFooter("Last Updated: $date")

            return chart
        } else {
            val volSmile = laevitasService.skewData(currency, maturity, type)
            val chart = generateSpecificMaturityChart(volSmile, currency, maturity, type)
            val date = LaevitasDataHandler.getUTCTimeFromMilli(volSmile.date * 1000).plus(" UTC")

            eb.setImage("attachment://volsmile.png")
            eb.setFooter("Last Updated: $date")

            return chart
        }
    }

    private fun generateAllMaturitiesChart(volSmile: SkewDataAllMaturities, currency: String, type: String): ByteArray {
        val pairedData = HashMap<String, List<Pair<String, Double>>>()

        volSmile.data.forEach {
            val key = it.key
            val value = it.value

            pairedData[key] = ArrayList()

            value.forEach { entry ->
                (pairedData[key] as ArrayList).add(Pair(entry.sharedValue.toString(), entry.iv))
            }
        }

        return ChartHandler.generateXYSeriesChart(
            "Vol Smile of $currency ($type)",
            type,
            "IV",
            pairedData
        )
    }

    private fun generateSpecificMaturityChart(volSmile: SkewData, currency: String, maturity: String, type: String): ByteArray {
        val pairedData = ArrayList<Pair<String, Double>>()

        volSmile.data.forEach {
            pairedData.add(Pair(it.sharedValue.toString(), it.iv))
        }

        return ChartHandler.generateXYSeriesChart(
            "Vol Smile of $currency Expiring @ $maturity ($type)",
            type,
            "IV",
            pairedData
        )
    }
}