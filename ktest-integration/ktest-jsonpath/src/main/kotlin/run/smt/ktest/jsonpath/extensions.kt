package run.smt.ktest.jsonpath

import com.jayway.jsonpath.*
import run.smt.ktest.jsonpath.criteria.CriteriaDSL
import run.smt.ktest.jsonpath.criteria.filter as cFilter

operator fun DocumentContext.get(query: String, vararg predicate: Predicate): DocumentContext
    = JsonPath.parse(read<Any>(query, *predicate))

operator fun DocumentContext.invoke(filter: Predicate): DocumentContext = this["$..[?]", filter]
infix fun DocumentContext.filter(criteriaBuilder: CriteriaDSL): DocumentContext = this(cFilter(criteriaBuilder))

fun DocumentContext.copy(vararg options: Option) = JsonPath.parse(
    jsonString(),
    Configuration.builder().options(*options).build()
)
