package com.urielsalis.dxdiaglib.model.postprocessor

import com.urielsalis.dxdiaglib.model.Dxdiag


interface PostProcessor {
    fun process(dxdiag: Dxdiag): Dxdiag
    fun getName(): String
    fun init() {}
}
