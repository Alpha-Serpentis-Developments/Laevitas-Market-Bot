package dev.alphaserpentis.bots.laevitasmarketdata.commands

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.text.NumberFormat

class TopTraded(laevitasService: LaevitasService) : LaevitasCommand(
    laevitasService,
    associatedPaths = mapOf(
        "toptraded" to "/analytics/options/top_traded_option/{market}/{currency}"
    ),
    botCommandOptions = BotCommandOptions(
        "toptraded",
        "Get the top traded options"
    )
        .setDeferReplies(true)
        .setForgiveRatelimitOnError(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()
        val market = event.getOption("market")!!.asString
        val currency = event.getOption("currency")!!.asString

        generateList(eb, market, currency)

        return CommandResponse(true, eb.build())
    }

    override fun updateCommand(jda: JDA) {
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addOption(OptionType.STRING, "market", "The exchange to view", true, true)
            .addOption(OptionType.STRING, "currency", "The underlying currency the options are based on", true, true)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }

    private fun generateList(eb: EmbedBuilder, market: String, currency: String) {
        val topTraded = laevitasService.topTradedOptions(market, currency)
        val date = LaevitasDataHandler.getUTCTimeFromMilli(topTraded.date * 1000).plus(" UTC")
        val listOfTopTraded = topTraded.data

        eb.setTitle("Top Traded Options for ${capitalizeFirstLetter(market)} - ${capitalizeFirstLetter(currency)}")
        eb.setFooter("Last Updated: $date")

        if (listOfTopTraded.isEmpty()) {
            eb.setDescription("No data found! Try a different market or currency?")
            eb.setColor(0xFF0000)
            return
        } else {
            eb.setColor(0x00FF00)
            val nf = NumberFormat.getInstance()

            for (i in listOfTopTraded.indices) {
                if (i == 10) break

                val option = listOfTopTraded[i]
                eb.addField(
                    "${i + 1}. ${option.instrument}",
                    "  - $${nf.format(option.volumeUsd)} (${nf.format(option.volume)})",
                    false
                )
            }
        }
    }

    private fun capitalizeFirstLetter(string: String) = string.substring(0, 1).uppercase() + string.substring(1)
}