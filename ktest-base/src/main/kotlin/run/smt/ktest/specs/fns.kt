package run.smt.ktest.specs

internal fun sanitizeSpecName(name: String) = name.replace("(", " ").replace(")", " ")