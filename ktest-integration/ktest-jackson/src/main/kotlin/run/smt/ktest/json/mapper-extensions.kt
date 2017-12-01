package run.smt.ktest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL

inline fun <reified T> ObjectMapper.readValue(stream: InputStream) = readValue(stream, T::class.java)
inline fun <reified T> ObjectMapper.readValue(file: File) = readValue(file, T::class.java)
inline fun <reified T> ObjectMapper.readValue(bytes: ByteArray) = readValue(bytes, T::class.java)
inline fun <reified T> ObjectMapper.readValue(string: String) = readValue(string, T::class.java)
inline fun <reified T> ObjectMapper.readValue(jp: JsonParser) = readValue(jp, T::class.java)
inline fun <reified T> ObjectMapper.readValue(reader: Reader) = readValue(reader, T::class.java)
inline fun <reified T> ObjectMapper.readValue(url: URL) = readValue(url, T::class.java)
