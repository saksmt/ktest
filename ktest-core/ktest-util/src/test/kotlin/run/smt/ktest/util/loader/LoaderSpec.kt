package run.smt.ktest.util.loader

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec
import run.smt.ktest.util.functional.Try.Failure
import run.smt.ktest.util.functional.Try.Success

interface Plugin

class NoArgs : Plugin
class TwoArgs(firstDep: String, secondDep: Int) : Plugin
object ObjectInstance : Plugin

class LoaderSpec : ShouldSpec({
    "load" {
        should("handle object instances") {
            load<Plugin>(ObjectInstance::class.qualifiedName!!) shouldEqual Success(ObjectInstance)
        }

        "with allowance of extra args" {
            should("handle exact injections") {
                load<Plugin>(TwoArgs::class.qualifiedName!!, "asdf", 1) should beInstanceOf(Success::class)
                load<Plugin>(NoArgs::class.qualifiedName!!) should beInstanceOf(Success::class)
            }

            should("ignore extra args") {
                load<Plugin>(NoArgs::class.qualifiedName!!, 1, 2, 3, 4) should beInstanceOf(Success::class)
                load<Plugin>(TwoArgs::class.qualifiedName!!, "df", 1, "", "") should beInstanceOf(Success::class)
            }
        }

        "with strict injection" {
            should("handle exact injections") {
                load<Plugin>(TwoArgs::class.qualifiedName!!, InjectionMode.STRICT, "asdf", 1) should beInstanceOf(Success::class)
                load<Plugin>(NoArgs::class.qualifiedName!!, InjectionMode.STRICT) should beInstanceOf(Success::class)
            }

            should("fail on extra args") {
                load<Plugin>(NoArgs::class.qualifiedName!!, InjectionMode.STRICT, 1, 2, 3, 4) should beInstanceOf(Failure::class)
                load<Plugin>(TwoArgs::class.qualifiedName!!, InjectionMode.STRICT, "df", 1, "", "") should beInstanceOf(Failure::class)
            }
        }
    }
})
