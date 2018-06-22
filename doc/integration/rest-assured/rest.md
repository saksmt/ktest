# [kTest](../../README.md) :: [Integration](../README.md) :: [RestAssured](README.md) :: Core Support

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-rest'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-rest</artifactId>
</dependency>
```

## Description

Better RestAssured DSL for Kotlin

### Configuration

```hocon
rest { # there you describe configuration for default rest context

  # base url which will be prefixed for every request
  base-url = "http://myhost:port/path"
  socket-timeout = 30s # optional
  connect-timeout = 10s # optional
  
  authorization.adapter = noop # optional, defaults to "noop", you can implement `AuthorizationAdapter` and place FQCN here
                               # there is also an noop authorization adapter
  
  # Here you can specify which logger will be used to log your requests through configurated context
  logger {
    name = allure # name or FQCN of logger, optional, defaults to allure
  }
  
  # you also can use multiple loggers at once with "composite" logger:
  
  logger {
    name = composite
    loggers = [{
      # format here is the same as for ${rest.logger}
      name = allure
    }]
  }
  
  # if you want to use your custom logging class just use FQCN and don't forget to write it correctly
  # you can use `common.logger.CompositeLogger` or `common.logger.SteppedLogger` as reference
  
  logger {
    class = fully.qualified.class.name.to.YourLogger
  }  

}
```

### Usage

#### Queries

```kotlin
import run.smt.ktest.rest.*
import java.io.InputStream
import org.hamcrest.CoreMatchers.containsString

// Simple queries (will be automatically validated to have 2XX response code)
val myResult: Account = rest {
    val id: String = "/accounts/byId/{id}".GET(queryParam("name", "John Doe"), pathParam("id", 10))
    val account: Account = "/accounts".GET(queryParam("id", id))
    account.phone = "+79999999999"
    "/accounts".PUT(body(account))
}

// Complex queries with validation
val fullResponse = rest {
    request(
        header("Origin", "somesite.com"),
        header("Referer", "someothersite.com"),
        queryParam("s", "Why Kotlin is such a good language?")
    ).expect()
        .statusCode(200)
            .and()
        .body(containsString("Because it's just perfect"))
    .`when`().get("google.com")
        .`as`<InputStream>()
}

val queriesWithDifferentConfig = rest["my-backend"] { // requires "my-backend" configuration section
    val id: String = "/accounts/byId/{id}".GET(queryParam("name", "John Doe"), pathParam("id", 10))
    // ...
}
```

Types you can use for `as` in complex queries and as type arguments for return type of simple queries:

 - `InputStream`
 - `ByteArray`
 - `String`
 - Jackson's `JsonNode`, you can also use `asJsonTree()` instead of `as<JsonNode>()`
 - JsonPath's `DocumentContext`
 - RestAssured's `JsonPath`
 - RestAssured's `XmlPath`
 - RestAssured's `Response`
 - Any POJO type supported by Jackson
 - If provided TypeDSL or Jackson's `JavaType` - almost any type at all
 - As bonus for `JavaType` representing `Pair<Int, T>` you'll get a pair of statusCode and body

##### Note about overriding policy for headers


First of all: headers are overridden by name, i.e. if you passed `header("a", "1"), header("a", "2")` there will be only
one header with one value. Overriding policy obeys following rule: last header will be chosen.
So if you have `header("a", "1"), header("a", "2")` you will get `header("a", "2")` as result. It also applies to combination of
`headers` with `header`:

[//]: # (no_check)
```kotlin
(headers(mapOf("a" to "1", "b" to "b")), header("a", "2")) // == header("a", "2"), header("b", "b")
(header("a", "2"), headers(mapOf("a" to "1", "b" to "b"))) // == header("a", "1"), header("b", "b")
```

Also don't forget that authorization adapter may provide additional headers which will be placed
either before your's (in which case they will be available for overriding) or after your's (which will lead to constant headers)

Don't forget that by RFC 2616 headers are case-insensitive so is ktest-rest. It means that it will
treat `my-header`, `My-Header` and even `mY-hEaDeR` as equal names and will use only one of them 

As for NOOP adapter your headers will have priority since NOOP adapter provides no headers which is obvious...

#### Bonus: URL DSL

```kotlin
import run.smt.ktest.rest.url.UrlProvider
import run.smt.ktest.rest.url.createUrlDsl
import run.smt.ktest.rest.*
import run.smt.ktest.config.get
import run.smt.ktest.util.dsl.*
import com.typesafe.config.Config

// First you need to create your very own URL DSL

