package com.urielsalis.dxdiaglib.parsers

import com.urielsalis.dxdiaglib.model.Dxdiag
import com.urielsalis.dxdiaglib.model.extradata.DisplayDevice
import com.urielsalis.dxdiaglib.model.extradata.DisplayDevices
import com.urielsalis.dxdiaglib.model.extradata.Section


class DisplayDevicesParser : DxdiagParser {
    override fun parse(dxdiag: Dxdiag): Dxdiag {
        val devicesSections = (dxdiag["Display Devices"] as? Section) ?: return dxdiag

        val devices = devicesSections.subSections.map {
            DisplayDevice(
                it["Card name"],
                it["Manufacturer"],
                it["Chip type"],
                it["Dedicated Memory"],
                it["Driver Version"],
                it["Driver Date/Size"],
                it["Vendor ID"],
                it["Device ID"],
                it["SubSys ID"],
                it["Driver Version"],
                it["Driver Date/Size"],
                it["Current Mode"],
                it["Native Mode"]
            )
        }
        dxdiag.extras["Display Devices"] = DisplayDevices(devices)
        return dxdiag
    }
}
