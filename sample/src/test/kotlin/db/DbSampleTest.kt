package db

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import entity.User
import org.junit.runner.RunWith
import run.smt.ktest.db.db
import run.smt.ktest.db.query.select
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import run.smt.ktest.specs.AllureSpec
import testData
import testDb

@RunWith(KTestJUnitRunner::class)
object DbSampleTest : AllureSpec({
    feature("DB usage") {
        story("TestDataRegistry usage") {
            case("test single") {
                val testDataUser: User = testData["test-data/singleUser.json"]

                val actualUser = testDb.db {
                    select<User>("SELECT * FROM users WHERE id = :id") {
                        parameter("id", testDataUser.id)
                    }.single()
                }

                assertThat(testDataUser, equalTo(actualUser))
            }
        }
    }
})
