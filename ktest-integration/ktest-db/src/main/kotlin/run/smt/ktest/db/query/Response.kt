package run.smt.ktest.db.query

/**
 * SQL response that can be represented as either list, map or single entity
 */
interface Response<out T> {
    fun asList(): List<T>
    fun asMap(): Map<String, Any>?
    fun single(): T?
}
