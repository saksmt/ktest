# [kTest](..) :: [Integration](../README.md) :: [RestAssured](README.md) :: RestTest

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-resttest'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-resttest</artifactId>
</dependency>
```

## Description

Declarative DSL for quick and easy testing of REST APIs

### Boilerplate

Sadly you need to write some boilerplate to make this work. In this section we assume that you already have read about [REST DSLs URL DSL](rest.md#bonus:-url-dsl) and now you have your URL DSL class named `Url`.

All you need to do is:

```kotlin
import run.smt.ktest.BaseSpec
import run.smt.ktest.resttest.*

val BaseSpec.restTest
    get() = createRestTestDSL<Url>(this) {
        urlDsl(Url)
    }
```

### Sample

```kotlin
import run.smt.ktest.specs.AllureSpec
import run.smt.ktest.resttest.*

// note that you're free to use any spec from run.smt.ktest.specs package (including AllureSpec)
class MySpec1 : BehaviorSpec({
    given("my service") {
        `when`("search works") {
            // all arguments for restTest except for DSL are optional
            restTest(name = { "Request on: ${it.method} ${it.url} should respond with valid value" }, metaInfo = {
                // here goes any allure meta info:
                title("${it.method}-request")
                severity(critical)
            }) {
                // here goes your URL DSL
                url { gateway / customer / search }
                
                // here you specify HTTP methods that will be used (anything from REST lib's simple requests):
                GET(queryParam("name", "John Doe"))
                // note that you can pass all available parameters from REST lib as arguments for this request methods
                POST(body("name" to "John Doe")) {
                    // you can redefine URL for any of your requests:
                    it / "some_post_specific_suffix" // where `it` is originally (in url section) set URL
                }
                
                expect { response: Customer ->
                    assert(response.fullName == "John Doe")
                }
                
                expect<Customer> {
                    assert(it.firstName == "John")
                }
            }
        }
    }
})
```

As result of the sample above there will be:
 - generated 2 tests for allure with titles:
    * *GET-request*
    * *POST-request*
 - first test will execute GET query to URL build from `gateway / customer / search`
   with query parameter `name` equal to `"John Doe"` and check that result:
    * converts to `Customer` POJO with field `fullName` equal to *John Doe*
    * converts to `Customer` POJO with field `name` equal to *John*
 - second test will execute POST query to URL build from `gateway / customer / search / "some_post_specific_suffix"`
   with body `{"name":"John Doe"}` and check that result:
    * converts to `Customer` POJO with field `fullName` equal to *John Doe*
    * converts to `Customer` POJO with field `name` equal to *John*

### REST Contexts

You can easily use rest contexts from [REST](rest.md) in your RESTTest:

```kotlin
import run.smt.ktest.specs.SimpleSpec
import run.smt.ktest.resttest.*

class MySpec2 : SimpleSpec({
    restTest["my.specific.context"] {
        url { "some" / "url" / "with" / param("param") }
        
        OPTIONS(header("X-CompanyHeader", "value"), pathParam("param", 123))
        
        expect<String> { assert(it.isNotBlank()) }
    }
})
```

### Adapting RESTTest for custom spec types

First of all ensure that you really need to specifically adapt RESTTest for your spec type. To do so you need to
know how RESTTest creates test cases for unknown spec types: it just uses `SpecBuilder.case` from `BaseSpec`.
So if it is acceptable behavior you don't need to anything else.

But consider you've made some custom spec type `MySuperSpec` with case method adding some annotations/making some
useful actions that you don't want to lose but you still want to use RESTTest - `RestTestSpecSkeleton` for the rescue!

[//]: # (package:com.company.skel)
```kotlin
package com.company.skel

import run.smt.ktest.resttest.api.RestTestSpecSkeleton
import run.smt.ktest.resttest.api.TestSpecProvider
import com.company.MySuperSpec

class MySuperSpecSkeleton : RestTestSpecSkeleton<MySuperSpec> {
    override fun MySuperSpec.execRestTest(restTestTemplate: TestSpecProvider) {
        restTestTemplate { annotations, name, body ->
            mySuperDuperTest(name, annotations, body) // method from your spec
        }
    }
}
```

Then you just need to register your new skeleton in RESTTest configuration:

[//]: # (package:com.company.skel)
```kotlin
package com.company.skel

import run.smt.ktest.BaseSpec
import run.smt.ktest.resttest.*
import com.company.MySuperSpec
import com.company.skel.MySuperSpecSkeleton

val BaseSpec.restTest
    get() = createRestTestDSL<Url>(this) {
        urlDsl(Url)
        
        skeletons += MySuperSpec::class to MySuperSpecSkeleton()
    }
```

That's all folks!
