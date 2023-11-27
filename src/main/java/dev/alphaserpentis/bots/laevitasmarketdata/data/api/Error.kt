package dev.alphaserpentis.bots.laevitasmarketdata.data.api

data class Error(
    val statusCode: Int,
    val message: List<String>,
    val error: String
)
