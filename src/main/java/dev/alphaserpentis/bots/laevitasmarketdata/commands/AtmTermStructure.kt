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

open class AtmTermStructure(laevitasService: LaevitasService) : LaevitasCommand(
    laevitasService,
    associatedPaths = mapOf(
        "ethbtc" to "/analytics/options/eth-btc_atm_iv_term_structure",
        "specific" to "/analytics/options/model_charts/term_structure_atm/{currency}"
    ),
    botCommandOptions = BotCommandOptions(
        "termstructure",
        "Get the ATM term structure"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()
        val subcommandName = event.subcommandName!!

        when (subcommandName) {
            "ethbtc" -> {
                val file = generateEthBtcTermStructure(eb)

                if (file.isNotEmpty()) {
                    val fileUpload = FileUpload.fromData(file, "atmTermStructure.png")

                    return CommandResponse(isOnlyEphemeral, fileUpload, eb.build())
                }
            }
            "specific" -> {
                val currency = event.getOption("currency")!!.asString
                val isTenor = event.getOption("tenor")?.asBoolean ?: false
                val file = generateSpecificTermStructure(eb, currency, isTenor)

                if (file.isNotEmpty()) {
                    val fileUpload = FileUpload.fromData(
                        file,
                        "atmTermStructure.png"
                    )

                    return CommandResponse(isOnlyEphemeral, fileUpload, eb.build())
                }
            }
        }

        handleInvalidArgument(eb)

        return CommandResponse(isOnlyEphemeral, eb.build())
    }

    override fun updateCommand(jda: JDA) {
        val ethBtcSubcommand = SubcommandData(
            "ethbtc",
            "Get the ETH/BTC ATM term structure"
        )
        val specificSubcommand = SubcommandData(
            "specific",
            "Get the ATM term structure for a specific currency"
        )
            .addOption(OptionType.STRING, "currency", "The currency", true, true)
            .addOption(OptionType.BOOLEAN, "tenor", "Whether to use tenor or expiry", false)
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addSubcommands(ethBtcSubcommand, specificSubcommand)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun generateEthBtcTermStructure(eb: EmbedBuilder): ByteArray {
        val atmTermStructure = laevitasService.ethBtcAtmIvTermStructure()
        val date = LaevitasDataHandler.getUTCTimeFromMilli(atmTermStructure.date * 1000).plus(" UTC")
        val mapToPairedData = HashMap<String, List<Pair<String, Double>>>()

        atmTermStructure.data.forEach {
            val key = it.key
            val value = it.value

            mapToPairedData[key] = ArrayList()

            value.forEach { entry ->
                (mapToPairedData[key] as ArrayList).add(Pair(entry.maturity, entry.iv))
            }

        }

        eb.setTitle("ETH/BTC ATM Term Structure")
        eb.setImage("attachment://atmTermStructure.png")
        eb.setFooter("Last Updated: $date")

        return ChartHandler.generateXYTimedSeriesChart(
            "ETH/BTC ATM Term Structure",
            "Maturity",
            "IV",
            mapToPairedData
        )
    }

    private fun generateSpecificTermStructure(eb: EmbedBuilder, currency: String, isTenor: Boolean): ByteArray {
        val atmTermStructure = laevitasService.atmTermStructure(currency)
        val date = LaevitasDataHandler.getUTCTimeFromMilli(atmTermStructure.date * 1000).plus(" UTC")
        val mapToPairedData = ArrayList<Pair<String, Double>>()

        eb.setTitle("${currency.uppercase()} ATM Term Structure")
        eb.setFooter("Last Updated: $date")
        eb.setImage("attachment://atmTermStructure.png")

        if (isTenor) {
            atmTermStructure.data.tenor.forEach {
                val key = it.ttm.toString()
                val value = it.iv

                mapToPairedData.add(Pair(key, value))
            }

            return ChartHandler.generateXYSeriesChart(
                "${currency.uppercase()} ATM Term Structure",
                "Days",
                "IV",
                mapToPairedData
            )
        } else {
            atmTermStructure.data.expiry.forEach {
                val key = it.expiry
                val value = it.iv

                mapToPairedData.add(Pair(key, value))
            }

            return ChartHandler.generateXYTimedSeriesChart(
                "${currency.uppercase()} ATM Term Structure",
                "Maturity",
                "IV",
                mapToPairedData
            )
        }
    }
}