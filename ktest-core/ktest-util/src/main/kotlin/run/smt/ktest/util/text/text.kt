package run.smt.ktest.util.text

fun String.stripMargin(): String {
    val marginRegex = "\\s+\\|(.*)".toRegex()
    return lines()
        .map { if (it matches marginRegex) it.replace(marginRegex, "$1") else it }
        .joinToString("\n")
}
