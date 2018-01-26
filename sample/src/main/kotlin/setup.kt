import org.mockserver.client.server.MockServerClient
import run.smt.ktest.db.db
import run.smt.ktest.db.query.ddl
import run.smt.ktest.util.resource.loadAsString

val mockServer = MockServerClient("localhost", 10010)

val testDb by lazy {
    "test".db {
        ddl("schema.sql".loadAsString())
    }
    "test"
}

val testData = UserTestDataRegistry()
