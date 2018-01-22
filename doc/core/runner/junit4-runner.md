# [kTest](../..) :: [Core](..) :: [Runner](README.md) :: JUnit 4 Runner

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-runner-junit4'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-runner-junit4</artifactId>
</dependency>
```

## Description

This module contains Junit4 runner for kTest. Mark your specifications with `@RunWith(KTestJUnitRunner::class)` which lay
in `run.smt.ktest.runner.junit4`:

```kotlin
import run.smt.ktest.specs.FreeSpec
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class MySpec : FreeSpec({
    "some suite" - {
        "some test" {
            // test code
        }
    }
})
```

## Limitations

Categories in this runner are implemented through [junit4 experimental category API](https://github.com/junit-team/junit4/wiki/Categories)
so you should read about that API to avoid surprises.

TL;DR:

 - Category **must** be FQCN of existing class or interface
 - Categories that are not FQCNs will be only used in for example allure as tags, but won't have effect on runner
