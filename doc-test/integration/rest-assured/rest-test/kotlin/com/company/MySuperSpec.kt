package com.company

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.MetaData
import run.smt.ktest.api.MetaInfoDSL
import run.smt.ktest.api.emptyMetaData
import run.smt.ktest.api.internal.SpecBuilder

abstract class MySuperSpec(init: MySuperSpec.() -> Unit) : BaseSpec() {
    init { init() }

    fun mySuperDuperTest(name: String, metaData: MetaData = emptyMetaData(), body: () -> Unit) = SpecBuilder.case(name, metaData, body)
    fun mySuperDuperTest(name: String, metaInfo: MetaInfoDSL = {}, body: () -> Unit) = SpecBuilder.case(name, metaInfo, body)
}
