import run.smt.ktest.rest.url.UrlProvider

object Url : UrlProvider {
    val gateway = "gateway"
    val customer = "customer"
    val search = "search"
}

class Customer {
    var fullName: String? = null
    var firstName: String? = null
}

