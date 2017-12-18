package run.smt.ktest.rest.authorization.impl

import com.typesafe.config.Config
import run.smt.ktest.rest.api.RequestBuilder
import run.smt.ktest.rest.api.RequestElement
import run.smt.ktest.rest.authorization.AuthorizationAdapter

class NoopAuthorizationAdapter : AuthorizationAdapter {
    override fun setup(config: Config) {
        // NOOP
    }

    override fun RequestBuilder.enrichRequest(request: Sequence<RequestElement>): Sequence<RequestElement> {
        return request // NOOP
    }
}
