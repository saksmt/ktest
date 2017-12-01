package run.smt.ktest.internal.util

internal fun sanitizeSpecName(name: String) = name.replace("(", " ").replace(")", " ")
