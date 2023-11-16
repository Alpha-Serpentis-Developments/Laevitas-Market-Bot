package dev.alphaserpentis.bots.laevitasmarketdata.launcher

import dev.alphaserpentis.bots.laevitasmarketdata.commands.FuturesCurve
import dev.alphaserpentis.bots.laevitasmarketdata.commands.OpenInterest
import dev.alphaserpentis.bots.laevitasmarketdata.commands.PerpFunding
import dev.alphaserpentis.bots.laevitasmarketdata.commands.VolumeBreakdown
import dev.alphaserpentis.bots.laevitasmarketdata.handlers.LaevitasDataHandler
import dev.alphaserpentis.coffeecore.core.CoffeeCore
import dev.alphaserpentis.coffeecore.core.CoffeeCoreBuilder
import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder

object Launcher {
    private var core: CoffeeCore? = null

    @JvmStatic
    fun main(args: Array<String>) {
        val dotenv = Dotenv.load()

        initializeCoffeeCore(dotenv["DISCORD_TOKEN"])
        LaevitasDataHandler.init(dotenv["LAEVITAS_TOKEN"])
        registerCommands()
    }

    private fun initializeCoffeeCore(token: String) {
        val builder = CoffeeCoreBuilder<DefaultShardManagerBuilder>()
        builder.setBuilderConfiguration(CoffeeCoreBuilder.BuilderConfiguration.DEFAULT)
        core = builder.build(token)
    }

    private fun registerCommands() {
        core!!.registerCommands(
            PerpFunding(),
            FuturesCurve(),
            OpenInterest(),
            VolumeBreakdown()
        )
    }
}