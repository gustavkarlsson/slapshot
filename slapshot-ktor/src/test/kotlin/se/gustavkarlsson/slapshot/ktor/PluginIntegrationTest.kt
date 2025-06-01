package se.gustavkarlsson.slapshot.ktor

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.ContentType.Text
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.parameters
import io.ktor.http.withCharset
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.gustavkarlsson.slapshot.junit5.JUnit5SnapshotContext
import se.gustavkarlsson.slapshot.junit5.SnapshotExtension

@ExtendWith(SnapshotExtension::class)
class PluginIntegrationTest {
    private lateinit var snapshotContext: JUnit5SnapshotContext

    @BeforeEach
    fun setUp(snapshotContext: JUnit5SnapshotContext) {
        this.snapshotContext = snapshotContext
    }

    @Test
    fun `get empty`() =
        testSnapshotting(
            configureRouting = {
                get("/") {
                    call.respond(HttpStatusCode.OK)
                }
            },
        ) {
            get("/")
        }

    @Test
    fun `post and receive plain text`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText("Hello, Client!")
                }
            },
        ) {
            post("/") {
                setBody("Hello, Server")
                accept(Text.Any)
            }
        }

    @Test
    fun `post and receive empty plain text`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText("")
                }
            },
        ) {
            post("/") {
                setBody("")
                accept(Text.Any)
            }
        }

    @Test
    fun `post and receive empty binary`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondBytes(byteArrayOf(), Application.OctetStream)
                }
            },
        ) {
            post("/") {
                setBody(byteArrayOf())
                accept(Application.OctetStream)
            }
        }

    @Test
    fun `post and receive json primitive string`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText(
                        text = "\"Hello, Client\"",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            post("/") {
                setBody("\"Hello, Server\"")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `post and receive json primitive empty string`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText(
                        text = "\"\"",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            post("/") {
                setBody("\"\"")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `post and receive json primitive integer`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText(
                        text = "7",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            post("/") {
                setBody("5")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `post and receive json primitive float`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText(
                        text = "7.7",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            post("/") {
                setBody("5.5")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `post and receive json primitive null`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText(
                        text = "null",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            post("/") {
                setBody("null")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `post and receive json object`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText(
                        text = """{ "message": "Hello, Client", "length": 13 }""",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            post("/") {
                setBody("""{ "message": "Hello, Server", "length": 13 }""")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `post and receive json array`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondText(
                        text = """[5, "six", 7.8]""",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            post("/") {
                setBody("""[1, "two", 3.4]""")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `post empty json`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respond(HttpStatusCode.OK)
                }
            },
        ) {
            post("/") {
                setBody("")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `post invalid json`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respond(HttpStatusCode.OK)
                }
            },
        ) {
            post("/") {
                setBody("[")
                contentType(Application.Json.withCharset(Charsets.UTF_8))
                accept(Application.Json)
            }
        }

    @Test
    fun `receive empty json`() =
        testSnapshotting(
            configureRouting = {
                get("/") {
                    call.respondText(
                        text = "",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            get("/") {
                accept(Application.Json)
            }
        }

    @Test
    fun `receive invalid json`() =
        testSnapshotting(
            configureRouting = {
                get("/") {
                    call.respondText(
                        text = "[",
                        contentType = Application.Json.withCharset(Charsets.UTF_8),
                    )
                }
            },
        ) {
            get("/") {
                accept(Application.Json)
            }
        }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `post and receive binary`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respondBytes(
                        "48656c6c6f2c20436c69656e74".hexToByteArray(),
                        Application.OctetStream,
                    )
                }
            },
        ) {
            post("/") {
                setBody("48656c6c6f2c20536572766572".hexToByteArray())
            }
        }

    @Test
    fun `post form data`() =
        testSnapshotting(
            configureRouting = {
                post("/") {
                    call.respond(HttpStatusCode.Created)
                }
            },
        ) {
            submitForm(
                url = "/",
                formParameters =
                    parameters {
                        append("username", "JetBrains")
                        append("email", "example@jetbrains.com")
                        append("password", "foobar")
                        append("tags", "foo")
                        append("tags", "bar")
                    },
            )
        }

    @Test
    fun `post form data in query`() =
        testSnapshotting(
            configureRouting = {
                get("/") {
                    call.respond(HttpStatusCode.Created)
                }
            },
        ) {
            submitForm(
                url = "/",
                formParameters =
                    parameters {
                        append("username", "JetBrains")
                        append("email", "example@jetbrains.com")
                        append("password", "foobar")
                        append("tags", "foo")
                        append("tags", "bar")
                        append("weird", "quote > \" < quote")
                    },
                encodeInQuery = true,
            )
        }

    @Test
    fun `complex path`() =
        testSnapshotting(
            configureRouting = {
                get("/some/complex/path") {
                    call.respond(HttpStatusCode.OK)
                }
            },
        ) {
            get("/some/complex/path") {
                parameter("key", "value")
            }
        }

    @Test
    fun `skipped headers`() =
        testSnapshotting(
            configureRouting = {
                get("/") {
                    call.response.header("Include-From-Response", "included")
                    call.response.header("Ignore-From-Response", "ignored")
                    call.respond(HttpStatusCode.OK)
                }
            },
            configurePlugin = {
                skipResponseHeaders += "Ignore-From-Response"
                skipRequestHeaders += "Ignore-From-Request"
            },
        ) {
            get("/") {
                header("Include-From-Request", "included")
                header("Ignore-From-Request", "ignored")
            }
        }

    @Test
    fun `multi value headers`() =
        testSnapshotting(
            configureRouting = {
                get("/") {
                    call.response.header("Multi-Response", "a")
                    call.response.header("Multi-Response", "b")
                    call.respond(HttpStatusCode.OK)
                }
            },
        ) {
            get("/") {
                header("Multi-Request", "a")
                header("Multi-Request", "b")
            }
        }

    @Test
    fun `not found error`() =
        testSnapshotting(
            configureRouting = {},
        ) {
            get("/")
        }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `upload file`() =
        testSnapshotting(
            configureRouting = {
                post {
                    call.respond(HttpStatusCode.OK)
                }
            },
            configurePlugin = {
                skipRequestHeaders.add("Content-Type") // Boundary is not deterministic
            },
        ) {
            submitFormWithBinaryData(
                url = "/",
                formData =
                    formData {
                        append("description", "Ktor logo")
                        append(
                            "image",
                            "48656c6c6f2c20536572766572".hexToByteArray(),
                            headers {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                            },
                        )
                    },
            )
        }

    private fun testSnapshotting(
        configureRouting: Routing.() -> Unit,
        configurePlugin: SnapshotTestingConfig.() -> Unit = {},
        block: suspend HttpClient.() -> Unit,
    ) {
        testApplication {
            application { routing(configureRouting) }
            val client =
                createClient {
                    install(SnapshotTesting(snapshotContext), configurePlugin)
                }
            block(client)
        }
    }
}
