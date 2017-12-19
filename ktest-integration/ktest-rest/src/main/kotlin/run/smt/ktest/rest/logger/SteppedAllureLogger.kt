package run.smt.ktest.rest.logger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import org.slf4j.LoggerFactory
import run.smt.ktest.allure.attach
import run.smt.ktest.allure.step
import run.smt.ktest.json.deserialize
import run.smt.ktest.json.dump

class SteppedAllureLogger : Logger {
    companion object {
        private val logger = LoggerFactory.getLogger(SteppedAllureLogger::class.java)
    }

    override fun log(request: FilterableRequestSpecification, response: Response) {
        "HTTP ${request.method} ${request.derivedPath} --> ${response.statusLine}" step {
            "request" step {
                if (!request.queryParams.isEmpty()) {
                    attach("query-parameters", request.queryParams.dump(), "application/json")
                }
                val contentType = purifyContentType(request.contentType)
                val requestBody = request.getBody<Any>()
                val body: Any? = when {
                    contentType?.equals("application/json", ignoreCase = true) == true -> when (requestBody) {
                        is String -> beautify(requestBody)
                        is ByteArray -> beautify(requestBody.toString(charset = Charsets.UTF_8))
                        else -> requestBody
                    }
                    else -> requestBody
                }
                val bodyString = requestBody?.let { body as? String ?: body.dump() }

                body?.let { attach("body", it,  contentType ?: "application/json") }

                attach("headers", request.headersString, "text/plain")
                attach("cURL", "curl -X ${request.method} '${request.uri}' ${curlHeaders(request)} ${curlBody(bodyString)}")
            }

            "response" step {
                val contentType = purifyContentType(response.contentType)
                val bodyAsString = if (contentType?.equals("application/json", ignoreCase = true) == true) {
                    beautify(response.asString())
                } else {
                    response.asString()
                }
                attach("body", bodyAsString, contentType ?: "application/json")
                attach("headers", response.headersString, "text/plain")

                try {
                    val developerMessageNode = (bodyAsString deserialize JsonNode::class).path("developerMessage")
                    if (response.statusCode >= 400 && developerMessageNode is TextNode) {
                        attach("stacktrace", developerMessageNode.textValue())
                    }
                } catch (e: Exception) {
                    // never mind...
                }
            }
        }
    }

    private fun curlBody(bodyString: String?) =
        bodyString?.let { "--data-raw '${it.replace("\'", "\\\'")}'" } ?: ""

    private fun purifyContentType(contentType: String?): String? = contentType?.replace(";.*$".toRegex(), "")

    private fun curlHeaders(request: FilterableRequestSpecification) =
        request.headers.asList().joinToString(" ") { "-H '${it.name}: ${it.value}'" }

    private val FilterableRequestSpecification.headersString
        get() = headers.joinToString("\n") { "${it.name}: ${it.value}" }

    private fun beautify(value: String) = try {
        value.deserialize<JsonNode>().dump()
    } catch (e: Exception) {
        logger.info("Failed to beautify json due to:", e)
        value
    }

    private val Response.headersString
        get() = headers.joinToString("\n") { "${it.name}: ${it.value}" }
}
