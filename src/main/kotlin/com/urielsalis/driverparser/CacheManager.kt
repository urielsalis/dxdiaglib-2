package com.urielsalis.driverparser

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.urielsalis.driverparser.model.Tag
import java.io.File
import java.time.Instant

object CacheManager {
    lateinit var cacheData: com.urielsalis.driverparser.CacheData

    init {
        com.urielsalis.driverparser.CacheManager.load()
    }

    fun addDriverUrl(match: String, tag: Tag, driver: String)  {
        com.urielsalis.driverparser.CacheManager.cacheData.driverData.putIfAbsent(tag, mutableMapOf())
        com.urielsalis.driverparser.CacheManager.cacheData.driverData[tag]!![match] = driver
        com.urielsalis.driverparser.CacheManager.cacheData.lastUpdated[tag] = Instant.now().toEpochMilli()
        com.urielsalis.driverparser.CacheManager.save()
    }
    fun addDriverPreProcessedData(match: String, tag: Tag, data: String) {
        com.urielsalis.driverparser.CacheManager.cacheData.preProcessedData.putIfAbsent(tag, mutableMapOf())
        com.urielsalis.driverparser.CacheManager.cacheData.preProcessedData[tag]!![match] = data
        com.urielsalis.driverparser.CacheManager.cacheData.lastUpdated[tag] = Instant.now().toEpochMilli()
        com.urielsalis.driverparser.CacheManager.save()
    }
    fun getLastUpdated(tag: Tag) = com.urielsalis.driverparser.CacheManager.cacheData.lastUpdated[tag]
    fun cleanup(tag: Tag) {
        com.urielsalis.driverparser.CacheManager.cacheData.driverData.remove(tag)
        com.urielsalis.driverparser.CacheManager.cacheData.preProcessedData.remove(tag)
        com.urielsalis.driverparser.CacheManager.cacheData.lastUpdated.remove(tag)
        com.urielsalis.driverparser.CacheManager.save()
    }
    fun getDriver(match: String, tag: Tag) = com.urielsalis.driverparser.CacheManager.cacheData.driverData[tag]?.filter { it.key.contains(match) }?.entries?.firstOrNull()?.value
    fun getPreProcessedData(match: String, tag: Tag): String? = com.urielsalis.driverparser.CacheManager.cacheData.preProcessedData[tag]?.filter { it.key.contains(match) }?.entries?.firstOrNull()?.value

    fun save() {
        val objectMapper = jacksonObjectMapper()
        val file = File("cache.json")
        file.delete()
        file.createNewFile()
        objectMapper.writeValue(file, com.urielsalis.driverparser.CacheManager.cacheData)
    }
    fun load() {
        val objectMapper = jacksonObjectMapper()
        val file = File("cache.json")
        com.urielsalis.driverparser.CacheManager.cacheData = if(file.exists()) {
            objectMapper.readValue(file)
        } else {
            com.urielsalis.driverparser.CacheData(mutableMapOf(), mutableMapOf(), mutableMapOf())
        }
    }
}

data class CacheData(
    val driverData: MutableMap<Tag, MutableMap<String, String>>,
    val preProcessedData: MutableMap<Tag, MutableMap<String, String>>,
    val lastUpdated: MutableMap<Tag, Long>
)