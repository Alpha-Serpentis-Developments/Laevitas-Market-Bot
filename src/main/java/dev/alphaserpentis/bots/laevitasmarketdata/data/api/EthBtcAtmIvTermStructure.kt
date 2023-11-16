package dev.alphaserpentis.bots.laevitasmarketdata.data.api

data class EthBtcAtmIvTermStructure(
    val date: Long,
    val data: Map<String, List<EthBtcAtmIvTermStructureEntry>>
) {
    data class EthBtcAtmIvTermStructureEntry(
        val maturity: Double,
        val iv: Double
    )
}