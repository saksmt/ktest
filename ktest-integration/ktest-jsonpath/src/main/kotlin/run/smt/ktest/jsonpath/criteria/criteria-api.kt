package run.smt.ktest.jsonpath.criteria

import com.jayway.jsonpath.Criteria
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.Filter
import com.jayway.jsonpath.Predicate
import java.util.regex.Pattern
import kotlin.reflect.KClass

fun filter(criteriaBuilder: CriteriaDSL): Filter {
    return Filter.filter(CriteriaBuilder.criteriaBuilder())
}

fun filterNot(criteriaBuilder: CriteriaDSL) = filter(criteriaBuilder).negate()

infix fun Filter.or(other: Predicate) = this.or(other)
infix fun Filter.and(other: Predicate) = this.and(other)
fun Filter.negate() = Filter.filter { !apply(it) }

typealias CriteriaDSL = CriteriaBuilder.() -> Predicate
object CriteriaBuilder {
    infix fun String.and(v: String): Criteria = Criteria.where(this) and v
    infix fun Criteria.and(v: String): Criteria = this.and(v)

    infix fun String.eq(v: Any): Criteria = Criteria.where(this) eq v
    infix fun Criteria.eq(v: Any): Criteria = this.eq(v)

    infix fun String.ne(v: Any): Criteria = Criteria.where(this) ne v
    infix fun Criteria.ne(v: Any): Criteria = this.ne(v)

    infix fun String.lt(v: Any): Criteria = Criteria.where(this) lt v
    infix fun Criteria.lt(v: Any): Criteria = this.lt(v)

    infix fun String.lte(v: Any): Criteria = Criteria.where(this) lte v
    infix fun Criteria.lte(v: Any): Criteria = this.lte(v)

    infix fun String.gt(v: Any): Criteria = Criteria.where(this) gt v
    infix fun Criteria.gt(v: Any): Criteria = this.gt(v)

    infix fun String.gte(v: Any): Criteria = Criteria.where(this) gte v
    infix fun Criteria.gte(v: Any): Criteria = this.gte(v)

    infix fun String.like(v: String): Criteria = Criteria.where(this) like v
    infix fun Criteria.like(v: String): Criteria = this.regex(v.toPattern())

    infix fun String.like(v: Pattern): Criteria = Criteria.where(this) like v
    infix fun Criteria.like(v: Pattern): Criteria = this.regex(v)

    infix fun String.listedIn(v: Collection<*>) = Criteria.where(this) listedIn v
    infix fun Criteria.listedIn(v: Collection<*>) = this.`in`(v)

    infix fun String.notListedIn(v: Collection<*>) = Criteria.where(this) notListedIn v
    infix fun Criteria.notListedIn(v: Collection<*>) = this.nin(v)

    infix fun String.contains(v: Any): Criteria = Criteria.where(this) contains v
    infix fun Criteria.contains(v: Any): Criteria = this.contains(v)

    infix fun String.all(v: Collection<Any>): Criteria = Criteria.where(this) all v
    infix fun Criteria.all(v: Collection<Any>): Criteria = this.all(v)

    infix fun String.hasSize(v: Int): Criteria = Criteria.where(this) hasSize v
    infix fun Criteria.hasSize(v: Int): Criteria = this.size(v)

    // May fail due to Kotlin's `KClass::javaObjectType` specifics
    infix fun String.isOfType(v: KClass<*>): Criteria = Criteria.where(this) isOfType v
    infix fun Criteria.isOfType(v: KClass<*>): Criteria = this.type(v.java)

    infix fun String.isOfType(v: Class<*>): Criteria = Criteria.where(this) isOfType v
    infix fun Criteria.isOfType(v: Class<*>): Criteria = this.type(v)

    infix fun String.exists(v: Boolean): Criteria = Criteria.where(this) exists v
    infix fun Criteria.exists(v: Boolean): Criteria = this.exists(v)

    fun String.exists(): Criteria = Criteria.where(this).exists()
    fun Criteria.exists(): Criteria = this.exists(true)

    infix fun String.empty(v: Boolean): Criteria = Criteria.where(this) empty v
    infix fun Criteria.empty(v: Boolean): Criteria = this.empty(v)

    fun String.empty(): Criteria = Criteria.where(this).empty()
    fun Criteria.empty(): Criteria = this.empty(true)

    fun String.nonEmpty(): Criteria = Criteria.where(this).nonEmpty()
    fun Criteria.nonEmpty(): Criteria = this.empty(false)

    infix fun String.matches(v: Predicate): Criteria = Criteria.where(this) matches v
    infix fun Criteria.matches(v: Predicate): Criteria = this.matches(v)

}
