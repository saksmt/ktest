package run.smt.ktest.rest.api

interface RestContext {
    operator fun <T> invoke(action: RequestBuilder.() -> T): T
}
