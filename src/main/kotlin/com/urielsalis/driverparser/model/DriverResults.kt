package com.urielsalis.driverparser.model

import com.urielsalis.dxdiaglib.model.extradata.ExtraData

data class DriverResults(val drivers: Map<String, DriverDownload>) : ExtraData
