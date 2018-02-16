# [kTest](../README.md) :: [Integration](README.md) :: JSON Matchers

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-json-matchers'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-json-matchers</artifactId>
</dependency>
```

## Description

Contains smart JSON diff tool with integration for Hamkrest

### Configuration

It uses global configuration from `ktest-core/ktest-config`. Here is default config:

```hocon
json {
    comparator {
        # Tells json comparator to treat missing fields not equal to fields containing null if true
        # otherwise missing fields are equal to null fields, also affects "object-difference-threshold"
        strict-null-checking = true
        
        # If true ignores extra fields in objects, also affects "object-difference-threshold"
        strict-object-field-checking = true
        
        # Specify how to compare arrays:
        #  strict - compare "as is", good choice for check of sorting
        #  unordered - sort and then compare
        #  permutation-based - compare every combination of elements and choose one with less mismatches
        array-comparison = strict
        
        # Maximum field list difference before giving up
        # lets say you have expected object with fields "a", "b", "c" and actual with fields "a", "d", "e", "f", so if
        # you want to still check for an "a" field, refer to the following table:
        # 
        #    strict-null-check | strict-field-check | minimal threshold to see "a"
        #    ------------------+--------------------+----------------------------- 
        #    false             | false              | 5
        #    true              | false              | 3
        #    false             | true               | 2
        #    true              | true               | 0 (it is ignored)
        #
        object-difference-threshold = 5
        
        # Default behavior of assertions is to print only first failure/mismatch, but you can change it there if you want
        # Also if you want to print all mismatches place "-1" here
        print-n-first-mismatches = 1
        # Alias for "print-n-first-mismatches"
        print-first-n-mismatches = ${matcher.json.print-n-first-mismatches}
    }
}
```

You can also create configuration through DSL in kotlin:
```kotlin
import run.smt.ktest.json.matcher.api.*

fun usage1() {
    val myConfig = matcherConfig {
        compareArraysBasedOnPermutations()
        printAllMismatches()
        neverGiveUpComparisonOfObjects()
        compareNullsStrictly()
        extraFieldsInObjectAreAllowed()
        // for more see help from IDEA on "this.<Ctrl + Space>"
    }
}
```

Also note that for options unspecified in kotlin defaults will be taken from global configuration

### Difference

```kotlin
import run.smt.ktest.json.matcher.api.*
import com.fasterxml.jackson.databind.JsonNode

fun usage2(firstNode: JsonNode, secondNode: JsonNode, configuration: MatcherConfig) {
    val myComparator = jsonComparator {
        // here goes configuration from previous section
    }
    // alternatively you can create comparator from existing configuration:
    val myOtherComparator = jsonComparatorFor(configuration)
    
    val myDiff = myComparator.diff(firstNode, secondNode)
    // you can now inspect resulting list of mismatches
    myDiff.forEach {
        println(it.message)
    }
}
```

### Hamkrest integration

Due to API of hamkrest about actual value there are 2 sets of matchers: for JSONPaths DocumentContext and Jacksons JsonNode.

**Attention:** `matches` function below has absolutely nothing to do with `com.natpryce.hamkrest.matches` function!

#### JsonNode

```kotlin
import run.smt.ktest.json.matcher.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.should.shouldMatch
import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.DocumentContext

fun usage3(
    someNode: JsonNode,
    otherNode: JsonNode,
    dc: DocumentContext,
    otherDc: DocumentContext
) {

    // default global configuration
    with(JsonNodeMatchers) {
        // honoring sort order in arrays
        assertThat(someNode, isIdenticalTo(otherNode))
        someNode shouldMatch ordered(otherNode) // same as previous

        // ignoring sort order in arrays
        assertThat(someNode, matches(otherNode))
        someNode shouldMatch unordered(otherNode) // same as previous
    }
    // there are also overloads for "matches", "identicalTo", "unordered" and "ordered" for DocumentContext

    with(jsonNodeMatcherConfig {
        // there goes configuration for comparator
        checkAllPermutationsOnArrayComparison()
    }) {
        someNode shouldMatch otherDc.json
        assertThat(someNode, matches(otherDc)) // same as previous
    }

    // there is also ability to match JSONs by their subtrees (see section about subtrees in integration with JSONPath)
    with(JsonNodeMatchers) {
        assertThat(someNode, isIdenticalTo(otherDc).bySubtree {
            + "nodeName"
        })

        someNode shouldMatch unordered(dc).afterRemovalOfSubtree {
            + "nodeName"
        }
    }
}
```

#### DocumentContext

API for matcher for DocumentContext is almost identical:

```kotlin
import run.smt.ktest.json.matcher.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.should.shouldMatch
import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.DocumentContext

fun usage4(
    someNode: JsonNode,
    otherNode: JsonNode,
    dc: DocumentContext,
    otherDc: DocumentContext
) {

    // default global configuration
    with(DocumentContextMatchers) {
        // honoring sort order in arrays
        assertThat(dc, isIdenticalTo(otherNode))
        dc shouldMatch ordered(otherNode) // same as previous

        // ignoring sort order in arrays
        assertThat(dc, matches(otherNode))
        dc shouldMatch unordered(otherNode) // same as previous
    }
    // there are also overloads for "matches", "identicalTo", "unordered" and "ordered" for DocumentContext
    
    with(documentContextMatcherConfig {
        // there goes configuration for comparator
        checkAllPermutationsOnArrayComparison()
    }) {
        dc shouldMatch otherDc.json
        assertThat(dc, matches(otherDc)) // same as previous
    }

    // there is also ability to match JSONs by their subtrees (see section about subtrees in integration with JSONPath)
    with(DocumentContextMatchers) {
        assertThat(dc, isIdenticalTo(otherDc).bySubtree {
            + "nodeName"
        })

        dc shouldMatch unordered(someNode).afterRemovalOfSubtree {
            + "nodeName"
        }
    }
}
```
