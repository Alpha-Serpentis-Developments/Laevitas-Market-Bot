package dev.alphaserpentis.bots.laevitasmarketdata.launcher

import dev.alphaserpentis.bots.laevitasmarketdata.commands.*
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.core.CoffeeCore
import dev.alphaserpentis.coffeecore.core.CoffeeCoreBuilder
import dev.alphaserpentis.coffeecore.data.bot.BotSettings
import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder

object Launcher {
    var core: CoffeeCore? = null

    @JvmStatic
    fun main(args: Array<String>) {
        val dotenv = Dotenv.load()

        initializeCoffeeCore(dotenv)
        LaevitasDataHandler.init(dotenv["LAEVITAS_TOKEN"])
        registerCommands()
    }

    private fun initializeCoffeeCore(dotenv: Dotenv) {
        val botSettings = BotSettings(
            dotenv["BOT_OWNER_ID"].toLong(),
            dotenv["BOT_SERVER_DATA_PATH"],
            dotenv["UPDATE_COMMANDS"].toBoolean(),
            true
        )
        val builder = CoffeeCoreBuilder<DefaultShardManagerBuilder>()
            .setAdditionalListeners(LaevitasDataHandler())
            .setSettings(botSettings)

        core = builder.build(dotenv["DISCORD_TOKEN"])
    }

    private fun registerCommands() {
        val laevitasService = LaevitasDataHandler.service!!

        core!!.registerCommands(
            PerpFunding(laevitasService),
            FuturesTermStructure(laevitasService),
            OpenInterest(laevitasService),
            Volume(laevitasService),
            GainersAndLosers(laevitasService),
            VolSmile(laevitasService),
            AtmTermStructure(laevitasService),
            VolRun(laevitasService),
            TopTraded(laevitasService)
        )
    }
}
