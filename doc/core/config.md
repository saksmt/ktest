# [kTest](../README.md) :: [Core](README.md) :: Config

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-config'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-config</artifactId>
</dependency>
```

## Description

Integration layer for [lightbend config](https://github.com/lightbend/config)

### Load

This integration layer will load (in order of priority):
 - System properties
 - Environment
 - file with name from `configFile` property (defaults to `tests.conf`)
 - All configuration files with names: `default.conf`, `defaults.conf` will be loaded from all resources

### Usage in code

```kotlin
import run.smt.ktest.config.*
import com.typesafe.config.Config
import java.time.Duration

fun usage(someOtherConfig: Config) {
    val someInt: Int = config["some.int"]
    val someConfig: Config = config["some.config.value"]
    val nested: Duration = someConfig["duration"]
    val fallbackToDefault = someConfig fallbackTo someOtherConfig
}
```
