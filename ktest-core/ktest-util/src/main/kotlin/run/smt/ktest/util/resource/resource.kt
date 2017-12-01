package run.smt.ktest.util.resource

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.NoSuchFileException

private val classLoader: ClassLoader
  get() {
    return Thread.currentThread().contextClassLoader
  }

fun String.load() = classLoader.getResourceAsStream(this)
    ?: throw NoSuchFileException("No file found at resource path \"$this\"")

fun String.loadAsBytes() = load().use { it.readBytes() }

fun String.loadAsString() = String(loadAsBytes(), UTF_8)

fun String.resourceExists() = classLoader.getResource(this) != null

inline fun <R, C : AutoCloseable> C.use(action: (C) -> R): R {
  try {
    return action(this)
  } finally {
    try {
      close()
    } catch (ee: Exception) {
    }
  }
}
