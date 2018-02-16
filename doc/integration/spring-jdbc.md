# [kTest](../README.md) :: [Integration](README.md) :: Spring JDBC

## Download

### Gradle

```groovy
'run.smt.ktest:ktest-db'
```

### Maven

```xml
<dependency>
    <groupId>run.smt.ktest</groupId>
    <artifactId>ktest-db</artifactId>
</dependency>
```

## Description

Integration with Spring JDBC + HikariCP

### Configuration

This module looks for `db.$DATABASENAME` node in global config and extracts it's children
(`url`, `username` and `password`), optionally you can also specify max pool size
through `pool-size` and connection timeout through `connection-timeout`

#### Example

```hocon
db {
  appDb {
    url = "jdbc:oracle:thin:@my-host:1521:ora01"
    driver = "FQCN of JDBC driver"
    username = user
    password = pass
    connection-timeout = 15 seconds # optional, default value = 15 seconds
    pool-size = 5 # optional, default value = 5
  }
  
  // alternatively you can define your connections as following:
  
  appDb = ${oracle} {
    url = "jdbc:oracle:thin:@my-host:1521:ora01"
    username = user
    password = pass
    connection-timeout = 15 seconds # optional, default value = 15 seconds
    pool-size = 5 # optional, default value = 5
  }
  
  // there are also predefined JDBC driver configurations for postgresql, mysql, h2 and sqlite
}

// also you can define default-driver and then use you could create new connection without specifying 
// driver class:
default-driver = ${oracle}
```

### Connect to db

Simple as follows:

```kotlin
import run.smt.ktest.db.db

fun usage1() {
    "databaseName".db {
        // here you're in context of database "databaseName" connection!
    }
    
    "appDb".db {
        // here you're in context of appDb database
    }
}
```

### Select

```kotlin
import run.smt.ktest.db.db
import run.smt.ktest.db.query.select

fun usage2() {
    "app".db {
        // mapping result to POJO
        val singleResult = select<ClassWhichWouldBeUsedForMapping>("SELECT * FROM world WHERE :parameterName IS NOT NULL") {
            parameters["parameterName"] = "hello named parameters!"
            parameter("parameterName", "you also can use method notation for parameters")
            parametersFrom(pojo = youCanAlsoUsePojoAsSourceOfParameters)
            // WARNING: YOU CAN'T MIX SIMPLE NAMED PARAMETERS (such as above) AND PARAMETERS FROM POJO!
            // WARNING: PARAMETERS FROM POJO WILL HAVE HIGHEST PRIORITY SO ALL OTHERS WOULD BE IGNORED!!!
        }.single() // here you say that you want single result
        
        val multipleResults: List<ClassWhichWouldBeUsedForMapping> = select<ClassWhichWouldBeUsedForMapping> {
            query = "SELECT ..." // yeah, it's totally legal to omit parameter and set it later, right inside builder
        }.asList() // here you say that you want multiple results
        
        val responseAsMap = select<Map<String, Any?>> {
            query = "..."
        }.asMap()
        
        val primitiveValue = select<Int> {
            query = "SELECT 1 FROM dual"
        }.single()
        
        val primitiveValueList = select<String> {
            query = "SELECT ..."
        }.asList()
    }
}
```

#### Note about mapping

Mapper firstly tries to map through constructor matching column index with parameter index (actually not tester) then if it fails
it tries to create empty instance and fill it up through property setters

Also you can specify name of column which will successfully match your field through `@run.smt.ktest.db.mapping.Column`:

```kotlin
import run.smt.ktest.db.mapping.Column

// Note default values! Mapper will need default empty constructor for instantiation of POJO for filling up through properties
data class MyPojo( // will try to match result set with (`Long`, `String`) tuple and map it through constructor on success
    @Column("myid")
    var id: Long? = null,  // will try to find column or label with name `myid` or `id`
    var someField: String? = null // will try to find column or label with name `somefield` 
)
```

### INSERT / UPDATE / DELETE

```kotlin
import run.smt.ktest.db.db
import run.smt.ktest.db.query.insert
import run.smt.ktest.db.query.update
import run.smt.ktest.db.query.delete

fun usage3() {
    "app".db {
        insert("INSERT ...") {
            // same syntax for parameters as in SELECT
        }
        
        update("UPDATE ...") {
            // same shit
        }
        
        delete("DELETE ...") {
            // also there
        }
        
        insert {
            query = "INSERT ..." // it works too!
        }
    }
}
```

