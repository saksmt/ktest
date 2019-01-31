package run.smt.ktest.json.matcher

import com.fasterxml.jackson.databind.node.MissingNode
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.runner.RunWith
import run.smt.ktest.json.createJsonNode
import run.smt.ktest.json.loadAsJsonTree
import run.smt.ktest.json.matcher.api.ComparisonMismatch
import run.smt.ktest.json.matcher.api.ComparisonPath
import run.smt.ktest.json.matcher.api.jsonComparator
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import run.smt.ktest.specs.WordSpec

@RunWith(KTestJUnitRunner::class)
object JsonDiffSpec : WordSpec({
    "JsonComparator" should {
        "return no mismatches for equal JSONs" {
            val firstJson = "simple.json".loadAsJsonTree()
            val secondJson = "simple-reordered.json".loadAsJsonTree()

            val comparator = jsonComparator {
                strictlyCompareArrays()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, isEmpty)
        }

        "return mismatch on strict comparison of arrays with different sort order" {
            val firstJson = "json-with-array.json".loadAsJsonTree()
            val secondJson = "json-with-array-reversed.json".loadAsJsonTree()

            val comparator = jsonComparator {
                strictlyCompareArrays()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, anyElement(has("path", { it.path.expectedPath }, equalTo("$.myArray.0.field"))))
            assertThat(diff, anyElement(has("path", { it.path.expectedPath }, equalTo("$.myArray.1.field"))))
            assertThat(diff, hasSize(equalTo(2)))
        }

        "not return mismatch for sort-based array comparison on arrays with different sort order" {
            val firstJson = "json-with-array.json".loadAsJsonTree()
            val secondJson = "json-with-array-reversed.json".loadAsJsonTree()

            val comparator = jsonComparator {
                compareArraysUnordered()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, isEmpty)
        }

        "not return mismatch for arrays containing equal objects with different field order" {
            val firstJson = "json-with-array-of-objects1.json".loadAsJsonTree()
            val secondJson = "json-with-array-of-objects2.json".loadAsJsonTree()

            val comparator = jsonComparator {
                compareArraysUnordered()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, isEmpty)
        }

        "not return mismatch for permutation-based array comparison on arrays with different sort order" {
            val firstJson = "json-with-array.json".loadAsJsonTree()
            val secondJson = "json-with-array-reversed.json".loadAsJsonTree()

            val comparator = jsonComparator {
                compareArraysBasedOnPermutations()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, isEmpty)
        }

        "return mismatch on non-equal values" {
            val firstJson = "simple-diff/first.json".loadAsJsonTree()
            val secondJson = "simple-diff/second.json".loadAsJsonTree()

            val comparator = jsonComparator()

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, hasSize(equalTo(1)))

            val diffElement = diff.first()

            assertThat(diffElement, isA<ComparisonMismatch.ValueMismatch>())
            assertThat(diffElement.path, equalTo(ComparisonPath() + "field"))
            assertThat(diffElement.expected, equalTo(createJsonNode("value1")))
            assertThat(diffElement.actual, equalTo(createJsonNode("value2")))
        }

        "return mismatch on different array size" {
            val firstJson = "array-size-diff/first.json".loadAsJsonTree()
            val secondJson = "array-size-diff/second.json".loadAsJsonTree()

            val comparator = jsonComparator {
                strictlyCompareArrays()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, hasSize(equalTo(1)))

            val diffElement = diff.first()

            assertThat(diffElement, isA(
                has(
                    ComparisonMismatch.ArraySizeMismatch::expectedSize, equalTo(2)
                ) and has(
                    ComparisonMismatch.ArraySizeMismatch::actualSize, equalTo(3)
                )
            ))
        }

        "return size mismatch and value mismatch for different size arrays on permutation-based comparison (unexpected)" {
            val firstJson = "array-size-diff/first.json".loadAsJsonTree()
            val secondJson = "array-size-diff/second.json".loadAsJsonTree()

            val comparator = jsonComparator {
                compareArraysBasedOnPermutations()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, hasSize(equalTo(2)))

            val sizeMismatch = diff.firstOrNull {
                it is ComparisonMismatch.ArraySizeMismatch
            } as? ComparisonMismatch.ArraySizeMismatch

            assertThat(sizeMismatch, present(
                has(
                    ComparisonMismatch.ArraySizeMismatch::expectedSize, equalTo(2)
                ) and has(
                    ComparisonMismatch.ArraySizeMismatch::actualSize, equalTo(3)
                )
            ))

            val unexpectedValue = diff.firstOrNull {
                it is ComparisonMismatch.UnexpectedValue
            } as? ComparisonMismatch.UnexpectedValue

            assertThat(unexpectedValue, present(
                has(
                    ComparisonMismatch.UnexpectedValue::path, equalTo(ComparisonPath() + "2")
                ) and has(
                    ComparisonMismatch.UnexpectedValue::actual, equalTo(createJsonNode("c"))
                )
            ))
        }

        "return size mismatch and value mismatch for different size arrays on permutation-based comparison (missing)" {
            val firstJson = "array-size-diff/first.json".loadAsJsonTree()
            val secondJson = "array-size-diff/second.json".loadAsJsonTree()

            val comparator = jsonComparator {
                compareArraysBasedOnPermutations()
                compareNullsStrictly()
            }

            val diff = comparator.diff(secondJson, firstJson)

            assertThat(diff, hasSize(equalTo(2)))

            val sizeMismatch = diff.firstOrNull {
                it is ComparisonMismatch.ArraySizeMismatch
            } as? ComparisonMismatch.ArraySizeMismatch

            assertThat(sizeMismatch, present(
                has(
                    ComparisonMismatch.ArraySizeMismatch::expectedSize, equalTo(3)
                ) and has(
                    ComparisonMismatch.ArraySizeMismatch::actualSize, equalTo(2)
                )
            ))

            val unexpectedValue = diff.firstOrNull {
                it is ComparisonMismatch.MissingValue
            } as? ComparisonMismatch.MissingValue

            assertThat(unexpectedValue, present(
                has(
                    ComparisonMismatch.MissingValue::path, equalTo(ComparisonPath() + "2")
                ) and has(
                    ComparisonMismatch.MissingValue::expected, equalTo(createJsonNode("c"))
                )
            ))
        }

        "return no mismatches for difference on null vs missing when strict null checking disabled" {
            val firstJson = "nulls-diff/first.json".loadAsJsonTree()
            val secondJson = "nulls-diff/second.json".loadAsJsonTree()

            val comparator = jsonComparator {
                compareNullsNonStrictly()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, isEmpty)
        }

        "return mismatch for difference on null vs missing when strict null checking enabled (missing)" {
            val firstJson = "nulls-diff/first.json".loadAsJsonTree()
            val secondJson = "nulls-diff/second.json".loadAsJsonTree()

            val comparator = jsonComparator {
                compareNullsStrictly()
            }

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, hasSize(equalTo(1)) and allElements(cast(
                has(
                    ComparisonMismatch.StructuralMismatch::missingFields,
                    hasElement("a")
                )
            )))
        }

        "return mismatch for difference on null vs missing when strict null checking enabled (extra)" {
            val firstJson = "nulls-diff/first.json".loadAsJsonTree()
            val secondJson = "nulls-diff/second.json".loadAsJsonTree()

            val comparator = jsonComparator {
                compareNullsStrictly()
            }

            val diff = comparator.diff(secondJson, firstJson)

            assertThat(diff, hasSize(equalTo(1)) and allElements(cast(
                has(
                    ComparisonMismatch.StructuralMismatch::extraFields,
                    hasElement("a")
                )
            )))
        }

        "return type mismatch for unequal types" {
            val firstJson = createJsonNode(1)
            val secondJson = createJsonNode("1")

            val comparator = jsonComparator()

            val diff = comparator.diff(firstJson, secondJson)

            assertThat(diff, hasSize(equalTo(1)) and allElements(cast(
                has(ComparisonMismatch.TypeMismatch::path, equalTo(ComparisonPath()))
            )))
        }

        "correctly work with nulls" {
            val comparator = jsonComparator {
                compareNullsStrictly()
            }

            assertThat(
                comparator.diff(createJsonNode(null), createJsonNode("")),
                hasSize(equalTo(1)) and allElements(cast(
                    has(ComparisonMismatch.ExpectedNull::path, equalTo(ComparisonPath()))
                ))
            )

            assertThat(
                comparator.diff(createJsonNode(""), createJsonNode(null)),
                hasSize(equalTo(1)) and allElements(cast(
                    has(ComparisonMismatch.MissingValue::path, equalTo(ComparisonPath()))
                ))
            )

            assertThat(
                comparator.diff(createJsonNode(null), MissingNode.getInstance()),
                hasSize(equalTo(1)) and allElements(cast(
                    has(ComparisonMismatch.ExpectedNull::path, equalTo(ComparisonPath()))
                ))
            )

            assertThat(
                comparator.diff(MissingNode.getInstance(), createJsonNode(null)),
                hasSize(equalTo(1)) and allElements(cast(
                    has(ComparisonMismatch.UnexpectedValue::path, equalTo(ComparisonPath()))
                ))
            )
        }
    }
})
