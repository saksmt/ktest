package run.smt.ktest.allure

import run.smt.ktest.json.dump

interface Log : AutoCloseable {
    /**
     * Writes formatted log message
     */
    fun write(message: String, vararg parameters: Any)

    /**
     * Dumps object to log as prettified JSON
     */
    fun dump(data: Any?)
}

private class AllureLog internal constructor(private val logName: String = "log") : Log {
    private val content = StringBuilder()

    override fun close() {
        attach(content to logName)
    }

    override fun write(message: String, vararg parameters: Any) {
        content.append(
            message.format(*parameters)
        )
    }

    override fun dump(data: Any?) {
        when (data) {
            is String -> write(data)
            is Number -> write(data.toString())
            null -> write("null")
            else -> write(data.dump())
        }
    }
}

// Stub
private object NoopLog : Log {
    override fun write(message: String, vararg parameters: Any) { /* NOOP */
    }

    override fun dump(data: Any?) { /* NOOP */
    }

    override fun close() { /* NOOP */
    }
}

fun logger(): Log = AllureLog()
fun nullLogger(): Log = NoopLog

fun <R> logsTo(logName: String, action: Log.() -> R) = AllureLog(logName).use { it.action() }
