package dev.alphaserpentis.bots.laevitasmarketdata.exceptions

import dev.alphaserpentis.bots.laevitasmarketdata.data.api.Error

class LaevitasHttpException(error: Error) : RuntimeException()