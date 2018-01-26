import entity.User
import run.smt.ktest.db.db
import run.smt.ktest.db.query.delete
import run.smt.ktest.db.query.insert
import run.smt.ktest.db.query.select
import run.smt.ktest.db.query.update
import run.smt.ktest.db.registry.TestDataRegistry
import run.smt.ktest.json.loadAsJson
import kotlin.reflect.KClass

class UserTestDataRegistry : TestDataRegistry() {
    override fun <T : Any> load(clazz: KClass<T>, identifier: String): T? {
        return identifier.loadAsJson { simple(clazz) }
    }

    override fun <T : Any> loadAll(clazz: KClass<T>, identifier: String): List<T> {
        return identifier.loadAsJson { list(simple(clazz)) }
    }

    override fun <T : Any> save(data: T) {
        if (data !is User) {
            throw IllegalArgumentException("Only user entity is supported")
        }

        testDb.db {
            val existing = select<User> {
                query = "SELECT * FROM users WHERE id = :id"
                parametersFrom(data)
            }.single()

            if (existing == null) {
                insert {
                    query = """
                        INSERT INTO users
                               (id, first_name, second_name, surname, age)
                        VALUES (:id, :firstName, :secondName, :surname, :age)
                        """.trimIndent()
                    parametersFrom(data)
                }
            } else {
                update {
                    query = "UPDATE users(first_name, second_name, surname, age) SET VALUES(:firstName, :secondName, :surname, :age) WHERE id = :id"
                    parametersFrom(data)
                }
            }
        }
    }

    override fun <T : Any> remove(data: T) {
        testDb.db {
            delete {
                query = "DELETE FROM users WHERE id = :id"
                parametersFrom(data)
            }
        }
    }
}
