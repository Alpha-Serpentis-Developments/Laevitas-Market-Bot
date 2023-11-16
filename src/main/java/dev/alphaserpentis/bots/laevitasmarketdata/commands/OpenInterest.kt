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
    private val laevitasService: LaevitasService = LaevitasDataHandler.service

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()

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
            .addOption(OptionType.STRING, "market", "The market to get OI for", false, true)
            .addOption(OptionType.STRING, "maturity", "The maturity to get OI for", false)
        val oiChangeSubcommand = SubcommandData(
            "change",
            "View the OI winners and losers for futures of the specified currency"
        )
            .addOption(OptionType.STRING, "currency", "The currency to get OI for", true, true)
            .addOption(OptionType.STRING, "type", "Type of future", true, true)
            .addOption(OptionType.INTEGER, "period", "The period to get OI for", true, true)
        val cmdData = (getJDACommandData(commandType, name, description) as SlashCommandData)
            .addSubcommands(futuresSubcommand, optionsSubcommand)

        jda.upsertCommand(cmdData).queue { cmd -> setGlobalCommandId(cmd.idLong) }
    }
}