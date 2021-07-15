package com.urielsalis.dxdiaglib.parsers

import com.urielsalis.dxdiaglib.model.Dxdiag
import com.urielsalis.dxdiaglib.model.extradata.Section
import com.urielsalis.dxdiaglib.model.extradata.SystemDevice
import com.urielsalis.dxdiaglib.model.extradata.SystemDevices

class SystemDevicesParser : DxdiagParser {
    override fun parse(dxdiag: Dxdiag): Dxdiag {
        val systemDevicesSection = (dxdiag["System Devices"] as? Section) ?: return dxdiag

        val systemDevices = systemDevicesSection.subSections.map {
            SystemDevice(it["Name"], it["Device ID"])
        }
        dxdiag.extras["System Devices"] = SystemDevices(systemDevices)
        return dxdiag
    }
}
