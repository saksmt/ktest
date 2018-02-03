# [kTest](..) :: [Integration](README.md) :: Jackson

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

Provides some DSL for Jackson JSON library

### Default ObjectMapper

```kotlin
import run.smt.ktest.json.*

fun usage1() {
    println(mapper.writeValueAsString(mapOf("hello" to "world")))
}
```

### Extension functions

```kotlin
import run.smt.ktest.json.*

class MyPojo(/* POJO fields */)

fun usage2() {
    val myPojo = mapper.readValue<MyPojo>(stringOrByteArrayOrStreamOrUrlOrFileOrReader)
}
```

### TypeDSL

[//]: # (package:typedsl)
```kotlin
package typedsl

import run.smt.ktest.json.*

class MyGenericPojo<T> { /*some body*/ }

fun usage3() {
    fun usage(mySource: String) {
        val myPojo: Map<String, Set<MyGenericPojo<List<String>>>> = mapper.readValue(mySource, type {
            // sadly arbitrary generics still need type specialization...
            map(simple(String::class), set(generic<MyGenericPojo<List<String>>>(MyGenericPojo::class, list<String>())))
        })
    }
}
```

### Serialization

```kotlin
import run.smt.ktest.json.*

fun usage4(myPojo: MyPojo) {
    val jsonAsBytes = myPojo.serialize()
    val myPrettyJsonAsString = myPojo.dump()
}
```

### Deserializtion

[//]: # (package:deserialization)
```kotlin
package deserialization

import run.smt.ktest.json.*
import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.* // if you have json-path as your dependency

class SomePojo { /* some body */ }
class SomeGenericPojo<T> { /* some body */ }

fun usage5(myData: String) {
    val deserialized1 = myData.deserialize<SomePojo>()
    val deserialized2 = myData deserialize SomePojo::class
    val deserialized3 = myData deserialize { generic<SomeGenericPojo<List<String>>>(SomeGenericPojo::class, list<String>()) }
    val deserializedAsJsonNode = myData deserialize JsonNode::class
    
    // if you added json-path as dependency to your project
    val deserializedAsJsonPath = myData deserialize DocumentContext::class
}
```

### Mapping

[//]: # (package:mapping)
```kotlin
package mapping

import run.smt.ktest.json.*
import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.* // if you have json-path as your dependency

class SomePojo { /* some body */ }
class SomeGenericPojo<T> { /* some body */ }

fun usage6(myData: JsonNode) {
    val mapped1 = myData.mapTo<SomePojo>()
    val mapped2 = myData mapTo SomePojo::class
    val mapped3 = myData mapTo { generic<SomeGenericPojo<List<String>>>(SomeGenericPojo::class, list<String>()) }
    val mappedToJsonNode = myData mapTo JsonNode::class
    
    // if you added json-path as dependency to your project
    val mappedToJsonPath = myData mapTo DocumentContext::class
}
```
