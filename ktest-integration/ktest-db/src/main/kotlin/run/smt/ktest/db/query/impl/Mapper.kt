package run.smt.ktest.db.query.impl

import org.slf4j.LoggerFactory
import run.smt.ktest.db.mapping.Column
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Maps SQL result set / callable statement to given Java type
 */
@Suppress("UNCHECKED_CAST")
internal class Mapper<out T : Any>(private val mapTo: KClass<T>) {
    companion object {
        private val log = LoggerFactory.getLogger(Mapper::class.java)
    }

    fun map(rs: SqlOutputAdapter?)
        = rs?.let { tryToFillUpByConstructor(it) ?: tryToFillUpThroughProperties(it) }

    /**
     * Try to map through constructor
     */
    private fun tryToFillUpByConstructor(rs: SqlOutputAdapter): T? {
        val metaData = rs.metaData ?: return null
        val matchingConstructor = mapTo.java.declaredConstructors.asSequence()
            .filter { it.parameters.size == metaData.columnCount }
            .firstOrNull() ?: return null

        val result: Any
        try {
            val arguments = (1..metaData.columnCount).map { rs.getObject(it) }
            val preparedArguments = prepareArguments(arguments, matchingConstructor.parameterTypes!!)
            result = matchingConstructor.newInstance(*preparedArguments.toTypedArray())
        } catch (e: Exception) {
            log.info("Failed to populate db result through constructor", e)
            return null
        }
        return result as? T
    }

    /**
     * Map list of SQL values to list of requested Java values
     */
    private fun prepareArguments(arguments: List<Any>, parameterTypes: Array<Class<*>>): List<Any> {
        return arguments.asSequence()
            .zip(parameterTypes.asSequence())
            .map { it.first coerceTo it.second }
            .toList()
    }

    /**
     * Try to map though property setters
     */
    private fun tryToFillUpThroughProperties(rs: SqlOutputAdapter): T? {
        val metaData = rs.metaData ?: return null
        val result = mapTo.java.newInstance()
        val props = mapTo.memberProperties

        val propNames = props.flatMap { extractColumnNames(it.annotations) + listOf(it.name) }

        for (columnIndex in 1..metaData.columnCount) {
            val columnName = metaData.getColumnLabel(columnIndex) ?: metaData.getColumnName(columnIndex)
            if (columnName !in propNames) {
                continue
            }
            val prop = props.find { it.suitableForColumn(columnName) } as? KMutableProperty1<T, Any?>
            prop?.set(result, rs.getObject(columnName))
        }

        return result
    }

    /**
     * Check if value from given column name can be written to property
     */
    private fun <T, R> KProperty1<T, R>.suitableForColumn(columnName: String): Boolean {
        return name == columnName || columnName in extractColumnNames(annotations)
    }

    /**
     * Extract list of column names from list of annotations
     */
    private fun extractColumnNames(annotations: List<Annotation>): List<String> {
        return annotations.asSequence()
            .map { it.annotationClass }
            .filterIsInstance(Column::class.java)
            .map { it.name }
            .toList()
    }
}
