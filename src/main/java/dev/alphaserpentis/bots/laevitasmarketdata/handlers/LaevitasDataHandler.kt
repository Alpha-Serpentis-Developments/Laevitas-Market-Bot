package dev.alphaserpentis.bots.laevitasmarketdata.handlers

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class LaevitasDataHandler {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(LaevitasDataHandler::class.java)
        private val cachedMaturities: HashSet<String> = HashSet()
        private val cachedMarkets: HashSet<String> = HashSet()
        private val cachedCurrencies: HashSet<String> = HashSet()
        private val cachedStrikes: HashMap<String, List<Double>> = HashMap()
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
                    1,
                    TimeUnit.HOURS
                )
            }
        }

        private fun updateCaches() {
            val catalog = service!!.catalog()
            val maturities = catalog.params.maturities.data.keys
            val currencies = catalog.params.currency.data

            cachedMaturities.addAll(maturities)
            cachedCurrencies.addAll(currencies)

            catalog.params.strikes.data.forEach { (exchange, currencyToStrike) ->
                cachedMarkets.add(exchange)

                currencyToStrike.forEach { (currency, strike) ->
                    val convertedList = strike.map { it.toDouble() }

                    cachedStrikes[currency] = convertedList
                }
            }
        }

        fun getUTCTimeFromMilliseconds(milliseconds: Long): String = DateTimeFormatter
            .ofPattern("HH:mm:ss yyyy-MM-dd")
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(milliseconds))
    }
}
