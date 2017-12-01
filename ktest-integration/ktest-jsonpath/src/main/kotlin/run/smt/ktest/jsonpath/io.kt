package run.smt.ktest.jsonpath

import com.jayway.jsonpath.JsonPath
import run.smt.ktest.util.resource.load

fun String.loadAsJsonPath() = JsonPath.parse(load())
