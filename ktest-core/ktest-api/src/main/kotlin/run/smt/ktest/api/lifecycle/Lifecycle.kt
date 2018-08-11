package run.smt.ktest.api.lifecycle

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import run.smt.ktest.api.*
import run.smt.ktest.config.config
import run.smt.ktest.config.fallbackTo
import run.smt.ktest.config.get
import run.smt.ktest.util.functional.Try.Try
import run.smt.ktest.util.functional.Try.recover
import run.smt.ktest.util.loader.instantiate
import run.smt.ktest.util.loader.loadClass

/**
 * Responsible for registration of all [[CaseLifecycleListener]]s
 */
object Lifecycle {
    private val log = LoggerFactory.getLogger(Lifecycle::class.java)

    private val listenerFactories =
        config.getObject("kTest.lifecycle.listeners").mapNotNull { (name, it) ->
            val cfg = it.atKey("listener") fallbackTo ConfigFactory.parseMap(mapOf("listener.priority" to 0))
            val listenerFactory = listenerFactoryFor(name, cfg["listener.class"])
            val priority = cfg.getInt("listener.priority")

            priority to listenerFactory
        }.sortedByDescending { it.first }.map { it.second }

    private fun listenerFactoryFor(name: String, className: String): Try<((BaseSpec) -> Try<CaseLifecycleListener>)> {
        return loadClass<CaseLifecycleListener>(className)
            .recover {
                log.error("Failed to load class for listener named \"$name\"... Used class name: $className", it);
                throw it
            }
            .map { { spec: BaseSpec ->
                Try.of { instantiate<CaseLifecycleListener>(spec)(it) }
                    .recover {
                        log.error("No valid constructor found for listener named \"$name\" of type \"$className\". " +
                            "Valid constructors are: default (zero arguments), constructor with single argument of " +
                            "(sub)type ${BaseSpec::class.qualifiedName}", it)
                        throw it
                    }
            } }
    }

    fun createNotifierFor(spec: BaseSpec): LifecycleNotifier {
        return LifecycleNotifier(listenerFactories.mapNotNull { it.flatMap { it(spec) }.value })
    }
}

