package dev.alphaserpentis.bots.laevitasmarketdata.handlers

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class LaevitasDataHandler {
    companion object {
        private val cachedMarkets: List<String>? = null
        private val cachedCurrencies: List<String>? = null
        private var executorService: ScheduledExecutorService? = null
        var service: LaevitasService? = null

        fun init(token: String) {
            service = LaevitasService(token)
            executorService = Executors.newScheduledThreadPool(1)
        }

        fun getUTCTimeFromMilliseconds(milliseconds: Long): String = DateTimeFormatter
            .ofPattern("HH:mm:ss yyyy-MM-dd")
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(milliseconds))
    }
}
