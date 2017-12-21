package run.smt.ktest.config

import com.natpryce.hamkrest.closeTo
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.isEmpty
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.should.shouldNotMatch
import com.typesafe.config.Config
import org.junit.Test
import java.time.Duration

class ConfigTest {
    @Test
    fun `test config should be properly loaded`() {
        config.root().unwrapped().entries shouldNotMatch isEmpty
        config shouldMatch equalTo(config)
        config.getString("will-be-overridden") shouldMatch equalTo("I am overridden")
        config.getString("wont-be-overridden") shouldMatch equalTo("value")
    }

    @Test
    fun `test config get extension`() {
        val stringActual: String = config["string-value"]
        stringActual shouldMatch equalTo("string")

        val intActual: Int = config["int-value"]
        intActual shouldMatch equalTo(1)

        val doubleActual: Double = config["double-value"]
        doubleActual shouldMatch closeTo(1.5)

        val configActual: Config = config["object-value"]
        configActual.root().unwrapped() shouldMatch (has<Map<String, Any>, Any?>("a", { it["a"] }, equalTo(3 as Any)) and has<Map<String, Any>, Any?>("b", { it["b"] }, equalTo(4 as Any)))

        val durationActual: Duration = config["duration-value"]
        durationActual shouldMatch equalTo(Duration.ofSeconds(5))

    }

}
