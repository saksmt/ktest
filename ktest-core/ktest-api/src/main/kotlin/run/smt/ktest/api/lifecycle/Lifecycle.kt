package run.smt.ktest.api.lifecycle

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import run.smt.ktest.api.*
import run.smt.ktest.config.config
import run.smt.ktest.config.fallbackTo
import run.smt.ktest.config.get
import run.smt.ktest.util.reflection.canBeAssignedTo

object Lifecycle {
    private val log = LoggerFactory.getLogger(Lifecycle::class.java)

    private val listenerFactories =
        config.getObject("kTest.lifecycle.listeners").mapNotNull { (name, it) ->
            val cfg = it.atKey("listener") fallbackTo ConfigFactory.parseMap(mapOf("listener.priority" to 0))
            val listenerFactory = listenerFactoryFor(name, cfg["listener.class"])
            val priority = cfg.getInt("listener.priority")

            listenerFactory?.let { priority to it }
        }.sortedByDescending { it.first }.map { it.second }

    private fun listenerFactoryFor(name: String, className: String): ((BaseSpec) -> CaseLifecycleListener)? {
        @Suppress("UNCHECKED_CAST")
        val clazz = try {
            Class.forName(className)
        } catch (e: Exception) {
            log.error("Failed to find class for listener named \"$name\"... Attempted to use class: $className", e)
            null
        }?.takeIf {
            it canBeAssignedTo CaseLifecycleListener::class
        } as? Class<CaseLifecycleListener>
            ?: run {
                log.error("\"$name\" is not a listener! \"$className\" can not be cast to \"${CaseLifecycleListener::class.qualifiedName}\"")
                return null
            }

        val defaultConstructor = clazz.declaredConstructors.find { it.parameterCount == 0 }

        if (defaultConstructor != null) {
            return {
                defaultConstructor.newInstance() as CaseLifecycleListener
            }
        }

        val specConsumingConstructor = clazz.declaredConstructors
            .filter { it.parameterCount == 1 }
            .find { it.parameterTypes.any { BaseSpec::class canBeAssignedTo it } }

        return specConsumingConstructor?.let { { spec: BaseSpec -> it.newInstance(spec) as CaseLifecycleListener } } ?: run {
            log.error("No valid constructor found for listener named \"$name\" of type \"$className\". Valid constructors are: default (zero arguments), constructor with single argument of (sub)type ${BaseSpec::class.qualifiedName}")
            null
        }
    }

    fun createNotifierFor(spec: BaseSpec): LifecycleNotifier {
        return LifecycleNotifier(listenerFactories.map { it(spec) })
    }
}

