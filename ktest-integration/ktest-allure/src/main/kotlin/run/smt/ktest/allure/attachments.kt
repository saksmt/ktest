package run.smt.ktest.allure

import java.io.InputStream

fun attach(attachment: Pair<Any, String>) = attach(name = attachment.second, value = attachment.first)
fun attach(name: String, value: Any, type: String = "", extension: String = "") {
    fun makeAttach(value: ByteArray) {
        allure.addAttachment(name, type, extension, value)
    }
    when (value) {
        is String -> makeAttach(value.toByteArray(Charsets.UTF_8))
        is ByteArray -> makeAttach(value)
        is InputStream -> allure.addAttachment(name, type, extension, value)
        else -> makeAttach(value.toString().toByteArray(Charsets.UTF_8))
    }
}
