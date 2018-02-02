# [kTest](../README.md) :: [Core](README.md) :: Util

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-util'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-util</artifactId>
</dependency>
```

## Description

This module contains some useful utilities

### IO

```kotlin
import run.smt.ktest.util.resource.*

fun usage1() {
    if ("myResource".resourceExists()) {
        val byteArrayResource = "myResource".loadAsBytes()
        val inputStreamResource = "myResource".load()
        val stringResource = "myResource".loadAsString()
    }
}
```

### Reflection

If you once been thinking what `assignableFrom` means you'll definitely like this:
```kotlin
import run.smt.ktest.util.reflection.*

fun usage2() {
    val myClassInstanceIsInstanceOfMyOtherClass = MyClass::class canBeAssignedTo MyOtherClass::class
    // there are also overloads for generics:
    val test = MyClass::class.canBeAssignedTo<MyOtherClass>()
    // and all possible combinations for java class reflection
    val test1 = MyClass::class.java canBeAssignedTo MyOtherClass::class
}
```

### Time/Duration DSL

```kotlin
import run.smt.ktest.duration.*

fun usage3() {
    val testTime = inSeconds { 10.days() + 2.hours() + 3.minutes() + 15.seconds() + 2.millis() }
    
    println(inSeconds { 2.minutes() })
    println(inDays { 240.hours() })
    println(inMillis { testTime.seconds().show() })
    println(inSeconds { testTime.seconds().show() })
    println(inMinutes { testTime.seconds().show() })
    println(inHours { testTime.seconds().show() })
    println(inDays { testTime.seconds().show() })
}
```

### Eventually

If you have async code that will complete some time and you need to test it you can use `within` and `eventually`

```kotlin
import run.smt.ktest.eventually.*

fun usage4() {
    eventually(Duration.ofSeconds(5)) {
        // some async checks that may throw exceptions
    }
    
    // using Duration DSL
    within { 5.seconds() } execute {
        // some code
    }
    
    // setup delay between attempts
    within { 5.seconds() } withDelay { 200.millis() } execute {
        // some code
    }
}
```

### ADTs/Monads/...

This is far from complete implementations mostly needed by code of kTest itself, so if you really need some ADTs/monads/...
you should use some other library like funKtionale, or even kotlin-monads, kats or kategory if you really want to write haskell in kotlin 

#### Identity function

```kotlin
import run.smt.ktest.util.functional.identity

fun <T> flatten(someList: List<List<T>>): List<T> {
    return someList.flatMap(identity())
}
```

#### Try

```kotlin
import run.smt.ktest.util.functional.Try.*

fun usage5() {
    val myTry = Try.of<Int> { throw Exception() }
    val myOtherTry = Success(5)

    fun applyToTry(attempt: Try<Int>): String {
        return attempt
            .map { it + 1 }
            .mapTry { someCodeThatMayThrow(it) }.flatMap { Success(it) }
            .value ?: "Exception occurred"
    }
    
    val myFirstTryResult = applyToTry(myTry)
    val myOtherTryResult = applyToTry(myOtherTry)
}

```

#### Either

```kotlin
import run.smt.ktest.util.functional.Either.*
import run.smt.ktest.util.functional.identity

fun usage6() {
    val myLeft = left<String, Int>("42")
    val myRight = right<String, Int>(42)
    
    val myString = myLeft.unify(identity, { it.toString() })
    val myInt = myRight.unify({ it.toInt() }, identity())
    
    val mapped: Either<String, String> = myLeft.bimap(identity(), { it.toString() })
    val leftMapped: Either<String, String> = myRight.mapLeft { it.toString() }
    val rightMapped: Either<Int, Int> = myRight.mapRight { it.toInt() }
    val flippedLeft : Either<Int, String> = myLeft.flip
}
```

### Text

```kotlin
import run.smt.ktest.util.text.*

fun usage7() {
    // works just like scala's stripMargin
    val myText = """
    | {
    |   "hello": "world"
    | }
    """.stripMargin()
   val myUncomfortablyWrittenText = " {\n   \"hello\": \"world\"\n }"
   // myText is equal to myUncomfortablyWrittenText
}
```

### Conversion to map

Allows you to convert arbitrary POJO to map:

```kotlin
import run.smt.ktest.util.tomap.*

fun usage8(myPojo: MyPojo) {
    val myPojoAsMap = myPojo.toMap()
    val myPojoWithNestedPojosInFieldAsMap = myPojo.toMap(deep = true)
}
```

### Collection API extensions

Package: `run.smt.ktest.util.collection`

List of extension methods:

 * `head`
 * `headOption`
 * `tail`
 * `init`
 * `scan`
 * `padTo`
 * `zipWithIndex`
 * `permutations`
 * `crossProduct`

### DSL keywords

Package: `run.smt.ktest.util.dsl`

Usage example (based on ktest-rest):

```kotlin
import run.smt.ktest.util.dsl.*
import run.smt.ktest.rest.rest
import run.smt.ktest.rest.url.*

object Url : UrlProvider {
    val a = "a"
    val b = "b"
}

fun usage9() {
    val url = createUrlDsl(Url)

    rest {
        val result: String = using(url) {
            a / b / param("c")
        } execute {
            GET(pathParam("c", "value"))
        }

        // alternative

        val result2: String = using(url {
            a / b / param("c")
        }) execute {
            GET(pathParam("c", "value"))
        }
    }
}
```
