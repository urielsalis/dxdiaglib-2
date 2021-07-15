package com.urielsalis.dxdiaglib.model.extradata

data class Drives(val devices: List<Drive>) : ExtraData
data class Drive(
    val driveLetter: String?,
    val freeSpace: String?,
    val totalSpace: String?,
    val fileSystem: String?,
    val model: String?
) : ExtraData