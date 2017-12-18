package run.smt.ktest.rest.logger

import com.typesafe.config.Config
import run.smt.ktest.config.get
import run.smt.ktest.util.reflection.canBeAssignedTo
import java.lang.reflect.Constructor

class LoggerInstantiator(private val config: Config) {
    companion object {
        private val knownNames = mapOf(
            "allure" to SteppedAllureLogger::class,
            "composite" to CompositeLogger::class
        )
    }

    fun instantiate(): Logger {
        val name: String? = if (config.hasPath("name")) config["name"] else null
        val clazz: Class<*> = name?.let { knownNames[it]?.java } ?: Class.forName(config.getString("class"))

        if (!clazz.canBeAssignedTo(Logger::class)) {
            throw AssertionError("Invalid configuration for ktest-rest logging. " +
                "Expected known name (${knownNames.keys.joinToString(", ")}) " +
                "or FQCN for class implementing Logger interface but given: ${name ?: clazz}")
        }

        val constructor = clazz.constructors
            .sortedByDescending { it.parameterCount }
            .find { it.parameters.isEmpty() || (it.parameters.size == 1 && hasConfigArgument(it)) }
            ?: throw AssertionError("Expected logger ($clazz) constructor to either have no arguments or have exactly one with parameter of type ${Config::class.java}")

        constructor.isAccessible = true

        return if (constructor.parameterCount == 0) {
            constructor.newInstance()
        } else {
            constructor.newInstance(config)
        } as Logger
    }

    private fun hasConfigArgument(it: Constructor<*>) =
        it.parameterTypes.any { it canBeAssignedTo Config::class }
}
