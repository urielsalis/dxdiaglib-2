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
import java.io.File

fun main(args: Array<String>) {
    try {
        val dxdiag = DxdiagBuilder()
            .of(File(args.first()).readText())
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

        println("OS: ${systemInfo.os}, CPU: ${systemInfo.cpu}, RAM: ${systemInfo.ram} OEM: ${systemInfo.oem}")
        println("Report time: ${systemInfo.reportTime}")
        displayDevices.devices.forEach { display ->
            println()
            println("Name: ${display.fullName}, Chip: ${display.chipType}")
            println("PCI Info: VEN ${display.vendorID} DEV ${display.deviceID} SUB ${display.subsystemID}")
            if(drivers.containsKey(display.fullName)) {
                println("Drivers: "+drivers[display.fullName]!!.map { it.downloadUrl })
            } else {
                println()
            }
        }
        println()
        disks.devices.forEach { disk ->
            if (disk.fileSystem == null) {
                println("${disk.driveLetter}: - ${disk.model}")
            } else {
                println("${disk.driveLetter}: (${disk.totalSpace}): Free ${disk.freeSpace}/${disk.totalSpace} - ${disk.model}")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        error("Exception occurred: ${e::class.simpleName}")
    }
}