package run.smt.ktest.json.matcher.api

class ComparisonPath {
    var expectedPath: String = "$"
        private set(value) { field = value }
    var actualPath: String = "$"
        private set(value) { field = value }

    operator fun plus(pathElem: String): ComparisonPath {
        val newPath = ComparisonPath()
        newPath.expectedPath = expectedPath + ".$pathElem"
        newPath.actualPath = actualPath + ".$pathElem"
        return newPath
    }

    fun appendExpected(pathElem: String): ComparisonPath {
        val newPath = ComparisonPath()
        newPath.expectedPath = expectedPath + ".$pathElem"
        newPath.actualPath = actualPath
        return newPath
    }

    fun appendActual(pathElem: String): ComparisonPath {
        val newPath = ComparisonPath()
        newPath.expectedPath = expectedPath
        newPath.actualPath = actualPath + ".$pathElem"
        return newPath
    }

    override fun toString(): String {
        return "Expected path: $expectedPath; Actual path: $actualPath"
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (
            other is ComparisonPath
                && other.actualPath == actualPath
                && other.expectedPath == expectedPath
        )
    }

    override fun hashCode(): Int {
        var result = expectedPath.hashCode()
        result = 31 * result + actualPath.hashCode()
        return result
    }
}
