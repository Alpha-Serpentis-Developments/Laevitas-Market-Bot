package dev.alphaserpentis.bots.laevitasmarketdata.data.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dev.alphaserpentis.bots.laevitasmarketdata.data.api.Catalog
import java.lang.reflect.Type

class MainParamDeserializer : JsonDeserializer<Catalog.MainParam> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Catalog.MainParam {
        val jsonObject = json!!.asJsonObject

        val maturities = deserializeSubParam(jsonObject.getAsJsonObject("maturities"), context)
        val strikes = deserializeSubParam(jsonObject.getAsJsonObject("strikes"), context)
        val currency = deserializeCurrencyParam(jsonObject.getAsJsonObject("currency"), context)

        return Catalog.MainParam(maturities, strikes, currency)
    }

    private fun deserializeSubParam(json: JsonObject, context: JsonDeserializationContext?): Catalog.MainParam.SubParam {
        val name = json.get("name").asString
        val dataMap = mutableMapOf<String, Map<String, List<String>>>()
        val dataJsonObject = json.getAsJsonObject("data")

        for ((exchange, value) in dataJsonObject.entrySet()) {
            if (value.isJsonObject) {
                val innerJsonObject = value.asJsonObject
                val isMapToList = innerJsonObject.entrySet().all { it.value.isJsonArray }

                if (isMapToList) {
                    val type = object : TypeToken<Map<String, List<String>>>() {}.type
                    val innerMap: Map<String, List<String>> = context!!.deserialize(innerJsonObject, type)
                    dataMap[exchange] = innerMap
                }
            }
        }

        return Catalog.MainParam.SubParam(name, dataMap)
    }

    private fun deserializeCurrencyParam(json: JsonObject, context: JsonDeserializationContext?): Catalog.MainParam.CurrencyParam {
        val name = json.get("name").asString
        val type = object : TypeToken<List<String>>() {}.type
        val data = context!!.deserialize<List<String>>(json.get("data"), type)

        return Catalog.MainParam.CurrencyParam(name, data)
    }
}