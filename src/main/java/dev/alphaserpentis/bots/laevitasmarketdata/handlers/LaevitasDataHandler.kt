package dev.alphaserpentis.bots.laevitasmarketdata.handlers

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import dev.alphaserpentis.bots.laevitasmarketdata.commands.LaevitasCommand
import dev.alphaserpentis.bots.laevitasmarketdata.launcher.Launcher
import dev.alphaserpentis.coffeecore.handler.api.discord.commands.CommandsHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class LaevitasDataHandler : ListenerAdapter() {
    private var commandsHandler: CommandsHandler? = null
    private val mappingOfCommands: HashMap<String, LaevitasCommand> = HashMap()

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val focusedOption = event.focusedOption
        val subcommandName = event.subcommandName ?: event.name

        if (commandsHandler == null) commandsHandler = Launcher.core!!.commandsHandler

        val command = findCommand(event.name) ?: return
        val override = command.overrideAutocompleteMapping[focusedOption.name]

        if (override != null) {
            event.replyChoiceStrings(override).queue()
            return
        }

        val path = command.associatedPaths[subcommandName] ?: return
        val params = pathToParams[path] ?: return
        val focusedOptionName = useAlternateFocusedName(focusedOption.name)
        val choices = params[focusedOptionName] ?: params[focusedOption.name] ?: return

        event.replyChoiceStrings(obtainMostRelevantOptions(focusedOption.value, choices.toList())).queue()
    }

    private fun useAlternateFocusedName(name: String): String {
        return when (name) {
            "type" -> "option"
            "period" -> "param"
            else -> name
        }
    }

    private fun findCommand(name: String): LaevitasCommand? {
        val command = mappingOfCommands[name]

        if (command == null) {
            val cmd = commandsHandler!!.getCommand(name)

            if (cmd == null) {
                return null
            } else {
                mappingOfCommands[name] = cmd as LaevitasCommand
                return cmd
            }
        }

        return command
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LaevitasDataHandler::class.java)
        private val pathToParams: HashMap<String, HashMap<String, HashSet<String>>> = HashMap()
        private var executorService: ScheduledExecutorService? = null
        private var scheduledFuture: ScheduledFuture<*>? = null
        var service: LaevitasService? = null

        fun init(token: String) {
            service = LaevitasService(token)
            executorService = Executors.newScheduledThreadPool(1)

            run()
        }

        private fun run() {
            if(scheduledFuture == null || scheduledFuture!!.cancel(false)) {
                scheduledFuture = executorService!!.scheduleAtFixedRate(
                    {
                        try {
                            updateCaches()
                        } catch(e: Exception) {
                            LOGGER.error("Error updating caches", e)
                        }
                    },
                    0,
                    3,
                    TimeUnit.HOURS
                )
            }
        }

        private fun updateCaches() {
            val catalog = service!!.catalog()

            catalog.apiList.forEach {
                val path = it.path
                val params = it.params ?: return@forEach

                pathToParams[path] = params
            }
        }

        fun getUTCTimeFromMilli(milliseconds: Long): String = DateTimeFormatter
            .ofPattern("HH:mm:ss yyyy-MM-dd")
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(milliseconds))

        fun obtainMostRelevantOptions(currentInput: String, listOfOptions: List<String>) =
            listOfOptions
                .filter { it.startsWith(currentInput, true) }
                .take(25)
                .sortedBy {
                    try {
                        it.toInt()
                    } catch(ignored: Exception) {
                        0
                    }
                }
    }
}
