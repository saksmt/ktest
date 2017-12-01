package run.smt.ktest.db.mapping

/**
 * Marks field as SQL column with specified name
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Column(val name: String)
