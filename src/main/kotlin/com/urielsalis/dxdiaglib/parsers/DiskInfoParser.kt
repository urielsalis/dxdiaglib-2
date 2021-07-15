package com.urielsalis.dxdiaglib.parsers

import com.urielsalis.dxdiaglib.model.Dxdiag
import com.urielsalis.dxdiaglib.model.extradata.*

class DiskInfoParser : DxdiagParser {
    override fun parse(dxdiag: Dxdiag): Dxdiag {
        val section = (dxdiag["Disk & DVD/CD-ROM Drives"] as? Section) ?: return dxdiag

        val devices = section.subSections.map {
            Drive(
                it["Drive"],
                it["Free Space"],
                it["Total Space"],
                it["File System"],
                it["Model"]
            )
        }
        dxdiag.extras["Disks"] = Drives(devices)
        return dxdiag
    }
}
