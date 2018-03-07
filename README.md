# kTest

[![CircleCI](https://img.shields.io/circleci/project/github/saksmt/ktest.svg?style=flat-square)](https://circleci.com/gh/saksmt/ktest) ![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/oss.sonatype.org/content/repositories/releases/run/smt/ktest/ktest-api/maven-metadata.xml.svg?style=flat-square)

kTest is integration / acceptance / system / any other non-unit test oriented modular test framework in Kotlin.
Inspired by [kotlintest](https://github.com/kotlintest/kotlintest), [specs2](https://github.com/etorreborre/specs2) and martians. 

## Usage

### [Styling your test](doc/core/api.md)

[//]: # (no_check)
```kotlin
object MyTestSpecification : BehaviorSpec({
    given("some service") {
        `when`("executing it's API") {
            then("it should work correctly") {
                // test body...
            }
        }
    }
})
```

### [Performing simple HTTP](doc/integration/rest-assured/rest.md)

[//]: # (no_check)
```kotlin
fun makeMyHttp(): List<Account> {
    return rest["my-backend"] {
        using(url) {
            agreements / param("id") / accounts 
        } execute {
            GET(pathParam(123), header("Accept", "application/json"))
        }
    }
}
```

### [Working with JSON](doc/integration/jackson.md)

[//]: # (no_check)
```kotlin
fun loadAccounts(resourcePath: String) =
    resourcePath.loadAsJson { list(map<String, Any>()) }.also {
        println(it.dump()) // pretty-printing pseudo-logging
    }
```

### [Matching over JSON](doc/integration/json-matcher.md)

[//]: # (no_check)
```kotlin
fun compareMyJsons(expected: JsonNode, actual: JsonNode) {
    with(JsonNodeMatchers) {
        assertThat(actual, isIdenticalTo(expected).bySubtree {
            // comparing only meaningful nodes
            "accounts[*]" {
                + "id"
                + "accountNumber"
                
                "owner" {
                    + "id"
                    + "name"
                }
            }
        })
    }
}
```

### [Searching over JSON](doc/integration/jsonpath.md)

*Powered by [JSONPath](https://github.com/json-path/JsonPath)*

[//]: # (no_check)
```kotlin
fun allIdsNearAccountNumbers(jp: DocumentContext) =
    jp select "id" where {
        "accountNumber".exists()
    } castTo { list<Long>() }
```

### [Accessing database](doc/integration/spring-jdbc.md)

[//]: # (no_check)
```kotlin
fun getAccounts(activeOn: Date) =
    "my-database".db {
        select<Account>("""
           | SELECT * FROM accounts 
           | WHERE 
           |   close_date is NULL 
           |   OR close_date < :activeOn
        """.trimMargin()) {
            parameter("activeOn", activeOn)
        }.asList()
    }
```

### [REST specification](doc/integration/rest-assured/rest-test.md)

[//]: # (no_check)
```kotlin
object MyTest : SimpleSpec({
    suite("my service suite") {
        restTest(name = { "${it.method} accounts" }) {
            url { agreements / accounts }
        
            GET(queryParam("name", "%"))
            POST(body("name", "%")) { it / search }
            
            expect { response: DocumentContext ->
                // do some check
            }
        }
    }
})
```

### [Configuring reporting engine](doc/integration/allure.md)

*Reporting powered by excellent [Allure](http://allure.qatools.ru)*

[//]: # (no_check)
```kotlin
object MyTest : AllureSpec({
    feature("some feature", metaInfo = {
        blocker()
    }) {
        story("true story", metaInfo = {
            issue("PROJ-111")
        }) {
            case("my case", metaInfo = {
                description("my description")
            }) {
                // test body...
            }
        }
    }
})
```

### Putting it all together

[//]: # (no_check)
```kotlin
object AccountByCustomerRestApiSpec : AllureSpec({
    beforeAll {
        rest["backend"] {
            using(url) {
                `internal` / caches
            } execute {
                DELETE(queryParam("force", "true"))
            }
        }
    }

    epic("Search") {
        feature("Account by customer search") {
            story("Single criteria search") {
                val testTable = table(
                    header("criteriaName", "criteriaValue", "expectedJsonName"),
                    row("billing", ">100", "richAccounts.json"),
                    row("region", "Central", "centralRegionAccounts.json"),
                    row("validTill", ">${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}", "activeAccounts.json")
                )
                // should be generated right before test
                val myGeneratedCustomer: Customer = testData["customer.json"]
                
                forAll(testTable) { criteriaName, criteriaValue, expectedJsonName ->
                    val criteria = mapOf<String, String>(
                        criteriaName, criteriaValue
                    )
                    
                    restTest(name = { "Search account by \"$criteriaName\": ${it.method}" }, metaData = {
                        category<Complex>()
                        flaky()
                    }) {
                        url { customers / param("customerId") / accounts }
                        
                        GET(queryParams(criteria), pathParam("customerId", myGeneratedCustomer.id))
                        POST(body(criteria), pathParam("customerId", myGeneratedCustomer.id))
                        
                        expect { response: DocumentContext ->
                            with(DocumentContextMatchers) {
                                assertThat(response, matches(expectedJsonName.loadAsJsonPath()).afterRemovalOfSubtree {
                                    "account[].metaData" {
                                        + "date"
                                        + "IP"
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
})
```

For more see [docs](doc/README.md) and [samples](sample)

## Download

You can use [dependency management](doc/pom.md)

### Gradle

```groovy
compile 'run.smt.ktest:ktest-api'
compile 'run.smt.ktest:ktest-config'
compile 'run.smt.ktest:ktest-util'
compile 'run.smt.ktest:ktest-runner-junit4'
compile 'run.smt.ktest:ktest-allure'
compile 'run.smt.ktest:ktest-jackson'
compile 'run.smt.ktest:ktest-json-matchers'
compile 'run.smt.ktest:ktest-jsonpath'
compile 'run.smt.ktest:ktest-db'
compile 'run.smt.ktest:ktest-rest'
compile 'run.smt.ktest:ktest-resttest'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-api</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-config</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-util</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-runner-junit4</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-allure</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-jackson</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-json-matchers</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-jsonpath</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-db</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-rest</artifactId>
</dependency>

<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-resttest</artifactId>
</dependency>
```

## License

All source code is licensed under [MIT license](LICENSE)
