# [kTest](..) :: [Core](README.md) :: Runner

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-runner'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-runner</artifactId>
</dependency>
```

## Description

This module contains base code of kTest (and actually it is kTest) providing ability to write specs like this:

```kotlin
import org.junit.Assert.*
import run.smt.ktest.specs.BehaviorSpec

class MySpec : BehaviorSpec({
    val thisIsAwesome = true

    given("kotlin") {
        `when`("need integration/acceptance testing framework") {
            then("use kTest!") {
                assertTrue(thisIsAwesome)
            }
        }
    }
})
```

There are no matchers, helpful methods, etc. Just the runner and the specs. You are free to use whatever matcher library
you want. Though recommended library is [hamkrest](https://github.com/npryce/hamkrest)

### Available spec styles

#### Behavior spec

```kotlin
import org.junit.Assert.*
import run.smt.ktest.specs.BehaviorSpec

class MySpec : BehaviorSpec({
    val thisIsAwesome = true

    given("kotlin") {
        `when`("need integration/acceptance testing framework") {
            then("use kTest!") {
                assertTrue(thisIsAwesome)
            }
        }
    }
})
```

#### Feature spec

```kotlin
import org.junit.Assert.*
import run.smt.ktest.specs.FeatureSpec

class MySpec : FeatureSpec({
    feature("usage of kTest") {
        scenario("awesomeness") {
            assertTrue(true)
        }
    }
})
```

#### Free spec

```kotlin
import org.junit.Assert.*
import run.smt.ktest.specs.FreeSpec

class MySpec : FreeSpec({
    "kTest" - {
        "is awesome" {
            assertTrue(true)
        }
    }
})
```

#### Simple spec

```kotlin
import org.junit.Assert.*
import run.smt.ktest.specs.SimpleSpec

class MySpec : SimpleSpec({
    suite("kTest") {
        test("awesomeness test") {
            assertTrue(true)
        }
    }
})
```

#### Word spec

```kotlin
import org.junit.Assert.*
import run.smt.ktest.specs.WordSpec

class MySpec : WordSpec({
    "kTest" should {
        "provide awesomeness" {
            assertTrue(true)
        }
    }
})
```

## Extending

### Writing you own spec style

1. Define your spec style:
    ```kotlin
    import run.smt.ktest.BaseSpec
    import run.smt.ktest.internal.api.SpecBuilder
    
    abstract class SuiteAndCheckSpec(body: MySpecStyle.() -> Unit) : BaseSpec {
        init { body() }
    
        infix fun String.suite(body: () -> Unit) = SpecBuilder.suite(this, body)
        infix fun String.check(body: () -> Unit) = SpecBuilder.case(this, emptyList(), body)
        fun String.check(vararg annotations: Annotation, body: () -> Unit) = SpecBuilder.case(this, annotations.toList(), body)
    }
    ```

2. Use your brand new spec style:
    ```kotlin
    import org.junit.Assert.*
    
    class MySpec : SuiteAndCheckSpec({
        "kTest even supports" suite {
            "my custom spec styles" check {
                assertTrue(true)
            }
        }
    })
    ```

3. PROFIT!
