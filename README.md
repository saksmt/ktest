# kTest

kTest is integration/acceptance test oriented modular test framework in Kotlin.
Inspired by [kotlintest](https://github.com/kotlintest/kotlintest), [specs2](https://github.com/etorreborre/specs2) and martians. 

## Example (with usage of all available modules)

```kotlin
class MyTest : BehaviorSpec({
    given("my service") {
        `when`("called some function") {
            then("it should work fine") {
                //... todo
            }
        }
    }
})
```

For more see [docs](doc/README.md)

## License

All source code is licensed under [MIT license](LICENSE)
