package run.smt.ktest.db.query

import kotlin.reflect.KClass

interface RespondingQuery<T : Any> {
    /**
     * Class to map result to
     */
    var mapTo: KClass<T>

    fun mapTo(clazz: KClass<T>)
}
