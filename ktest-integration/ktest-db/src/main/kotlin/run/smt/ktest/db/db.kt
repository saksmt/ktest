package run.smt.ktest.db

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import run.smt.ktest.config.config
import run.smt.ktest.config.fallbackTo
import run.smt.ktest.config.get
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

private val registeredDs: MutableMap<String, HikariDataSource> by lazy {
    ConcurrentHashMap<String, HikariDataSource>().apply {
        Runtime.getRuntime().addShutdownHook(Thread({
            values.forEach(HikariDataSource::close)
        }))
    }
}

private fun getDs(name: String)
    = registeredDs.computeIfAbsent(name, ::createDatasource)

private fun createDatasource(name: String): HikariDataSource {
    if (!config.hasPath("db.$name")) {
        throw IllegalArgumentException("No database found for name \"$name\"")
    }

    val defaultDriver = when {
        !config.hasPath("default-driver") -> ConfigFactory.empty()
        config.getValue("default-driver").valueType() == ConfigValueType.OBJECT -> config["default-driver"]
        config.getValue("default-driver").valueType() == ConfigValueType.STRING -> ConfigFactory.parseMap(mapOf(
            "driver" to config.get<String>("default-driver")
        ))
        else -> throw IllegalArgumentException(
            "Unsupported JDBC driver class: ${config.getValue("default-driver").unwrapped()}"
        )
    }

    val dbConfig = config.get<Config>("db.$name") fallbackTo ConfigFactory.parseMap(mapOf(
        "pool-size" to 5,
        "connection-timeout" to Duration.ofSeconds(15L)
    )) fallbackTo defaultDriver

    return HikariDataSource(HikariConfig().apply {
        driverClassName = dbConfig["driver"]
        jdbcUrl = dbConfig["url"]
        username = dbConfig["username"]
        password = dbConfig["password"]
        maximumPoolSize = dbConfig["pool-size"]
        connectionTimeout = dbConfig.getDuration("connection-timeout", TimeUnit.MILLISECONDS)
    })
}

fun String.db(): DataSource = getDs(this)

fun <T> String.db(executeOnDb: DataSource.() -> T) = db().executeOnDb()
