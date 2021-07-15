package com.urielsalis.driverparser.postprocessor

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.urielsalis.driverparser.CacheManager
import com.urielsalis.driverparser.model.DriverDownload
import com.urielsalis.driverparser.model.Tag
import com.urielsalis.driverparser.model.nvidia.LookupValue
import com.urielsalis.driverparser.model.nvidia.LookupValueSearch
import com.urielsalis.dxdiaglib.model.extradata.DisplayDevice
import org.jsoup.Jsoup
import java.net.URL
import java.time.Instant

class NvidiaPostProcessor : DriverPostProcessor {
    override fun matchesCondition(manufacturer: String) = manufacturer.contains("nvidia", true)

    override fun findDriver(displayDevice: DisplayDevice, os: String, cacheManager: com.urielsalis.driverparser.CacheManager): DriverDownload? {
        val url = search(displayDevice.fullName!!.replace("NVIDIA", "").trim(), os, cacheManager) ?: return null
        return DriverDownload(displayDevice.fullName, os, url)
    }

    private fun search(fullName: String, os: String, cacheManager: com.urielsalis.driverparser.CacheManager): String? {
        val preProcessedData = cacheManager.getPreProcessedData(fullName, Tag.NVIDIA) ?: return null
        val osTable = cacheManager.getPreProcessedData(preProcessedData, Tag.NVIDIA) ?: return null

        val osParsed = parseOS(os)

        if(osParsed.contains("Unknown")) {
            return null
        }

        val osTableParsed = osTable
            .replace("{", "").replace("}", "")
            .split(",")
            .map {
                val parts = it.split("=")
                parts.first() to parts.last()
            }
            .toMap()

        val driver = Jsoup
            .connect("https://www.nvidia.com/Download/processDriver.aspx?pfid=${preProcessedData}&osid=${osTableParsed[osParsed]}&lang=en-us")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
            .ignoreContentType(true)
            .execute()
            .body()

        return if(driver.contains("html")) {
            "Error with nvidia, go to https://www.nvidia.com/Download/processDriver.aspx?pfid=${preProcessedData}&osid=${osTableParsed[osParsed]}&lang=en-us"
        } else {
            driver
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
            os.contains("64-bit") -> "Windows $name 64-bit"
            os.contains("32-bit") -> "Windows $name 32-bit"
            name == "XP" -> "Windows XP"
            else -> ""
        }
    }

    override fun preLoad(cacheManager: com.urielsalis.driverparser.CacheManager) {
        val lastUpdated = cacheManager.getLastUpdated(Tag.NVIDIA)
        if(lastUpdated==null || lastUpdated < (Instant.now().toEpochMilli()-(31L*24*60*60*1000))) {
            cacheManager.cleanup(Tag.NVIDIA)

            //First step: Reading types(Geforce/Quadro/etc)
            "https://www.nvidia.com/Download/API/lookupValueSearch.aspx?TypeID=1".parseAndLoop {
                //Second step: Reading series
                "https://www.nvidia.com/Download/API/lookupValueSearch.aspx?TypeID=2&ParentID=${it.value}".parseAndLoop {
                    //Third step: Get OS supported by GPU
                    val supportedOS =
                        "https://www.nvidia.com/Download/API/lookupValueSearch.aspx?TypeID=4&ParentID=${it.value}"
                            .parse()
                            .map { Pair(it.name, it.value) }
                            .toMap()
                    //Fourth step: Getting specific GPUs in series
                    "https://www.nvidia.com/Download/API/lookupValueSearch.aspx?TypeID=3&ParentID=${it.value}".parseAndLoop {
                        //Last step: Get download link
                        cacheManager.addDriverPreProcessedData(it.value.toString(), Tag.NVIDIA, supportedOS.toString())
                        cacheManager.addDriverPreProcessedData(it.name, Tag.NVIDIA, it.value.toString())
                    }
                }
            }
        }
    }

    private fun String.parse(): List<LookupValue> {
        val xmlMapper = XmlMapper()
        return try {
            val xml = Jsoup
                .connect(this)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
                .ignoreContentType(true)
                .execute()
                .body()
            val lookupValueSearch = xmlMapper.readValue(xml, LookupValueSearch::class.java)
            lookupValueSearch.lookupValues
        } catch (e: MismatchedInputException) {
            emptyList()
        }
    }

    private fun String.parseAndLoop(function: (LookupValue) -> Unit) {
        this.parse().forEach { function(it) }
    }


    override fun getName() = "Nvidia driver"
}