class MyUrls(urls: Config) : UrlProvider {
    // Best practice is expose all accepted urls as fields (see motivation below)
    // Also it is good idea to expose parts of urls (pieces of text between "/") and not the full urls at least because it looks just great :) 
    
    val gateway: String = urls["gateway"]
    val backend: String = urls["backend"]
    val customers: String = urls["customers"]
    val search: String = urls["search"]
    val ping: String = urls["ping"]
}

val url = createUrlDsl<MyUrls> { MyUrls(it) }

// assume you have the following in config:
// url {
//   gateway = /api/v2/customer-service
//   backend = /customer-service-backend
//   customers = customers
//   search = search
// }

// now you can use it like the following:

val myUrl1 = url { gateway / customers / search } // = "/api/v2/customer-service/customers/search"
// you can use `param` function to create parameters accepted by our REST component
val myUrl2 = url { backend / customers / param("customerId") } // = "/customer-service-backend/customers/{customerId}"

// combining this with REST simple queries and DSL-utils from ktest-utils we can get following:

fun usage1() {
    rest {
        val result: String = using(url) {
            backend / customers / search
        } execute {
            GET(queryParam("criteria", "value"))
        }
        
        // or we can store URL in some variable and then use it:
        val storedUrl = url {
            backend / customers / search
        }
        val result2: String = using(storedUrl) execute {
            GET(queryParam("criteria", "value"))
        }
    }
}
```

**Motivation to expose all URLs in configuration file:**
Imagine situation where all your URLs on backend reflects all your URLs on gateway except for prefix, so you want to
run your test on backend instead of gateway (or vice versa), then all you'll need to do is to place
`url.gateway = ${url.backend}` (or `url.backend = ${url.gateway}` for "vice versa" effect) into your configuration file

Also it's very handy that you can define `defaults.conf` in your resources that will be loaded automatically, so you'll have:

(defaults.conf):
```hocon
url {
  gateway = /api/v2/customer-service
  backend = /customer-service-backend
  customers = customers
  search = search
  ping = ping
}
```

(configForMyBackend.conf)
```hocon
rest.base-url = "http://url-of-my-backend-server"
url.gateway = ${urls.backend}
```

For reverse effect:

(configForMyGateway.conf)
```hocon
rest.base-url = "http://url-of-my-gateway-server"
url.backend = ${urls.gateway}
```

### Advanced usage

#### Writing custom Logger

```kotlin
import run.smt.ktest.rest.logger.Logger
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification

class MySuperLogger : Logger {
    override fun log(request: FilterableRequestSpecification): (Response) -> Unit {
        println(request) // my super-duper logging of request
        return { response ->
            println(response) // my super-duper logging of response
        }
    }
}
```

#### Writing custom authorization adapter

```kotlin
import run.smt.ktest.rest.authorization.AuthorizationAdapter
import run.smt.ktest.rest.authorization.getRestLogger
import run.smt.ktest.rest.api.RequestBuilder
import run.smt.ktest.rest.api.RequestElement
import run.smt.ktest.config.get
import com.typesafe.config.Config
import kotlin.properties.Delegates
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.config.LogConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.restassured.internal.TestSpecificationImpl

class MyAuthAdapter : AuthorizationAdapter {
    var config : Config by Delegates.notNull()
    
    override fun setup(config: Config) {
        this.config = config
    }
    
    override fun RequestBuilder.enrichRequest(request: Sequence<RequestElement>) : Sequence<RequestElement> {
        return sequenceOf(header("token", obtainSomeToken())) + request
    }
    
    private fun obtainSomeToken(): String {
        return makeRequestForToken(TestSpecificationImpl(
            RequestSpecBuilder()
                .setConfig(RestAssuredConfig.config().logConfig(
                    LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails()
                        .enablePrettyPrinting(true)
                ))
                .addFilter(getRestLogger(config["logger"]))
                .setContentType(ContentType.JSON)
                .setBaseUri(config.getString("my-authorization-host"))
                .build(),
            ResponseSpecBuilder().build()
        ).requestSpecification)
    }
    
    private fun makeRequestForToken(restAssured: RequestSpecification): String = TODO("need to write authorization request logic")
}
```

(defaults.conf)
```HOCON
authorization.adapters.my-auth = MyAuthAdapter
```

#### Hacking default configuration

You may want to setup default configuration for all context that you use:

```HOCON
___DEFAULTS___ {
    rest {
        logger {
            class = MySupperLogger // from example above
        }
        authorization.adapters.my-auth = MyAuthAdapter // from example above
    }
}
```
