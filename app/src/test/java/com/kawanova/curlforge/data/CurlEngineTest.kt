package com.kawanova.curlforge.data

import org.junit.Assert.*
import org.junit.Test

class CurlEngineTest {

    @Test
    fun testTokenizeSimple() {
        val input = "curl -X GET https://api.example.com/items"
        val tokens = CurlEngine.tokenize(input)
        assertEquals(listOf("curl", "-X", "GET", "https://api.example.com/items"), tokens)
    }

    @Test
    fun testTokenizeWithQuotesAndEscapes() {
        val input = """curl -X POST "https://api.example.com/login" \
  -H 'Content-Type: application/json' \
  -d '{"username": "john", "role": "admin"}'"""
        val tokens = CurlEngine.tokenize(input)
        assertTrue(tokens.contains("-X"))
        assertTrue(tokens.contains("POST"))
        assertTrue(tokens.contains("https://api.example.com/login"))
        assertTrue(tokens.contains("-H"))
        assertTrue(tokens.contains("Content-Type: application/json"))
        assertTrue(tokens.contains("-d"))
        assertTrue(tokens.contains("{\"username\": \"john\", \"role\": \"admin\"}"))
    }

    @Test
    fun testParseGetRequest() {
        val input = "curl https://api.github.com/user -H \"Authorization: Bearer token123\""
        val req = CurlEngine.parseCurl(input)
        assertEquals("GET", req.method)
        assertEquals("https://api.github.com/user", req.url)
        assertEquals(1, req.headers.size)
        assertEquals("Authorization", req.headers[0].key)
        assertEquals("Bearer token123", req.headers[0].value)
        assertNull(req.body)
    }

    @Test
    fun testParsePostJsonRequest() {
        val input = """curl -X POST https://api.openai.com/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model":"gpt-4o"}'"""
        val req = CurlEngine.parseCurl(input)
        assertEquals("POST", req.method)
        assertEquals("https://api.openai.com/v1/chat/completions", req.url)
        assertEquals(1, req.headers.size)
        assertEquals("Content-Type", req.headers[0].key)
        assertEquals("application/json", req.headers[0].value)
        assertEquals("{\"model\":\"gpt-4o\"}", req.body)
        assertTrue(req.isJsonBody)
    }

    @Test
    fun testParseAuthAndRedirect() {
        val input = "curl -u admin:secret -L --max-time 15 https://example.com/redirect"
        val req = CurlEngine.parseCurl(input)
        assertEquals("admin", req.authUser)
        assertEquals("secret", req.authPass)
        assertTrue(req.followRedirects)
        assertEquals(15, req.timeoutSeconds)
    }

    @Test
    fun testBuildCurl() {
        val req = ParsedCurlRequest(
            method = "POST",
            url = "https://httpbin.org/post",
            headers = listOf(HeaderItem("Content-Type", "application/json")),
            body = "{\"test\": true}",
            followRedirects = true,
            timeoutSeconds = 10
        )
        val curlCmd = CurlEngine.buildCurl(req)
        assertTrue(curlCmd.contains("curl"))
        assertTrue(curlCmd.contains("-X POST"))
        assertTrue(curlCmd.contains("https://httpbin.org/post"))
        assertTrue(curlCmd.contains("-H \"Content-Type: application/json\""))
        assertTrue(curlCmd.contains("-L"))
        assertTrue(curlCmd.contains("--max-time 10"))
        assertTrue(curlCmd.contains("-d '{\"test\": true}'"))
    }

    @Test
    fun testGenerateCodeAllLanguages() {
        val req = ParsedCurlRequest(
            method = "POST",
            url = "https://api.example.com/v1/data",
            headers = listOf(
                HeaderItem("Authorization", "Bearer sample_token"),
                HeaderItem("Content-Type", "application/json")
            ),
            body = "{\"key\": \"value\"}",
            isJsonBody = true,
            authUser = "myuser",
            authPass = "mypass",
            timeoutSeconds = 30
        )

        TargetLanguage.values().forEach { lang ->
            val code = CurlEngine.generateCode(req, lang)
            assertNotNull(code)
            assertTrue("Generated code for $lang must not be blank", code.isNotBlank())
            assertTrue("Generated code for $lang must contain target URL", code.contains("https://api.example.com/v1/data"))
        }

        // Python requests specific
        val pyCode = CurlEngine.generateCode(req, TargetLanguage.PYTHON_REQUESTS)
        assertTrue(pyCode.contains("import requests"))
        assertTrue(pyCode.contains("requests.post"))

        // JS Fetch specific
        val jsCode = CurlEngine.generateCode(req, TargetLanguage.JS_FETCH)
        assertTrue(jsCode.contains("fetch("))
        assertTrue(jsCode.contains("method: 'POST'"))

        // Go net/http specific
        val goCode = CurlEngine.generateCode(req, TargetLanguage.GO_NET_HTTP)
        assertTrue(goCode.contains("http.NewRequest(\"POST\""))

        // Kotlin OkHttp specific
        val ktCode = CurlEngine.generateCode(req, TargetLanguage.KOTLIN_OKHTTP)
        assertTrue(ktCode.contains("OkHttpClient"))
        assertTrue(ktCode.contains("Request.Builder()"))

        // PHP specific
        val phpCode = CurlEngine.generateCode(req, TargetLanguage.PHP_CURL)
        assertTrue(phpCode.contains("curl_init()"))
        assertTrue(phpCode.contains("CURLOPT_URL"))

        // Rust specific
        val rustCode = CurlEngine.generateCode(req, TargetLanguage.RUST_REQWEST)
        assertTrue(rustCode.contains("reqwest::Client"))
    }

    @Test
    fun testPresetsAndGlossary() {
        val presets = CurlEngine.getPresets()
        assertTrue("Presets should have at least 5 items", presets.size >= 5)
        presets.forEach { preset ->
            assertTrue(preset.id.isNotBlank())
            assertTrue(preset.nameJa.isNotBlank())
            assertTrue(preset.curlCommand.startsWith("curl"))
        }

        val glossary = CurlEngine.getCurlOptionDocs()
        assertTrue("Glossary should have at least 15 items", glossary.size >= 15)
        assertTrue(glossary.any { it.flag == "-X" })
        assertTrue(glossary.any { it.flag == "-H" })
        assertTrue(glossary.any { it.flag == "-d" })
    }
}
