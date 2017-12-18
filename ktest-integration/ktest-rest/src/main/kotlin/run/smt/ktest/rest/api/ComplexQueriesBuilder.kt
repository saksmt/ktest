package run.smt.ktest.rest.api

import io.restassured.specification.RequestSpecification

/**
 * Standard rest assured methods for REST DSL
 */
interface ComplexQueriesBuilder {
    fun given(vararg parameters: RequestElement) = request(parameters.asSequence())

    fun request(vararg parameters: RequestElement) = request(parameters.asSequence())

    fun request(parameters: Sequence<RequestElement>): RequestSpecification
}
