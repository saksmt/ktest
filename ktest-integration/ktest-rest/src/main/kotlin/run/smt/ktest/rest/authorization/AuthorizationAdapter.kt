package run.smt.ktest.rest.authorization

import com.typesafe.config.Config
import run.smt.ktest.rest.api.RequestBuilder
import run.smt.ktest.rest.api.RequestElement
import run.smt.ktest.rest.impl.getLogger as _getLogger

interface AuthorizationAdapter {
    /**
     * Guarantied to be called only once per instance
     */
    fun setup(config: Config)
    fun RequestBuilder.enrichRequest(request: Sequence<RequestElement>) : Sequence<RequestElement>

    companion object {
        protected fun getLogger(config: Config) = _getLogger(config)
    }
}
