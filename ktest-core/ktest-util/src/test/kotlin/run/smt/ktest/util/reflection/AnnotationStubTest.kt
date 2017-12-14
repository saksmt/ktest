package run.smt.ktest.util.reflection

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.should.describedAs
import com.natpryce.hamkrest.should.shouldMatch
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

class AnnotationStubTest {
    @Test
    fun `test Annotation helper with map-like arguments`() {
        val deprecationMessage = "deprecation message"
        val replacementExpression = "test replacement expression"
        val annotation = a<Deprecated>(
            "message" to deprecationMessage,
            "replaceWith" to a<ReplaceWith>(
                "expression" to replacementExpression
            )
        )

        assert.that(annotation, isA<Deprecated>())
        assert.that(annotation.message, equalTo(deprecationMessage))
        assert.that(annotation.level, equalTo(DeprecationLevel.WARNING))
        assert.that(annotation.replaceWith, isA<ReplaceWith>())
        assert.that(annotation.replaceWith.expression, equalTo(replacementExpression))
        assert.that(annotation.replaceWith.imports.isEmpty(), equalTo(true))
    }

    @Test
    fun `test AnnotationHelper with no arguments`() {
        val annotation = a<Test>()

        assert.that(annotation, isA<Test>())
        assert.that(annotation.expected, present())
        assertEquals(annotation.expected, Test.None::class)
        assert.that(annotation.timeout, equalTo(0L))
    }

    @Test
    fun `test AnnotationHelper with single argument`() {
        val ignoreMessage = "ignore message"
        val annotation = a<Ignore>(ignoreMessage)

        assert.that(annotation, isA<Ignore>())
        assert.that(annotation.value, equalTo(ignoreMessage))
    }

    @Test
    fun `test AnnotationHelper #equals`() {
        val annotation = a<Ignore>()
        val annotationToCompareWith = a<Ignore>()

        assert.that(annotation, equalTo(annotationToCompareWith))
    }

    @Test
    fun `test AnnotationHelper #toString`() {

        @Deprecated(message = "testMessage")
        class ClassToExtractRealAnnotation

        val expectedAnnotation = ClassToExtractRealAnnotation::class.annotations.first()
        val actual = a<Deprecated>("message" to "testMessage")

        // Can't just use equals, cause result strings have different order of properties
        actual.toString().toSet() - expectedAnnotation.toString().toSet() describedAs "Strings are similar" shouldMatch isEmpty
    }

    @Test
    fun `test AnnotationHelper single argument array`() {
        val testValue = "test value"
        val annotation = a<TestAnnotation>("test" to testValue)

        annotation shouldMatch isA<TestAnnotation>()
        annotation.test shouldMatch present()
        annotation.test.size shouldMatch equalTo(1)
        annotation.test.first() shouldMatch equalTo(testValue)
    }

    annotation class TestAnnotation(vararg val test: String)
}
