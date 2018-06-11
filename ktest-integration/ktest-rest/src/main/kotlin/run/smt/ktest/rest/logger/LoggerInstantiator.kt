package run.smt.ktest.rest.logger

import com.typesafe.config.Config
import run.smt.ktest.config.get
import run.smt.ktest.util.functional.Try.*
import run.smt.ktest.util.loader.loadClass
import run.smt.ktest.util.loader.instantiate
import kotlin.reflect.KClass

class LoggerInstantiator(private val config: Config) {
    companion object {
        private val knownNames = mapOf(
            "allure" to SteppedAllureLogger::class,
            "composite" to CompositeLogger::class
        )
    }

    fun instantiate(): Logger {
        val name: String? = if (config.hasPath("name")) config["name"] else null
        val clazz: Try<KClass<out Logger>> = name?.let { knownNames[it] }?.let { Success(it) }
            ?: loadClass(config.getString("class"))

        return clazz
            .recover {
                throw AssertionError("Invalid configuration for ktest-rest logging. " +
                    "Expected known name (${knownNames.keys.joinToString(", ")}) " +
                    "or FQCN for class implementing Logger interface but given: ${name ?: clazz}")
            }
            .flatMap {
                Try.of {
                    instantiate<Logger>(config)(it)
                }.recover {
                    throw AssertionError("Expected logger ($clazz) constructor to either have no arguments or " +
                        "have exactly one with parameter of type ${Config::class.java}")
                }
            }
            .fold { throw it }
    }
}
