package dev.alphaserpentis.bots.laevitasmarketdata.handlers;

import dev.alphaserpentis.bots.laevitasmarketdata.api.LaevitasService;
import dev.alphaserpentis.bots.laevitasmarketdata.data.api.PerpetualFunding;
import io.reactivex.rxjava3.annotations.NonNull;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class LaevitasDataHandler {
    private static List<String> cachedMarkets = null;
    private static List<String> cachedCurrencies = null;
    private static ScheduledExecutorService executorService;
    public static LaevitasService service;

    public static void init(@NonNull String token) {
        service = new LaevitasService(token);
        executorService = Executors.newScheduledThreadPool(1);
    }
}
