package run.smt.ktest.rest.api

interface Debugging {
    var debug: Boolean

    fun enableDebug() {
        debug = true
    }
}
