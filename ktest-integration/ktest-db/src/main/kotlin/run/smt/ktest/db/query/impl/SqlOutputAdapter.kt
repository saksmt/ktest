package run.smt.ktest.db.query.impl

import run.smt.ktest.util.functional.Either.*
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*

/**
 * Extracts common interface from `ResultSet` and `CallableStatement`
 */
class SqlOutputAdapter private constructor(
    private val source: Either<ResultSet, CallableStatement>
) {
    val metaData: ResultSetMetaData?
        get() = source.unify({ it.metaData }, { it.metaData })

    constructor(rs: ResultSet) : this(left(rs))
    constructor(cs: CallableStatement) : this(right(cs))

    fun getNClob(columnIndex: Int): NClob
        = source.unify({ it.getNClob(columnIndex) }, { it.getNClob(columnIndex) })

    fun getNClob(columnLabel: String?): NClob
        = source.unify({ it.getNClob(columnLabel) }, { it.getNClob(columnLabel) })

    fun getStatement(): Statement
        = source.unify({ it.statement }, { it })

    fun getDate(columnIndex: Int): Date
        = source.unify({ it.getDate(columnIndex) }, { it.getDate(columnIndex) })

    fun getDate(columnLabel: String?): Date
        = source.unify({ it.getDate(columnLabel) }, { it.getDate(columnLabel) })

    fun getDate(columnIndex: Int, cal: Calendar?): Date
        = source.unify({ it.getDate(columnIndex, cal) }, { it.getDate(columnIndex, cal) })

    fun getDate(columnLabel: String?, cal: Calendar?): Date
        = source.unify({ it.getDate(columnLabel, cal) }, { it.getDate(columnLabel, cal) })

    fun getWarnings(): SQLWarning
        = source.unify({ it.warnings }, { it.warnings })

    fun close()
        = source.unify({ it.close() }, { it.close() })

    fun getBoolean(columnIndex: Int): Boolean
        = source.unify({ it.getBoolean(columnIndex) }, { it.getBoolean(columnIndex) })

    fun getBoolean(columnLabel: String?): Boolean
        = source.unify({ it.getBoolean(columnLabel) }, { it.getBoolean(columnLabel) })

    @Deprecated("DEPRECATED IN `ResultSet`")
    fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal
        = source.unify({ it.getBigDecimal(columnIndex, scale) }, { it.getBigDecimal(columnIndex, scale) })

    fun getBigDecimal(columnIndex: Int): BigDecimal
        = source.unify({ it.getBigDecimal(columnIndex) }, { it.getBigDecimal(columnIndex) })

    fun getBigDecimal(columnLabel: String?): BigDecimal
        = source.unify({ it.getBigDecimal(columnLabel) }, { it.getBigDecimal(columnLabel) })

    fun getTime(columnIndex: Int): Time
        = source.unify({ it.getTime(columnIndex) }, { it.getTime(columnIndex) })

    fun getTime(columnLabel: String?): Time
        = source.unify({ it.getTime(columnLabel) }, { it.getTime(columnLabel) })

    fun getTime(columnIndex: Int, cal: Calendar?): Time
        = source.unify({ it.getTime(columnIndex, cal) }, { it.getTime(columnIndex, cal) })

    fun getTime(columnLabel: String?, cal: Calendar?): Time
        = source.unify({ it.getTime(columnLabel, cal) }, { it.getTime(columnLabel, cal) })

    fun getSQLXML(columnIndex: Int): SQLXML
        = source.unify({ it.getSQLXML(columnIndex) }, { it.getSQLXML(columnIndex) })

    fun getSQLXML(columnLabel: String?): SQLXML
        = source.unify({ it.getSQLXML(columnLabel) }, { it.getSQLXML(columnLabel) })

    fun <T : Any?> unwrap(iface: Class<T>?): T
        = source.unify({ it.unwrap(iface) }, { it.unwrap(iface) })

    fun getFloat(columnIndex: Int): Float
        = source.unify({ it.getFloat(columnIndex) }, { it.getFloat(columnIndex) })

    fun getFloat(columnLabel: String?): Float
        = source.unify({ it.getFloat(columnLabel) }, { it.getFloat(columnLabel) })

    fun getURL(columnIndex: Int): URL
        = source.unify({ it.getURL(columnIndex) }, { it.getURL(columnIndex) })

    fun getURL(columnLabel: String?): URL
        = source.unify({ it.getURL(columnLabel) }, { it.getURL(columnLabel) })

    fun getBlob(columnIndex: Int): Blob
        = source.unify({ it.getBlob(columnIndex) }, { it.getBlob(columnIndex) })

    fun getBlob(columnLabel: String?): Blob
        = source.unify({ it.getBlob(columnLabel) }, { it.getBlob(columnLabel) })

    fun getByte(columnIndex: Int): Byte
        = source.unify({ it.getByte(columnIndex) }, { it.getByte(columnIndex) })

    fun getByte(columnLabel: String?): Byte
        = source.unify({ it.getByte(columnLabel) }, { it.getByte(columnLabel) })

    fun getString(columnIndex: Int): String
        = source.unify({ it.getString(columnIndex) }, { it.getString(columnIndex) })

    fun getString(columnLabel: String?): String
        = source.unify({ it.getString(columnLabel) }, { it.getString(columnLabel) })

    fun getObject(columnIndex: Int): Any?
        = source.unify({ it.getObject(columnIndex) }, { it.getObject(columnIndex) })

    fun getObject(columnLabel: String?): Any
        = source.unify({ it.getObject(columnLabel) }, { it.getObject(columnLabel) })

    fun getObject(columnIndex: Int, map: MutableMap<String, Class<*>>?): Any
        = source.unify({ it.getObject(columnIndex, map) }, { it.getObject(columnIndex, map) })

    fun getObject(columnLabel: String?, map: MutableMap<String, Class<*>>?): Any
        = source.unify({ it.getObject(columnLabel, map) }, { it.getObject(columnLabel, map) })

    fun <T : Any?> getObject(columnIndex: Int, type: Class<T>?): T
        = source.unify({ it.getObject(columnIndex, type) }, { it.getObject(columnIndex, type) })

    fun <T : Any?> getObject(columnLabel: String?, type: Class<T>?): T
        = source.unify({ it.getObject(columnLabel, type) }, { it.getObject(columnLabel, type) })

    fun getLong(columnIndex: Int): Long
        = source.unify({ it.getLong(columnIndex) }, { it.getLong(columnIndex) })

    fun getLong(columnLabel: String?): Long
        = source.unify({ it.getLong(columnLabel) }, { it.getLong(columnLabel) })

    fun getClob(columnIndex: Int): Clob
        = source.unify({ it.getClob(columnIndex) }, { it.getClob(columnIndex) })

    fun getClob(columnLabel: String?): Clob
        = source.unify({ it.getClob(columnLabel) }, { it.getClob(columnLabel) })

    fun isClosed(): Boolean
        = source.unify({ it.isClosed }, { it.isClosed })

    fun getNString(columnIndex: Int): String
        = source.unify({ it.getNString(columnIndex) }, { it.getNString(columnIndex) })

    fun getNString(columnLabel: String?): String
        = source.unify({ it.getNString(columnLabel) }, { it.getNString(columnLabel) })

    fun getArray(columnIndex: Int): Array
        = source.unify({ it.getArray(columnIndex) }, { it.getArray(columnIndex) })

    fun getArray(columnLabel: String?): Array
        = source.unify({ it.getArray(columnLabel) }, { it.getArray(columnLabel) })

    fun getCharacterStream(columnIndex: Int): Reader
        = source.unify({ it.getCharacterStream(columnIndex) }, { it.getCharacterStream(columnIndex) })

    fun getCharacterStream(columnLabel: String?): Reader
        = source.unify({ it.getCharacterStream(columnLabel) }, { it.getCharacterStream(columnLabel) })

    fun getShort(columnIndex: Int): Short
        = source.unify({ it.getShort(columnIndex) }, { it.getShort(columnIndex) })

    fun getShort(columnLabel: String?): Short
        = source.unify({ it.getShort(columnLabel) }, { it.getShort(columnLabel) })

    fun getTimestamp(columnIndex: Int): Timestamp
        = source.unify({ it.getTimestamp(columnIndex) }, { it.getTimestamp(columnIndex) })

    fun getTimestamp(columnLabel: String?): Timestamp
        = source.unify({ it.getTimestamp(columnLabel) }, { it.getTimestamp(columnLabel) })

    fun getTimestamp(columnIndex: Int, cal: Calendar?): Timestamp
        = source.unify({ it.getTimestamp(columnIndex, cal) }, { it.getTimestamp(columnIndex, cal) })

    fun getTimestamp(columnLabel: String?, cal: Calendar?): Timestamp
        = source.unify({ it.getTimestamp(columnLabel, cal) }, { it.getTimestamp(columnLabel, cal) })

    fun getRef(columnIndex: Int): Ref
        = source.unify({ it.getRef(columnIndex) }, { it.getRef(columnIndex) })

    fun getRef(columnLabel: String?): Ref
        = source.unify({ it.getRef(columnLabel) }, { it.getRef(columnLabel) })

    fun getNCharacterStream(columnIndex: Int): Reader
        = source.unify({ it.getNCharacterStream(columnIndex) }, { it.getNCharacterStream(columnIndex) })

    fun getNCharacterStream(columnLabel: String?): Reader
        = source.unify({ it.getNCharacterStream(columnLabel) }, { it.getNCharacterStream(columnLabel) })

    fun getBytes(columnIndex: Int): ByteArray
        = source.unify({ it.getBytes(columnIndex) }, { it.getBytes(columnIndex) })

    fun getBytes(columnLabel: String?): ByteArray
        = source.unify({ it.getBytes(columnLabel) }, { it.getBytes(columnLabel) })

    fun getDouble(columnIndex: Int): Double
        = source.unify({ it.getDouble(columnIndex) }, { it.getDouble(columnIndex) })

    fun getDouble(columnLabel: String?): Double
        = source.unify({ it.getDouble(columnLabel) }, { it.getDouble(columnLabel) })

    fun isWrapperFor(iface: Class<*>?): Boolean
        = source.unify({ it.isWrapperFor(iface) }, { it.isWrapperFor(iface) })

    fun getInt(columnIndex: Int): Int
        = source.unify({ it.getInt(columnIndex) }, { it.getInt(columnIndex) })

    fun getInt(columnLabel: String?): Int
        = source.unify({ it.getInt(columnLabel) }, { it.getInt(columnLabel) })

    fun getRowId(columnIndex: Int): RowId
        = source.unify({ it.getRowId(columnIndex) }, { it.getRowId(columnIndex) })

    fun getRowId(columnLabel: String?): RowId
        = source.unify({ it.getRowId(columnLabel) }, { it.getRowId(columnLabel) })

    fun clearWarnings()
        = source.unify({ it.clearWarnings() }, { it.clearWarnings() })
}
