package com.urielsalis.driverparser.postprocessor

import com.urielsalis.driverparser.CacheManager
import com.urielsalis.driverparser.model.DriverDownload
import com.urielsalis.driverparser.model.Tag
import com.urielsalis.dxdiaglib.model.extradata.DisplayDevice
import org.jsoup.Jsoup
import java.time.Instant

class IntelPostProcessor : DriverPostProcessor {
    override fun matchesCondition(manufacturer: String) = manufacturer.contains("intel", true)

    override fun findDriver(displayDevice: DisplayDevice, os: String, cacheManager: com.urielsalis.driverparser.CacheManager): DriverDownload? {
        val url = search(cleanup(displayDevice.fullName!!), os, cacheManager) ?: return null
        return DriverDownload(displayDevice.fullName, os, url)
    }

    private fun search(fullName: String, os: String, cacheManager: com.urielsalis.driverparser.CacheManager): String? {
        val driver =  cacheManager.getDriver(fullName, Tag.INTEL) ?: return null
        val doc = Jsoup.connect(driver).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0").get()
        val parsedOS = parseOS(os)
        if(doc.getElementsByClass("dc-os").map { it.text() }.any { it.contains(parsedOS) }) {
            return driver
        } else {
            return "$driver - Not compatible with $parsedOS"
        }
    }

    private fun parseOS(os: String): String {
        val name = when {
            os.contains("Windows 10") -> "10"
            os.contains("Windows XP") -> "XP"
            os.contains("Windows Vista") -> "Vista"
            os.contains("Windows 7") -> "7"
            os.contains("Windows 8") -> "8"
            os.contains("Windows 8.1") -> "8.1"
            else -> "Unknown"
        }
        return when {
            os.contains("64-bit") -> "Windows $name, 64-bit"
            os.contains("32-bit") -> "Windows $name, 32-bit"
            name == "XP" -> "Windows XP"
            else -> ""
        }
    }

    override fun preLoad(cacheManager: com.urielsalis.driverparser.CacheManager) {
        val lastUpdated = cacheManager.getLastUpdated(Tag.INTEL)
        if(lastUpdated==null || lastUpdated < (Instant.now().toEpochMilli()-(31L*24*60*60*1000))) {
            cacheManager.cleanup(Tag.INTEL)
            val doc = Jsoup
                .connect("https://www.intel.com/content/www/us/en/support/products/80939/graphics.html")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
                .get()
            val regex = "support/products/[0-9]*/graphics/.*".toRegex()
            val graphics = doc
                .getElementsByTag("a")
                .filter { it.attr("data-wap_ref").matches(regex) }
                .map { cleanup(it.text()) to it.attr("href") }
                .forEach {
                    val epmId = it.second.split("/")[7]
                    cacheManager.addDriverUrl(it.first, Tag.INTEL, "https://downloadcenter.intel.com/product/$epmId")
                }
        }
    }

    private fun cleanup(text: String): String = text
        .replace("Intel", "")
        .replace("Graphics Drivers for", "")
        .replace("Graphics for", "")
        .replace("®", "")
        .replace("™", "")
        .replace("(GMCH)", "")
        .replace("Series", "")
        .replace("Family", "")
        .replace("Chipset", "")
        .replace("(R)", "")
        .replace("\\(.*\\)".toRegex(), "")
        .replace("  ", " ")
        .trim()
    override fun getName() = "Intel driver"
}

