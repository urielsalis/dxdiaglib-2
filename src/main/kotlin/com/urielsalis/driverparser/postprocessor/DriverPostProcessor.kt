package com.urielsalis.driverparser.postprocessor

import com.urielsalis.driverparser.CacheManager
import com.urielsalis.driverparser.model.DriverDownload
import com.urielsalis.driverparser.model.DriverResults
import com.urielsalis.dxdiaglib.model.Dxdiag
import com.urielsalis.dxdiaglib.model.extradata.DisplayDevice
import com.urielsalis.dxdiaglib.model.extradata.DisplayDevices
import com.urielsalis.dxdiaglib.model.extradata.SystemInfo
import com.urielsalis.dxdiaglib.model.postprocessor.PostProcessor

interface DriverPostProcessor: PostProcessor {
    override fun process(dxdiag: Dxdiag): Dxdiag {
        val displayDevices = dxdiag["Display Devices"] as DisplayDevices
        val systemInfo = dxdiag["System Information"] as SystemInfo
        val devices = displayDevices.devices.filter { matchesCondition(it.manufacturer!!) }
        if (devices.isEmpty()) {
            return dxdiag
        }

        val drivers: Map<String, DriverDownload> = devices
            .map { it.fullName!! to findDriver(it, systemInfo.os!!, com.urielsalis.driverparser.CacheManager) }
            .filterNot { it.second == null }
            .map { it.first to it.second!! } // Kotlin is stupid and doesn't know that it cant be null
            .toMap()

        if (drivers.isNotEmpty()) {
            dxdiag.extras[getName()] = DriverResults(drivers)
        }
        return dxdiag

    }

    override fun init() = preLoad(com.urielsalis.driverparser.CacheManager)

    fun matchesCondition(manufacturer: String): Boolean
    fun findDriver(displayDevice: DisplayDevice, os: String, cacheManager: com.urielsalis.driverparser.CacheManager): DriverDownload?
    fun preLoad(cacheManager: com.urielsalis.driverparser.CacheManager)
    fun String.toDriverDownload(name: String, os: String) = DriverDownload(name, os, this)
}