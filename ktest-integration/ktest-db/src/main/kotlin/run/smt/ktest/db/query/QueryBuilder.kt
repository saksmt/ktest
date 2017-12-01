package run.smt.ktest.db.query

interface QueryBuilder {
    /**
     * Map of parameters
     */
    var parameters: MutableMap<String, Any?>
    /**
     * Query string
     */
    var query: String

    /**
     * Add/overwrite parameter, exclusive with `parametersFrom`
     */
    fun parameter(name: String, value: Any?)

    /**
     * Extract parameters from given pojo, exclusive with `parameter`
     */
    fun parametersFrom(pojo: Any)
}
