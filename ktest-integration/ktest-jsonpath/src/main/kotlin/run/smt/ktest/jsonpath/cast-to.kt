package run.smt.ktest.jsonpath

import com.fasterxml.jackson.databind.JavaType
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.TypeRef
import run.smt.ktest.json.*
import kotlin.reflect.KClass

inline fun <reified T : Any> DocumentContext.castTo() = this castTo T::class
infix fun <T : Any> DocumentContext.castTo(clazz: KClass<T>) = createJsonNode(json()) mapTo clazz
infix fun <T : Any> DocumentContext.castTo(type: TypeRef<T>) = read("$", type)
infix fun <T : Any> DocumentContext.castTo(type: JavaType): T = createJsonNode(json()) mapTo type
infix fun <T : Any> DocumentContext.castTo(typeDSL: TypeDSL<T>): T = this castTo type(typeDSL)
