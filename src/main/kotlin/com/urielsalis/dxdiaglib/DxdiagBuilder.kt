package com.urielsalis.dxdiaglib

import com.urielsalis.dxdiaglib.model.Dxdiag
import com.urielsalis.dxdiaglib.model.postprocessor.PostProcessor
import com.urielsalis.dxdiaglib.parsers.DxdiagParser
import com.urielsalis.dxdiaglib.state.InitialState
import com.urielsalis.dxdiaglib.state.State
import com.urielsalis.dxdiaglib.state.VariableContext
import kotlin.reflect.full.findAnnotation

class DxdiagBuilder {
    var content = ""
    var parsers = mutableListOf<DxdiagParser>()
    var postProcessors = mutableListOf<PostProcessor>()

    fun of(text: String) = apply {
        this.content = text
    }

    fun withParser(parser: DxdiagParser) = apply {
        this.parsers.add(parser)
    }

    fun withPostProcessor(postProcessor: PostProcessor) = apply {
        postProcessor.init()
        postProcessors.add(postProcessor)
    }

    fun parse(): Dxdiag {
        var dxdiag = parseDxdiag(content)

        parsers.forEach {
            dxdiag = it.parse(dxdiag)
        }

        postProcessors.forEach {
            try {
                dxdiag = it.process(dxdiag)
            } catch (e: Exception) {
                //post processor throwed exception, this should be look at but we dont want to block other processors
                e.printStackTrace()
            }
        }

        return dxdiag
    }

    private fun parseDxdiag(dxdiag: String): Dxdiag {
        var currentState: State = InitialState(VariableContext("root"))
        dxdiag.lines().forEach { currentState = currentState.next(it) }
        return Dxdiag(currentState.context.sections, mutableMapOf())
    }

}
