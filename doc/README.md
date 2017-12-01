# kTest

kTest is integration/acceptance test oriented modular test framework in Kotlin

## Notes

For all dependencies either maven or gradle assuming you have some sort of dependency management 
(`dependencyManagement` section in maven or spring dependency management plugin for gradle), so there will 
be no versions for dependencies in this documentation

## Modules

1. [Core](core/README.md) - core module
    1. [Runner](core/runner.md) - core module containing spec-styles and jUnit runner
    2. [Utilities](core/util.md) - some utilities that can't be in other modules (IO, reflection, ...)
    3. [Config](core/config.md) - easy to use configuration for your tests in HOCON
2. [Integration](integration/README.md) - integration layer module for external libraries/tools
    1. Allure - integration with Allure reporting framework
    2. [Jackson](integration/jackson.md) - integration with Jackson JSON library
    3. [JSONPath](integration/jsonpath.md) - integration with JSONPath library
    4. [JSON matchers](integration/json-matcher.md) - smart JSON matchers based on Jackson and JSONPath with integration for HamKrest
    5. RestAssured - integration with RestAssured
        1. Core - core RestAssured integration
        2. RestTest - DSL for quick implementation of simple REST tests
    6. [Spring JDBC](integration/spring-jdbc.md) - integration with Spring JDBC
    7. **Not Implemented Yet** RabbitMQ - integration with RabbitMQ
