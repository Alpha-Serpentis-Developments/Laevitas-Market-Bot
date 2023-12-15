package dev.alphaserpentis.bots.laevitasmarketdata.launcher

import dev.alphaserpentis.bots.laevitasmarketdata.commands.*
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.commands.defaultcommands.About
import dev.alphaserpentis.coffeecore.commands.defaultcommands.Help
import dev.alphaserpentis.coffeecore.commands.defaultcommands.Settings
import dev.alphaserpentis.coffeecore.core.CoffeeCore
import dev.alphaserpentis.coffeecore.core.CoffeeCoreBuilder
import dev.alphaserpentis.coffeecore.data.bot.AboutInformation
import dev.alphaserpentis.coffeecore.data.bot.BotSettings
import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import java.awt.Color

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
        val aboutInfo = AboutInformation(
            """
                Get real time derivatives data from Laevitas!
                
                [**Source Code**](https://github.com/Alpha-Serpentis-Developments/Laevitas-Market-Bot)
                [**Laevitas Discord**](https://discord.com/invite/yaXc4EFFay)
                [**Profile Picture by Aedrian**](https://unsplash.com/photos/a-close-up-of-a-computer-screen-with-numbers-on-it-OKNJX7B-cbc)
            """.trimIndent(),
            null,
            Color.GREEN,
            true,
            true
        )
        val botSettings = BotSettings(
            dotenv["BOT_OWNER_ID"].toLong(),
            dotenv["BOT_SERVER_DATA_PATH"],
            dotenv["UPDATE_COMMANDS"].toBoolean(),
            false,
            aboutInfo
        )
        val builder = CoffeeCoreBuilder<DefaultShardManagerBuilder>()
            .setAdditionalListeners(LaevitasDataHandler())
            .setSettings(botSettings)
            .enableSharding(true)

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
            TopTraded(laevitasService),
            Help(),
            About(),
            Settings()
        )
    }
}
