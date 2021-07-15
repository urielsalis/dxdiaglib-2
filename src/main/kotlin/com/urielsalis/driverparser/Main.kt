package com.urielsalis.driverparser

import com.urielsalis.driverparser.model.DriverResults
import com.urielsalis.driverparser.postprocessor.AmdPostProcessor
import com.urielsalis.driverparser.postprocessor.IntelPostProcessor
import com.urielsalis.driverparser.postprocessor.NvidiaPostProcessor
import com.urielsalis.dxdiaglib.DxdiagBuilder
import com.urielsalis.dxdiaglib.model.extradata.DisplayDevices
import com.urielsalis.dxdiaglib.model.extradata.Drives
import com.urielsalis.dxdiaglib.model.extradata.SystemInfo
import com.urielsalis.dxdiaglib.parsers.DiskInfoParser
import com.urielsalis.dxdiaglib.parsers.DisplayDevicesParser
import com.urielsalis.dxdiaglib.parsers.SystemDevicesParser
import com.urielsalis.dxdiaglib.parsers.SystemInfoParser
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import java.io.File
import java.net.URL


fun main(args: Array<String>) {
    args.forEach { parse(it) }
}

fun parse(link: String) {
    val doc = Jsoup.parse(URL(link), 10000)
    val div = (doc.getElementsByClass("code").last().child(0).child(0).childNode(1) as TextNode).wholeText
    try {
        val dxdiag = DxdiagBuilder()
            .of(div)
            .withParser(SystemDevicesParser())
            .withParser(DisplayDevicesParser())
            .withParser(SystemInfoParser())
            .withParser(DiskInfoParser())
            .withPostProcessor(AmdPostProcessor())
            .withPostProcessor(NvidiaPostProcessor())
            .withPostProcessor(IntelPostProcessor())
            .parse()
        val displayDevices = dxdiag["Display Devices"] as DisplayDevices
        val disks = dxdiag["Disks"] as Drives
        val systemInfo = dxdiag["System Information"] as SystemInfo
        val amdInfo = dxdiag["AMD driver"] as DriverResults?
        val nvidiaInfo = dxdiag["Nvidia driver"] as DriverResults?
        val intelInfo = dxdiag["Intel driver"] as DriverResults?
        val drivers = listOfNotNull(amdInfo, nvidiaInfo, intelInfo)
            .map { it.drivers }
            .flatMap { it.entries }
            .groupBy { it.key }
            .mapValues { entry -> entry.value.map { it.value } }

        println("OS: ${systemInfo.os}")
        println("CPU: ${systemInfo.cpu}")
        println("RAM: ${systemInfo.ram}")
        println("OEM: ${systemInfo.oem}")
        println("Report time: ${systemInfo.reportTime}")
        displayDevices.devices.forEach { display ->
            print("Name: ${display.fullName}, Chip: ${display.chipType}")
            print(", Current driver: Version ${display.driverVersion} @ ${display.driverDate} bytes")
            print(", Monitor ${display.monitorCurrentMode} (Native ${display.monitorNativeMode})")
            print(", PCI: VEN_${display.vendorID}/DEV_${display.deviceID}/${display.subsystemID}")
            if(drivers.containsKey(display.fullName)) {
                println(", Drivers: "+drivers[display.fullName]!!.map { it.downloadUrl })
            } else {
                println()
            }
        }
        println()
        disks.devices.forEach { disk ->
            if (disk.fileSystem == null) {
                println("${disk.driveLetter}: - ${disk.model}")
            } else {
                println("${disk.driveLetter}: (${disk.fileSystem}): Free ${disk.freeSpace}/${disk.totalSpace} - ${disk.model}")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        error("Exception occurred: ${e::class.simpleName}")
    }
}
