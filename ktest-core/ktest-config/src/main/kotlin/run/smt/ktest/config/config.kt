package run.smt.ktest.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigFactory.systemEnvironment
import com.typesafe.config.ConfigFactory.systemProperties
import run.smt.ktest.util.reflection.canBeAssignedTo
import run.smt.ktest.util.resource.resourceExists
import java.io.File
import java.time.Duration
import kotlin.reflect.KClass

private const val CONFIG_PROPERTY_NAME = "configFile"
private const val DEFAULT_FILE = "tests.conf"

private val unresolvedConfig: Config by lazy {
  val loadedConfig = loadConfig() ?: ConfigFactory.empty()

  (systemEnvironment()
      fallbackTo systemProperties()
      fallbackTo loadedConfig
      fallbackTo DEFAULT_FILE
      fallbackTo "defaults.conf"
      fallbackTo "default.conf")
}

val config: Config by lazy {
  unresolvedConfig.resolveWith(unresolvedConfig)
}

private fun loadConfig(): Config? {
  val configName = System.getProperty(CONFIG_PROPERTY_NAME) ?: return null
  val configFile = File(configName)

  return when {
    configFile.exists() -> ConfigFactory.parseFile(configFile)
    configName.resourceExists() -> ConfigFactory.parseResources(configName)
    else -> null
  }
}

infix fun Config.fallbackTo(fallback: Config) = withFallback(fallback)!!
infix fun Config.fallbackTo(fallbackConfigName: String) = withFallback(ConfigFactory.parseResources(fallbackConfigName))!!

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Config.getAt(path: String, clazz: KClass<T>) = when {
  String::class canBeAssignedTo clazz -> getString(path) as T
  Int::class canBeAssignedTo clazz -> getInt(path) as T
  Double::class canBeAssignedTo clazz -> getDouble(path) as T
  List::class canBeAssignedTo clazz -> getAnyRefList(path) as T
  Config::class canBeAssignedTo clazz -> getConfig(path) as T
  Duration::class canBeAssignedTo clazz -> getDuration(path) as T
  Boolean::class canBeAssignedTo clazz -> getBoolean(path) as T
  else -> throw IllegalArgumentException("Config can not contain value of type ${clazz.qualifiedName} at path $path")
}

fun <T : Any> Config.get(path: String, clazz: KClass<T>) = getAt(path, clazz)

inline infix operator fun <reified T : Any> Config.get(path: String) = get(path, T::class)
