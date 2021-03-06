package com.urielsalis.dxdiaglib.model.extradata

data class DisplayDevices(val devices: List<DisplayDevice>) : ExtraData
data class DisplayDevice(
    val fullName: String?,
    val manufacturer: String? = null,
    val chipType: String? = null,
    val dedicatedMemory: String? = null,
    val version: String? = null,
    val date: String? = null,
    val vendorID: String? = null,
    val deviceID: String? = null,
    val subsystemID: String? = null,
    val driverVersion: String? = null,
    val driverDate: String? = null,
    val monitorCurrentMode: String? = null,
    val monitorNativeMode: String? = null
) : ExtraData