### Callable Statements

```kotlin
import run.smt.ktest.db.db
import run.smt.ktest.db.query.call
import run.smt.ktest.db.mapping.Column
import run.smt.ktest.util.text.stripMargin
import java.sql.JDBCType

data class MyPojo1(
    var parameter1: String? = null,
    @Column("parameter2")
    var output: Long? = null
)

fun usage4() {
    "app".db {
        val result = call<MyPojo1>("{call my_stored_procedure(:parameter1, :parameter2)}") {
            parameter("parameter1", "value") // if you omit parameter it will be set to null
            // for output parameters you need to specify types!
            outParameter("parameter2", JDBCType.BIGINT) // Yeah, you need to use "BIGINT" to map it to `Long`
        }.single() // list is not available for callable statements!
        
        val result1: Map<String, Any?>? = call<Map<String, Any?>> {
            // you can use lateinit query like everywhere else
            query = """
                | DECLARE
                |   myParam VARCHAR2(100) := :someField
                | BEGIN
                |   :parameter2 := 0;
                | END;
            """.stripMargin()
            
            parametersFrom(MyPojo(someField = "hello")) // it works too!
            
            // you must define all out parameters otherwise it will fail on trying to think that it is in-param
            outParameter("parameter2", JDBCType.BIGINT)
        }.asMap()
    }
}
```

#### Note about mapping

There is no constructor mapper so everything goes through properties

#### Note about parameters

You can't use any indexed parameters, but you can use named one's!

As bonus (cause of hack in implementation, heh) - call's (and only call's!) are allowed to mix `parametersFrom()` and `parameter()` methods

### Bonus: conventional class for registry of test resources that must be cleanly loaded into some storage

#### Configuration

```kotlin
import run.smt.ktest.db.registry.TestDataRegistry
import run.smt.ktest.json.*
import java.io.InputStream
import kotlin.reflect.KClass

// assuming you have some globally accessible variable with name "dao" which can persist your entities
class MyTestDataRegistry(private val loadJsonResource: (String) -> InputStream) : TestDataRegistry() {
    override fun <T : Any> load(clazz: KClass<T>, identifier: String): T? {
        return loadJsonResource(identifier) deserialize clazz.java
    }
        
    override fun <T : Any> loadAll(clazz: KClass<T>, identifier: String): List<T> {
        return loadJsonResource(identifier) deserialize { list(clazz) }
    }

    override fun <T: Any> save(data: T) {
        when (data) {
            is Entity1 -> dao.saveEntity1(listOf(data))
            is Entity2 -> dao.saveEntity2(listOf(data))
            is List<*> -> when (data.first() ?: return) {
                is Entity1 -> dao.saveEntity1(data as List<Entity1>)
                is Entity2 -> dao.saveEntity2(data as List<Entity2>)
                else -> throw SaveException("Unsupported entity (List<${data.first()?.javaClass?.simpleName}>) for save!")
            }
            else -> throw SaveException("Unsupported entity (${data.javaClass.simpleName}) for save!")
        }
    }

    override fun <T: Any> remove(data: T) {
        if (data !is Entity1 && data !is Entity2) {
            return
        }
        dao.removeEntity(data)
    }
}

```

#### Usage

```kotlin
import run.smt.ktest.db.registry.TestDataRegistry
import run.smt.ktest.util.resource.*
import com.fasterxml.jackson.databind.JsonNode

val testData: TestDataRegistry by lazy { MyTestDataRegistry({ resourceName -> "test-data/$resourceName.json".load() }) }

fun usage5() {
    // Insert fresh entity1 into database (from "resources/test-data/some-entity1-json.json")
    testData.setup<Entity1>("some-entity1-json")
    
    // Insert fresh organization into database and retrieve its java value
    val ent1: Entity1? = testData["entity1-json"]
    
    // Usage without inserting into database
    val ent2: Entity2? = testData.load("entity2")
    val sameAsEnt2: JsonNode? = testData.load("entity2")
    val ents: List<Entity2> = testData.loadAll("entities-2")
}
```
