package com.urielsalis.dxdiaglib.parsers

import com.urielsalis.dxdiaglib.model.Dxdiag

interface DxdiagParser {
    fun parse(dxdiag: Dxdiag): Dxdiag
}
