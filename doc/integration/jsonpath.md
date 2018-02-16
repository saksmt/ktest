# [kTest](../README.md) :: [Integration](README.md) :: JSONPath

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-jackson'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-jackson</artifactId>
</dependency>
```

## Description

Provides some DSL for JSONPath library. It contains of 2 huge parts: basic API + SQL-like API and subtree DSL

### SQL-like DSL + basic operations

#### Complex usage example

Since it is pretty easy to understand there will be only this sample, for more see sources and IDEAs help popups :)

```kotlin
import run.smt.ktest.json.type
import run.smt.ktest.jsonpath.*
import run.smt.ktest.jsonpath.criteria.filterNot
import com.jayway.jsonpath.DocumentContext

fun usage() {
    val jp = "myJson.json".loadAsJsonPath()
    val simpleQuery: DocumentContext = jp["$.myField"]
    val filtering: DocumentContext = jp filter { "myField".exists() and "myOtherField" eq "10" }
    val sqlStyle: DocumentContext = jp select "myField" where { "myOtherField" eq "12" }
    val complexSqlStyle: DocumentContext = jp select "$..outer[?]" where {
      "someField" eq "3" and "inner" exists true
    } select "$..someObjectInsideInner[?]" where {
      "objectField" empty false
    }
    val trueSqlStyle: DocumentContext = select("$..someObjectInsideInner[?]") from (
        select("$..outer[?]") from jp where {
            "someField" eq "3" and "inner" exists true
        }
    ) where {
        "objectField" empty false
    }
    
    // deserialization
    val myResult = simpleQuery castTo ResultPojo::class
    val myResult2: ResultPojo = simpleQuery.castTo()
    val myResult3 = complexSqlStyle castTo { list<Contained>() } 
}
```

### Subtree DSL

This DSL allows you to manipulate your JSON in JSONPath DocumentContext with tree-like style.

#### Extraction

```kotlin
import run.smt.ktest.util.text.*
import run.smt.ktest.jsonpath.*
import run.smt.ktest.jsonpath.criteria.*
import run.smt.ktest.jsonpath.subtree.*
import com.jayway.jsonpath.JsonPath

val sourceJson = """
    | {
    |   "a": {
    |       "b": {
    |          "b": "hello",
    |          "c": "don't need this"
    |       }
    |   },
    |   "a1": { "b": "but need this" }
    | }
    """.stripMargin()

val jp = JsonPath.parse(sourceJson)
val myResultSubtree = extractSubtree(jp) {
    + "a.b" {
        + "b"
    }
    + "a1"
}
// you'll get:
/// {
///     "a": { "b": { "b": "hello" } },
///     "a1": { "b": "but need this" }
/// }

val otherWay = extractSubtree(jp) {
    + "a.b.b"
    + "a1"
} // will give same result

val yetAnotherWay = extractSubtree(jp) {
    + "a" {
        + "b" {
            + "b"
        }
    }
    + "a1"
} // will give same result

val youCanUseFiltersToo = extractSubtree(jp) {
    + "a.b.b"
    + filter {
        "@.b" eq "but need this"
    }
} // will give same result

val youAlsoCanStoreYourSubtreeSpecifications = createSubtree {
    + "hello.stored.subtrees"
}

val andThenApplyThemToYourJsonPath = extractSubtree(jp, youAlsoCanStoreYourSubtreeSpecifications)

// huge example

val applicationSubtree = createSubtree {
   + "some.application.configuration" {
       + "core" {
           + "datasources.appDatasource"

           + "cache"

           // node containing predicates node
           + filter { "@.predicates".exists() }
       }
       
       + "rabbitmq" // whole "rabbitmq" node
        
       // leaf nodes
       + "technological_user"
       + "someParam"
       + "someOtherParam"
        
       // will be correctly and carefully merged with previous mentioning of "core"
       + "core.datasources.someOtherAppDatasource"
       + "core.datasources.appDatasource" // duplicates will be ignored
   }
}
```

#### Removal

Reverse operation for extraction, will remove specified subtree leaving everything else untouched

```kotlin
import run.smt.ktest.jsonpath.*
import run.smt.ktest.jsonpath.criteria.*
import run.smt.ktest.jsonpath.subtree.*
import com.jayway.jsonpath.DocumentContext

fun removeConnectionInfo(globalConfig: DocumentContext) {
    globalConfig.remove {
        // supports everything supported by extraction
        + "some.application.config" {
            + "core.datasources"
            
            + "rabbitmq"
        }
    }
    
    // you can also force ignore of missing nodes 
    globalConfig.remove(ignoreMissing = true) {
        // supports everything supported by extraction
        + "some.application.config" {
            + "core.datasources"
            
            + "rabbitmq"
            
            + "MISSING_NODE" // will fail without ignoreMissing flag
        }
    }
}
```

#### Renaming

**WARN:** It is renaming, **NOT MOVING**

```kotlin
import run.smt.ktest.jsonpath.*
import run.smt.ktest.jsonpath.criteria.*
import run.smt.ktest.jsonpath.subtree.*
import com.jayway.jsonpath.DocumentContext

fun renameAllPasswordsToHashes(applicationConfig: DocumentContext) {
    applicationConfig.rename {
        "config.apps.app" {
            "core.datasources" {
                "otherAppDs.password" to "hash"
                "appDs" {
                    "password" to "hash" // almost same as previous renaming
                }
            }
            
            "rabbitmq" {
                "first.password" to "hash"
                "second.password" to "hash"
                "third.password" to "hash"
            }
        }
    }
}

fun renameApp(applicationConfig: DocumentContext) {
    applicationConfig.rename {
        // will work well even if there are nested nodes
        // but you should be careful doing such things
        // cause if you want to rename child node as well
        // you'll have to do so in separate "rename"-call
        // or else behavior is undefined
        "config.apps.app" to "otherApp"
    }
}
```

#### Addition

You can add something to your tree:

```kotlin
import run.smt.ktest.util.text.*
import run.smt.ktest.jsonpath.*
import run.smt.ktest.jsonpath.criteria.*
import run.smt.ktest.jsonpath.subtree.*
import com.jayway.jsonpath.DocumentContext

fun addPasswordsTo(applicationConfig: DocumentContext) {
    applicationConfig.put {
        "config.apps.app" {
            "core.datasources" {
                "pass" at "anotherApp.password"
                "APP" at "app.password"
            }
            
            "rabbitmq.first" {
                "guest" at "password"
            }
        }
    }
}

// you can also rewrite hierarchy...

fun replaceRabbitMqWithPropertiesAndBeanValidationWithTree(applicationConfig: DocumentContext) {
    applicationConfig.put(force = true) { // note the "force" flag, don't use it always, cause it is at least 3 times slower
        "config.apps.app" {
            """
            | first.username = guest
            | first.password = guest
            | first.host = somehost
            | first.port = 5672
            |
            | second.username = guest
            | # ...
            """.stripMargin() at "rabbitmq" // this will work even without force
            
            "beanValidation" { // and this will only work with force enabled
                "SomeClass" {
                    "@NotNull" at "msisdn"
                }
            }
        }
    }
}

// ... and create new nodes even if their parents don't exist

fun appendNewRabbitCredentials(applicationConfig: DocumentContext) {
    applicationConfig.put {
        "config.apps.app.rabbitmq.connection" {
            "outer-space" {
                "spaceship" at "host"
                "42" at "port"
                "cap" at "username"
                "let me enter" at "password"
                "/" at "vhost"
            }
        }
    }
}
```
