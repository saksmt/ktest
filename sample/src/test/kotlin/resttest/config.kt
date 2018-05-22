package resttest

import com.typesafe.config.Config
import run.smt.ktest.api.BaseSpec
import run.smt.ktest.config.get
import run.smt.ktest.rest.url.UrlProvider
import run.smt.ktest.rest.url.createUrlDsl
import run.smt.ktest.resttest.createRestTestDSL

class Url(config: Config) : UrlProvider {
    val rest: String = config["rest"]
    val ping: String = config["ping"]
    val simple: String = config["simple"]
    val errorHttpCodes: String = config["error-http-codes"]
    val person: String = config["person"]
    val complexJson: String = config["complex-json"]

    val backend: String = config["backend"]
}

val url = createUrlDsl(::Url)

val BaseSpec.restTest
    get() = createRestTestDSL<Url>(this) {
        urlDsl = url
    }
