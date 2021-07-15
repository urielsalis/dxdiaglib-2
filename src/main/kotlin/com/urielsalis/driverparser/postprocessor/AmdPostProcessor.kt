package com.urielsalis.driverparser.postprocessor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.urielsalis.driverparser.CacheManager
import com.urielsalis.driverparser.model.DriverDownload
import com.urielsalis.driverparser.model.Tag
import com.urielsalis.dxdiaglib.model.extradata.DisplayDevice
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit

class AmdPostProcessor: DriverPostProcessor {
    override fun matchesCondition(manufacturer: String) =
        manufacturer.equals("amd", true) || manufacturer.equals("advanced micro devices, inc.", true)

    override fun findDriver(displayDevice: DisplayDevice, os: String, cacheManager: com.urielsalis.driverparser.CacheManager): DriverDownload? {
        var fullName = displayDevice.fullName!!.replace("AMD ", "").replace("(TM) ", "").replace(" Graphics", "").trim()
        var url = search(fullName, os, cacheManager)
        if (url == null) {
            url = search(fullName, os, cacheManager)
        }
        if (url == null) {
            fullName = fullName.substring(0, fullName.length - 1)
            url = search(fullName, os, cacheManager)
        }
        if (url == null) {
            fullName = fullName.replace("HD ", "")
            url = search(fullName, os, cacheManager)
//            url = downloads.filterKeys { it.contains(fullName, true) && it.contains("ATI") && it.contains("Series") }.values.firstOrNull()
        }
        if (url == null) {
            return null
        }
        cacheManager.addDriverUrl(fullName, Tag.AMD, url)
        return DriverDownload(displayDevice.fullName, os, url)
    }

    private fun search(fullName: String, os: String, cacheManager: com.urielsalis.driverparser.CacheManager): String? {
        val driver = cacheManager.getDriver(fullName, Tag.AMD)
        if(driver != null) { return driver }
        val preProcessedData = cacheManager.getPreProcessedData(fullName, Tag.AMD) ?: return null

        val json = Jsoup
            .connect(preProcessedData)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
            .ignoreContentType(true)
            .execute()
            .body()
        return "https://www.amd.com" + jacksonObjectMapper().readTree(json)["link"].textValue()
    }

    override fun preLoad(cacheManager: com.urielsalis.driverparser.CacheManager) {
        val lastUpdated = cacheManager.getLastUpdated(Tag.AMD)
        if(lastUpdated==null || lastUpdated < (Instant.now().toEpochMilli()-(7*24*60*60*1000))) {
            cacheManager.cleanup(Tag.AMD)
            val doc = Jsoup.connect("https://www.amd.com/en/support").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0").get()
            val autocomplete = doc.getElementById("support_autocomplete")
            autocomplete
                .children()
                .map { it.attr("value") to (it.childNode(0) as TextNode).text() }
                .filter { it.first.isNotEmpty() }
                .forEach { cacheManager.addDriverPreProcessedData(it.second, Tag.AMD, "https://www.amd.com/rest/support_alias/en/${it.first}") }
        }
    }

    override fun getName() = "AMD driver"
}

