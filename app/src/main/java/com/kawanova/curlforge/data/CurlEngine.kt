package com.kawanova.curlforge.data

import java.util.regex.Pattern

object CurlEngine {

    /**
     * トークン分割関数。
     * クオート（シングル/ダブル）、バックスラッシュ改行、エスケープに対応。
     */
    fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        // 複数行のバックスラッシュ改行を結合
        val normalized = input.replace(Regex("\\\\[\\r\\n]+"), " ").trim()
        
        val sb = StringBuilder()
        var inSingleQuote = false
        var inDoubleQuote = false
        var escape = false

        var i = 0
        while (i < normalized.length) {
            val c = normalized[i]

            if (escape) {
                sb.append(c)
                escape = false
                i++
                continue
            }

            if (c == '\\' && !inSingleQuote) {
                escape = true
                i++
                continue
            }

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote
                i++
                continue
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote
                i++
                continue
            }

            if (c.isWhitespace() && !inSingleQuote && !inDoubleQuote) {
                if (sb.isNotEmpty()) {
                    tokens.add(sb.toString())
                    sb.clear()
                }
                i++
                continue
            }

            sb.append(c)
            i++
        }

        if (sb.isNotEmpty()) {
            tokens.add(sb.toString())
        }

        return tokens
    }

    /**
     * cURLコマンド文字列を解析して ParsedCurlRequest を生成
     */
    fun parseCurl(rawCommand: String): ParsedCurlRequest {
        val tokens = tokenize(rawCommand)
        if (tokens.isEmpty()) {
            return ParsedCurlRequest(rawCommand = rawCommand)
        }

        var method: String? = null
        var url: String? = null
        val headers = mutableListOf<HeaderItem>()
        var body: String? = null
        var authUser: String? = null
        var authPass: String? = null
        var followRedirects = false
        var timeoutSeconds: Int? = null

        var i = 0
        // "curl" コマンドトークンがあればスキップ
        if (tokens.isNotEmpty() && tokens[0].equals("curl", ignoreCase = true)) {
            i = 1
        }

        while (i < tokens.size) {
            val token = tokens[i]

            when {
                token == "-X" || token == "--request" -> {
                    if (i + 1 < tokens.size) {
                        method = tokens[i + 1].uppercase()
                        i += 2
                        continue
                    }
                }
                token == "-H" || token == "--header" -> {
                    if (i + 1 < tokens.size) {
                        val headerStr = tokens[i + 1]
                        val colonIndex = headerStr.indexOf(':')
                        if (colonIndex != -1) {
                            val key = headerStr.substring(0, colonIndex).trim()
                            val value = headerStr.substring(colonIndex + 1).trim()
                            if (key.isNotEmpty()) {
                                headers.add(HeaderItem(key, value))
                            }
                        }
                        i += 2
                        continue
                    }
                }
                token == "-d" || token == "--data" || token == "--data-raw" || token == "--data-binary" || token == "--data-ascii" -> {
                    if (i + 1 < tokens.size) {
                        body = tokens[i + 1]
                        if (method == null) {
                            method = "POST"
                        }
                        i += 2
                        continue
                    }
                }
                token == "-u" || token == "--user" -> {
                    if (i + 1 < tokens.size) {
                        val userPass = tokens[i + 1]
                        val colonIndex = userPass.indexOf(':')
                        if (colonIndex != -1) {
                            authUser = userPass.substring(0, colonIndex)
                            authPass = userPass.substring(colonIndex + 1)
                        } else {
                            authUser = userPass
                            authPass = ""
                        }
                        i += 2
                        continue
                    }
                }
                token == "-L" || token == "--location" -> {
                    followRedirects = true
                    i++
                    continue
                }
                token == "-m" || token == "--max-time" || token == "--connect-timeout" -> {
                    if (i + 1 < tokens.size) {
                        timeoutSeconds = tokens[i + 1].toIntOrNull()
                        i += 2
                        continue
                    }
                }
                token.startsWith("-") -> {
                    // その他のフラグ（引数があるかもしれないフラグ、または単独フラグ）
                    // よくある引数付きフラグ
                    if (token in listOf("-o", "--output", "-A", "--user-agent", "-e", "--referer", "-b", "--cookie", "-c", "--cookie-jar")) {
                        i += 2
                        continue
                    } else {
                        i++
                        continue
                    }
                }
                else -> {
                    // フラグでなければURL候補
                    if (url == null && (token.startsWith("http://") || token.startsWith("https://") || token.contains("."))) {
                        url = token
                    }
                }
            }
            i++
        }

        val resolvedMethod = method ?: if (body != null) "POST" else "GET"
        val resolvedUrl = url ?: ""
        val isJson = body?.trim()?.let { (it.startsWith("{") && it.endsWith("}")) || (it.startsWith("[") && it.endsWith("]")) } ?: false

        return ParsedCurlRequest(
            method = resolvedMethod,
            url = resolvedUrl,
            headers = headers,
            body = body,
            isJsonBody = isJson,
            authUser = authUser,
            authPass = authPass,
            followRedirects = followRedirects,
            timeoutSeconds = timeoutSeconds,
            rawCommand = rawCommand
        )
    }

    /**
     * ParsedCurlRequest から整形された cURL コマンドを生成
     */
    fun buildCurl(request: ParsedCurlRequest): String {
        val parts = mutableListOf<String>()
        parts.add("curl")

        if (request.method.uppercase() != "GET") {
            parts.add("-X ${request.method.uppercase()}")
        }

        val safeUrl = if (request.url.contains(" ") || request.url.contains("&") || request.url.contains("?")) {
            "\"${request.url}\""
        } else if (request.url.isNotEmpty()) {
            request.url
        } else {
            "\"https://example.com/api\""
        }
        parts.add(safeUrl)

        request.headers.filter { it.enabled && it.key.isNotBlank() }.forEach { header ->
            parts.add("-H \"${header.key}: ${header.value.replace("\"", "\\\"")}\"")
        }

        if (!request.authUser.isNullOrEmpty()) {
            val pass = request.authPass ?: ""
            parts.add("-u \"${request.authUser}:$pass\"")
        }

        if (request.followRedirects) {
            parts.add("-L")
        }

        if (request.timeoutSeconds != null && request.timeoutSeconds > 0) {
            parts.add("--max-time ${request.timeoutSeconds}")
        }

        if (!request.body.isNullOrEmpty()) {
            val escapedBody = request.body.replace("'", "'\\''")
            parts.add("-d '$escapedBody'")
        }

        return parts.joinToString(" \\\n  ")
    }

    /**
     * 各ターゲット言語へのコード生成
     */
    fun generateCode(request: ParsedCurlRequest, target: TargetLanguage): String {
        return when (target) {
            TargetLanguage.PYTHON_REQUESTS -> generatePythonRequests(request)
            TargetLanguage.PYTHON_HTTPX -> generatePythonHttpx(request)
            TargetLanguage.JS_FETCH -> generateJsFetch(request)
            TargetLanguage.JS_AXIOS -> generateJsAxios(request)
            TargetLanguage.GO_NET_HTTP -> generateGoNetHttp(request)
            TargetLanguage.KOTLIN_OKHTTP -> generateKotlinOkHttp(request)
            TargetLanguage.KOTLIN_KTOR -> generateKotlinKtor(request)
            TargetLanguage.PHP_CURL -> generatePhpCurl(request)
            TargetLanguage.JAVA_HTTP_CLIENT -> generateJavaHttpClient(request)
            TargetLanguage.RUST_REQWEST -> generateRustReqwest(request)
        }
    }

    private fun generatePythonRequests(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("import requests\n")
        if (req.isJsonBody) {
            sb.append("import json\n")
        }
        sb.append("\n")
        sb.append("url = \"${req.url}\"\n\n")

        val enabledHeaders = req.headers.filter { it.enabled && it.key.isNotBlank() }
        if (enabledHeaders.isNotEmpty()) {
            sb.append("headers = {\n")
            enabledHeaders.forEach { h ->
                sb.append("    \"${h.key}\": \"${h.value.replace("\"", "\\\"")}\",\n")
            }
            sb.append("}\n\n")
        } else {
            sb.append("headers = {}\n\n")
        }

        val methodLower = req.method.lowercase()
        val args = mutableListOf<String>()
        args.add("url")
        if (enabledHeaders.isNotEmpty()) {
            args.add("headers=headers")
        }

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            args.add("auth=(\"${req.authUser}\", \"${pass}\")")
        }

        if (!req.body.isNullOrEmpty()) {
            if (req.isJsonBody) {
                sb.append("payload = ${req.body.trim()}\n\n")
                args.add("json=payload")
            } else {
                sb.append("data = \"\"\"${req.body}\"\"\"\n\n")
                args.add("data=data")
            }
        }

        if (req.timeoutSeconds != null) {
            args.add("timeout=${req.timeoutSeconds}")
        }
        if (!req.followRedirects && (req.method == "GET" || req.method == "POST")) {
            // requests はデフォルトで follow_redirects=True
        } else if (!req.followRedirects) {
            args.add("allow_redirects=False")
        }

        sb.append("response = requests.$methodLower(\n    ")
        sb.append(args.joinToString(",\n    "))
        sb.append("\n)\n\n")
        sb.append("print(f\"Status Code: {response.status_code}\")\n")
        sb.append("print(response.text)\n")
        return sb.toString()
    }

    private fun generatePythonHttpx(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("import httpx\nimport asyncio\n")
        if (req.isJsonBody) sb.append("import json\n")
        sb.append("\nasync def fetch():\n")
        sb.append("    url = \"${req.url}\"\n")
        
        val enabledHeaders = req.headers.filter { it.enabled && it.key.isNotBlank() }
        if (enabledHeaders.isNotEmpty()) {
            sb.append("    headers = {\n")
            enabledHeaders.forEach { h ->
                sb.append("        \"${h.key}\": \"${h.value.replace("\"", "\\\"")}\",\n")
            }
            sb.append("    }\n")
        }

        val methodLower = req.method.lowercase()
        val args = mutableListOf<String>()
        args.add("url")
        if (enabledHeaders.isNotEmpty()) args.add("headers=headers")

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            args.add("auth=(\"${req.authUser}\", \"$pass\")")
        }

        if (!req.body.isNullOrEmpty()) {
            if (req.isJsonBody) {
                sb.append("    payload = ${req.body.trim()}\n")
                args.add("json=payload")
            } else {
                sb.append("    content = \"\"\"${req.body}\"\"\"\n")
                args.add("content=content")
            }
        }

        if (req.followRedirects) {
            args.add("follow_redirects=True")
        }

        sb.append("    async with httpx.AsyncClient() as client:\n")
        sb.append("        res = await client.$methodLower(${args.joinToString(", ")})\n")
        sb.append("        print(res.status_code)\n")
        sb.append("        print(res.text)\n\n")
        sb.append("asyncio.run(fetch())\n")
        return sb.toString()
    }

    private fun generateJsFetch(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("const url = '${req.url}';\n")
        sb.append("const options = {\n")
        sb.append("  method: '${req.method.uppercase()}',\n")

        val enabledHeaders = req.headers.filter { it.enabled && it.key.isNotBlank() }.toMutableList()
        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            val authBasic = java.util.Base64.getEncoder().encodeToString("${req.authUser}:$pass".toByteArray())
            enabledHeaders.add(HeaderItem("Authorization", "Basic $authBasic"))
        }

        if (enabledHeaders.isNotEmpty()) {
            sb.append("  headers: {\n")
            enabledHeaders.forEach { h ->
                sb.append("    '${h.key}': '${h.value.replace("'", "\\'")}',\n")
            }
            sb.append("  },\n")
        }

        if (!req.body.isNullOrEmpty() && req.method.uppercase() != "GET") {
            if (req.isJsonBody) {
                val cleanJson = req.body.trim()
                sb.append("  body: JSON.stringify($cleanJson),\n")
            } else {
                sb.append("  body: `${req.body.replace("`", "\\`")}`,\n")
            }
        }

        if (req.followRedirects) {
            sb.append("  redirect: 'follow',\n")
        }

        sb.append("};\n\n")
        sb.append("fetch(url, options)\n")
        sb.append("  .then(response => {\n")
        sb.append("    console.log('Status:', response.status);\n")
        sb.append("    return response.text();\n")
        sb.append("  })\n")
        sb.append("  .then(data => console.log(data))\n")
        sb.append("  .catch(err => console.error(err));\n")
        return sb.toString()
    }

    private fun generateJsAxios(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("import axios from 'axios';\n\n")
        sb.append("const config = {\n")
        sb.append("  method: '${req.method.lowercase()}',\n")
        sb.append("  url: '${req.url}',\n")

        val enabledHeaders = req.headers.filter { it.enabled && it.key.isNotBlank() }
        if (enabledHeaders.isNotEmpty()) {
            sb.append("  headers: {\n")
            enabledHeaders.forEach { h ->
                sb.append("    '${h.key}': '${h.value.replace("'", "\\'")}',\n")
            }
            sb.append("  },\n")
        }

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            sb.append("  auth: {\n    username: '${req.authUser}',\n    password: '$pass'\n  },\n")
        }

        if (!req.body.isNullOrEmpty()) {
            if (req.isJsonBody) {
                sb.append("  data: ${req.body.trim()},\n")
            } else {
                sb.append("  data: `${req.body.replace("`", "\\`")}`,\n")
            }
        }

        if (req.timeoutSeconds != null) {
            sb.append("  timeout: ${req.timeoutSeconds * 1000},\n")
        }

        sb.append("};\n\n")
        sb.append("async function sendRequest() {\n")
        sb.append("  try {\n")
        sb.append("    const response = await axios(config);\n")
        sb.append("    console.log(response.status);\n")
        sb.append("    console.log(response.data);\n")
        sb.append("  } catch (error) {\n")
        sb.append("    console.error('Request failed:', error);\n")
        sb.append("  }\n")
        sb.append("}\n\n")
        sb.append("sendRequest();\n")
        return sb.toString()
    }

    private fun generateGoNetHttp(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("package main\n\n")
        sb.append("import (\n")
        sb.append("\t\"fmt\"\n")
        sb.append("\t\"io\"\n")
        sb.append("\t\"net/http\"\n")
        if (!req.body.isNullOrEmpty()) {
            sb.append("\t\"strings\"\n")
        }
        sb.append(")\n\n")
        sb.append("func main() {\n")
        sb.append("\turl := \"${req.url}\"\n")

        if (!req.body.isNullOrEmpty()) {
            val escaped = req.body.replace("\"", "\\\"").replace("\n", "\\n")
            sb.append("\tpayload := strings.NewReader(\"$escaped\")\n")
            sb.append("\treq, err := http.NewRequest(\"${req.method.uppercase()}\", url, payload)\n")
        } else {
            sb.append("\treq, err := http.NewRequest(\"${req.method.uppercase()}\", url, nil)\n")
        }
        sb.append("\tif err != nil {\n\t\tpanic(err)\n\t}\n\n")

        val enabledHeaders = req.headers.filter { it.enabled && it.key.isNotBlank() }
        enabledHeaders.forEach { h ->
            sb.append("\treq.Header.Add(\"${h.key}\", \"${h.value.replace("\"", "\\\"")}\")\n")
        }

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            sb.append("\treq.SetBasicAuth(\"${req.authUser}\", \"$pass\")\n")
        }

        sb.append("\n\tres, err := http.DefaultClient.Do(req)\n")
        sb.append("\tif err != nil {\n\t\tpanic(err)\n\t}\n")
        sb.append("\tdefer res.Body.Close()\n\n")
        sb.append("\tbody, err := io.ReadAll(res.Body)\n")
        sb.append("\tif err != nil {\n\t\tpanic(err)\n\t}\n\n")
        sb.append("\tfmt.Println(res.Status)\n")
        sb.append("\tfmt.Println(string(body))\n")
        sb.append("}\n")
        return sb.toString()
    }

    private fun generateKotlinOkHttp(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("import okhttp3.MediaType.Companion.toMediaType\n")
        sb.append("import okhttp3.OkHttpClient\n")
        sb.append("import okhttp3.Request\n")
        sb.append("import okhttp3.RequestBody.Companion.toRequestBody\n\n")
        sb.append("fun main() {\n")
        sb.append("    val client = OkHttpClient()\n\n")

        if (!req.body.isNullOrEmpty() && req.method.uppercase() != "GET") {
            val cType = req.headers.firstOrNull { it.key.equals("content-type", ignoreCase = true) }?.value ?: if (req.isJsonBody) "application/json" else "text/plain"
            sb.append("    val mediaType = \"$cType\".toMediaType()\n")
            val escapedBody = req.body.replace("\"", "\\\"").replace("\$", "\\$")
            sb.append("    val body = \"\"\"$escapedBody\"\"\".toRequestBody(mediaType)\n\n")
        }

        sb.append("    val request = Request.Builder()\n")
        sb.append("        .url(\"${req.url}\")\n")

        val method = req.method.uppercase()
        if (method == "GET") {
            sb.append("        .get()\n")
        } else if (!req.body.isNullOrEmpty()) {
            sb.append("        .method(\"$method\", body)\n")
        } else {
            sb.append("        .method(\"$method\", null)\n")
        }

        req.headers.filter { it.enabled && it.key.isNotBlank() }.forEach { h ->
            sb.append("        .addHeader(\"${h.key}\", \"${h.value.replace("\"", "\\\"")}\")\n")
        }

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            sb.append("        .addHeader(\"Authorization\", okhttp3.Credentials.basic(\"${req.authUser}\", \"$pass\"))\n")
        }

        sb.append("        .build()\n\n")
        sb.append("    client.newCall(request).execute().use { response ->\n")
        sb.append("        println(\"Status: \${response.code}\")\n")
        sb.append("        println(response.body?.string())\n")
        sb.append("    }\n")
        sb.append("}\n")
        return sb.toString()
    }

    private fun generateKotlinKtor(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("import io.ktor.client.*\n")
        sb.append("import io.ktor.client.engine.cio.*\n")
        sb.append("import io.ktor.client.request.*\n")
        sb.append("import io.ktor.client.statement.*\n")
        sb.append("import io.ktor.http.*\n\n")
        sb.append("suspend fun main() {\n")
        sb.append("    val client = HttpClient(CIO)\n\n")
        sb.append("    val response: HttpResponse = client.request(\"${req.url}\") {\n")
        sb.append("        method = HttpMethod.parse(\"${req.method.uppercase()}\")\n")

        req.headers.filter { it.enabled && it.key.isNotBlank() }.forEach { h ->
            sb.append("        headers.append(\"${h.key}\", \"${h.value.replace("\"", "\\\"")}\")\n")
        }

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            val basic = java.util.Base64.getEncoder().encodeToString("${req.authUser}:$pass".toByteArray())
            sb.append("        headers.append(HttpHeaders.Authorization, \"Basic $basic\")\n")
        }

        if (!req.body.isNullOrEmpty()) {
            val escapedBody = req.body.replace("\"", "\\\"").replace("\$", "\\$")
            sb.append("        setBody(\"\"\"$escapedBody\"\"\")\n")
        }

        sb.append("    }\n\n")
        sb.append("    println(response.status)\n")
        sb.append("    println(response.bodyAsText())\n")
        sb.append("    client.close()\n")
        sb.append("}\n")
        return sb.toString()
    }

    private fun generatePhpCurl(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("<?php\n\n")
        sb.append("\$curl = curl_init();\n\n")
        sb.append("curl_setopt_array(\$curl, array(\n")
        sb.append("  CURLOPT_URL => '${req.url}',\n")
        sb.append("  CURLOPT_RETURNTRANSFER => true,\n")
        sb.append("  CURLOPT_ENCODING => '',\n")
        sb.append("  CURLOPT_MAXREDIRS => 10,\n")
        sb.append("  CURLOPT_TIMEOUT => ${req.timeoutSeconds ?: 30},\n")
        sb.append("  CURLOPT_FOLLOWLOCATION => ${if (req.followRedirects) "true" else "false"},\n")
        sb.append("  CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,\n")
        sb.append("  CURLOPT_CUSTOMREQUEST => '${req.method.uppercase()}',\n")

        if (!req.body.isNullOrEmpty()) {
            sb.append("  CURLOPT_POSTFIELDS => '${req.body.replace("'", "\\'")}',\n")
        }

        val enabledHeaders = req.headers.filter { it.enabled && it.key.isNotBlank() }
        if (enabledHeaders.isNotEmpty()) {
            sb.append("  CURLOPT_HTTPHEADER => array(\n")
            enabledHeaders.forEach { h ->
                sb.append("    '${h.key}: ${h.value.replace("'", "\\'")}',\n")
            }
            sb.append("  ),\n")
        }

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            sb.append("  CURLOPT_USERPWD => '${req.authUser}:$pass',\n")
        }

        sb.append("));\n\n")
        sb.append("\$response = curl_exec(\$curl);\n")
        sb.append("\$httpCode = curl_getinfo(\$curl, CURLINFO_HTTP_CODE);\n")
        sb.append("curl_close(\$curl);\n\n")
        sb.append("echo \"HTTP Status: \" . \$httpCode . \"\\n\";\n")
        sb.append("echo \$response;\n")
        return sb.toString()
    }

    private fun generateJavaHttpClient(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("import java.net.URI;\n")
        sb.append("import java.net.http.HttpClient;\n")
        sb.append("import java.net.http.HttpRequest;\n")
        sb.append("import java.net.http.HttpResponse;\n")
        if (req.timeoutSeconds != null) {
            sb.append("import java.time.Duration;\n")
        }
        sb.append("\npublic class Main {\n")
        sb.append("    public static void main(String[] args) throws Exception {\n")
        sb.append("        HttpRequest.Builder builder = HttpRequest.newBuilder()\n")
        sb.append("            .uri(URI.create(\"${req.url}\"))\n")

        if (req.timeoutSeconds != null) {
            sb.append("            .timeout(Duration.ofSeconds(${req.timeoutSeconds}))\n")
        }

        req.headers.filter { it.enabled && it.key.isNotBlank() }.forEach { h ->
            sb.append("            .header(\"${h.key}\", \"${h.value.replace("\"", "\\\"")}\")\n")
        }

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            val basic = java.util.Base64.getEncoder().encodeToString("${req.authUser}:$pass".toByteArray())
            sb.append("            .header(\"Authorization\", \"Basic $basic\")\n")
        }

        val method = req.method.uppercase()
        if (!req.body.isNullOrEmpty()) {
            val escapedBody = req.body.replace("\"", "\\\"").replace("\n", "\\n")
            sb.append("            .method(\"$method\", HttpRequest.BodyPublishers.ofString(\"$escapedBody\"));\n\n")
        } else if (method == "GET") {
            sb.append("            .GET();\n\n")
        } else {
            sb.append("            .method(\"$method\", HttpRequest.BodyPublishers.noBody());\n\n")
        }

        sb.append("        HttpRequest request = builder.build();\n")
        sb.append("        HttpClient client = HttpClient.newHttpClient();\n")
        sb.append("        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());\n\n")
        sb.append("        System.out.println(\"Status Code: \" + response.statusCode());\n")
        sb.append("        System.out.println(response.body());\n")
        sb.append("    }\n")
        sb.append("}\n")
        return sb.toString()
    }

    private fun generateRustReqwest(req: ParsedCurlRequest): String {
        val sb = StringBuilder()
        sb.append("use reqwest::Client;\n")
        sb.append("use std::error::Error;\n\n")
        sb.append("#[tokio::main]\n")
        sb.append("async fn main() -> Result<(), Box<dyn Error>> {\n")
        sb.append("    let client = Client::new();\n")
        sb.append("    let res = client.${req.method.lowercase()}(\"${req.url}\")\n")

        req.headers.filter { it.enabled && it.key.isNotBlank() }.forEach { h ->
            sb.append("        .header(\"${h.key}\", \"${h.value.replace("\"", "\\\"")}\")\n")
        }

        if (!req.authUser.isNullOrEmpty()) {
            val pass = req.authPass ?: ""
            sb.append("        .basic_auth(\"${req.authUser}\", Some(\"$pass\"))\n")
        }

        if (!req.body.isNullOrEmpty()) {
            val escapedBody = req.body.replace("\"", "\\\"").replace("\n", "\\n")
            sb.append("        .body(\"$escapedBody\")\n")
        }

        sb.append("        .send()\n")
        sb.append("        .await?;\n\n")
        sb.append("    println!(\"Status: {}\", res.status());\n")
        sb.append("    let body = res.text().await?;\n")
        sb.append("    println!(\"{}\", body);\n")
        sb.append("    Ok(())\n")
        sb.append("}\n")
        return sb.toString()
    }

    /**
     * 実務プリセットリスト
     */
    fun getPresets(): List<CurlPreset> {
        return listOf(
            CurlPreset(
                id = "openai-chat",
                nameJa = "OpenAI Chat Completions",
                nameEn = "OpenAI Chat Completions",
                descriptionJa = "GPT-4o / GPT-4o-mini へメッセージを送信する標準APIリクエスト",
                descriptionEn = "Standard API request to send prompts to GPT-4o models",
                category = "AI / LLM",
                method = "POST",
                curlCommand = """curl https://api.openai.com/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_OPENAI_API_KEY" \
  -d '{
    "model": "gpt-4o-mini",
    "messages": [
      {"role": "system", "content": "You are a helpful assistant."},
      {"role": "user", "content": "Hello!"}
    ],
    "temperature": 0.7
  }'"""
            ),
            CurlPreset(
                id = "github-user",
                nameJa = "GitHub ユーザー情報取得",
                nameEn = "GitHub Get Authenticated User",
                descriptionJa = "GitHub REST APIで認証済みユーザープロファイルを取得",
                descriptionEn = "Fetch authenticated user profile using GitHub REST API v3",
                category = "Developer Tools",
                method = "GET",
                curlCommand = """curl -X GET https://api.github.com/user \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer YOUR_GITHUB_TOKEN" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "User-Agent: CurlForge-App""""
            ),
            CurlPreset(
                id = "stripe-payment-intent",
                nameJa = "Stripe 決済インテント作成",
                nameEn = "Stripe Create PaymentIntent",
                descriptionJa = "クレジットカード決済トランザクション初期化リクエスト",
                descriptionEn = "Create a PaymentIntent with Stripe API",
                category = "Payment",
                method = "POST",
                curlCommand = """curl https://api.stripe.com/v1/payment_intents \
  -u "sk_test_YOUR_STRIPE_KEY:" \
  -d "amount=2000" \
  -d "currency=jpy" \
  -d "payment_method_types[]=card""""
            ),
            CurlPreset(
                id = "slack-webhook",
                nameJa = "Slack Incoming Webhook",
                nameEn = "Slack Incoming Webhook",
                descriptionJa = "Slackチャンネルへの通知メッセージ送信",
                descriptionEn = "Post formatted notifications directly to Slack channels",
                category = "Messaging",
                method = "POST",
                curlCommand = """curl -X POST https://hooks.slack.com/services/YOUR/WEBHOOK/URL \
  -H "Content-type: application/json" \
  -d '{"text": "🚨 Build #42 succeeded on production environment!"}'"""
            ),
            CurlPreset(
                id = "weather-open-meteo",
                nameJa = "Open-Meteo 天気予報 (認証不要)",
                nameEn = "Open-Meteo Weather Forecast (No Auth)",
                descriptionJa = "東京(緯度35.68, 経度139.69)の現在天気をリアルタイム取得",
                descriptionEn = "Fetch current weather data for Tokyo without API keys",
                category = "Public APIs",
                method = "GET",
                curlCommand = """curl -X GET "https://api.open-meteo.com/v1/forecast?latitude=35.6895&longitude=139.6917&current_weather=true""""
            ),
            CurlPreset(
                id = "graphql-countries",
                nameJa = "GraphQL 国情報クエリ",
                nameEn = "GraphQL Countries Query",
                descriptionJa = "GraphQLエンドポイントへのクエリ送信 (JSON payload)",
                descriptionEn = "Send a GraphQL query payload to retrieve countries and capitals",
                category = "GraphQL",
                method = "POST",
                curlCommand = """curl https://countries.trevorblades.com/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { countries { code name capital currency } }"}'"""
            ),
            CurlPreset(
                id = "oauth-token",
                nameJa = "OAuth 2.0 Client Credentials",
                nameEn = "OAuth 2.0 Client Credentials Token",
                descriptionJa = "認可サーバーからアクセストークンを発行してもらうリクエスト",
                descriptionEn = "Request access token using client credentials flow",
                category = "Auth",
                method = "POST",
                curlCommand = """curl -X POST https://auth.example.com/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET""""
            ),
            CurlPreset(
                id = "jsonplaceholder-post",
                nameJa = "JSONPlaceholder 新規投稿",
                nameEn = "JSONPlaceholder Create Post",
                descriptionJa = "モックRESTサーバーへのJSONデータ作成リクエスト",
                descriptionEn = "Sample POST request to mock JSON placeholder API",
                category = "Testing",
                method = "POST",
                curlCommand = """curl -X POST https://jsonplaceholder.typicode.com/posts \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d '{"title": "CurlForge Release", "body": "Super fast cURL conversion tool", "userId": 1}'"""
            )
        )
    }

    /**
     * cURL オプション辞典
     */
    fun getCurlOptionDocs(): List<CurlOptionDoc> {
        return listOf(
            CurlOptionDoc(
                flag = "-X",
                longFlag = "--request",
                category = "HTTP Method",
                summaryJa = "HTTPリクエストメソッドを指定",
                summaryEn = "Specify custom HTTP request method",
                detailJa = "GET, POST, PUT, DELETE, PATCH, HEAD 等のHTTP動詞を明示的に指定します。-dを指定した場合は自動的にPOSTになります。",
                detailEn = "Specifies custom HTTP method such as GET, POST, PUT, DELETE, PATCH. If -d is used, POST is default.",
                example = "curl -X POST https://api.example.com"
            ),
            CurlOptionDoc(
                flag = "-H",
                longFlag = "--header",
                category = "Headers",
                summaryJa = "カスタムHTTPヘッダーを追加",
                summaryEn = "Pass custom header to server",
                detailJa = "\"Header-Name: Value\" の形式で指定します。複数指定する場合は -H を繰り返します。",
                detailEn = "Extra header to include in request. Specify multiple headers by passing -H multiple times.",
                example = "curl -H \"Authorization: Bearer token123\" -H \"Accept: application/json\" https://example.com"
            ),
            CurlOptionDoc(
                flag = "-d",
                longFlag = "--data",
                category = "Request Body",
                summaryJa = "POSTデータを送信 (application/x-www-form-urlencoded または JSON)",
                summaryEn = "HTTP POST data",
                detailJa = "HTTP POSTリクエストでボディデータを送信します。改行は削除されます。JSONを送信する場合は -H \"Content-Type: application/json\" と併用します。",
                detailEn = "Sends specified data in POST request. Omits newlines. Pair with JSON Content-Type header for JSON APIs.",
                example = "curl -d '{\"key\":\"value\"}' -H \"Content-Type: application/json\" https://example.com"
            ),
            CurlOptionDoc(
                flag = "--data-raw",
                longFlag = "",
                category = "Request Body",
                summaryJa = "@プレフィックスをファイル展開せずにそのまま送信",
                summaryEn = "Send data raw without @ file expansion",
                detailJa = "-d では @filename でファイル参照できますが、--data-raw は @ で始まる文字列もそのままデータとして送信します。",
                detailEn = "Like -d, but does not interpret leading @ as file reference.",
                example = "curl --data-raw '@twitter_handle' https://example.com"
            ),
            CurlOptionDoc(
                flag = "-u",
                longFlag = "--user",
                category = "Auth",
                summaryJa = "HTTP Basic認証 ユーザー名:パスワード",
                summaryEn = "Server user and password",
                detailJa = "サーバーに送信するBasic認証情報を「user:password」の形式で指定します。自動的にAuthorization: Basicヘッダーに変換されます。",
                detailEn = "Specify user and password for server authentication, generating Basic Auth header.",
                example = "curl -u admin:secret123 https://api.example.com"
            ),
            CurlOptionDoc(
                flag = "-L",
                longFlag = "--location",
                category = "Navigation",
                summaryJa = "HTTPリダイレクト (301, 302) に追従",
                summaryEn = "Follow HTTP redirects",
                detailJa = "サーバーから 3xx ステータスコードが返された場合、Locationヘッダーの遷移先へ自動的にリクエストを再送信します。",
                detailEn = "If server reports requested page moved, automatically resend request to new location.",
                example = "curl -L https://shorturl.com/xyz"
            ),
            CurlOptionDoc(
                flag = "-i",
                longFlag = "--include",
                category = "Output",
                summaryJa = "レスポンスヘッダーも出力に含める",
                summaryEn = "Include protocol response headers in output",
                detailJa = "レスポンス本文の前に、ステータスラインおよびHTTPレスポンスヘッダーを表示します。デバッグに非常に便利です。",
                detailEn = "Includes HTTP response headers in the output before response body.",
                example = "curl -i https://httpbin.org/get"
            ),
            CurlOptionDoc(
                flag = "-I",
                longFlag = "--head",
                category = "Output",
                summaryJa = "HTTPヘッダーのみを取得 (HEADリクエスト)",
                summaryEn = "Show document info only (HEAD request)",
                detailJa = "ボディを取得せずHEADリクエストを送信し、ヘッダーのみを表示します。ファイルの存在確認やサイズ確認に便利です。",
                detailEn = "Fetches headers only using HEAD method. Useful for checking file existence or size.",
                example = "curl -I https://google.com"
            ),
            CurlOptionDoc(
                flag = "-s",
                longFlag = "--silent",
                category = "Output",
                summaryJa = "プログレスメーターやエラー出力を非表示",
                summaryEn = "Silent mode (suppress progress meter)",
                detailJa = "進捗バーやエラーメッセージを非表示にします。パイプライン処理やスクリプト内での実行に適しています。",
                detailEn = "Don't show progress meter or error messages. Makes curl mute.",
                example = "curl -s https://api.ipify.org"
            ),
            CurlOptionDoc(
                flag = "-S",
                longFlag = "--show-error",
                category = "Output",
                summaryJa = "-s と併用し、エラー時のみエラーメッセージを表示",
                summaryEn = "Show error even when -s is used",
                detailJa = "-s でサイレントにした際、失敗した時だけ標準エラー出力にエラーメッセージを出力させます。",
                detailEn = "When used with -s, it makes curl show an error message if it fails.",
                example = "curl -sS https://example.com"
            ),
            CurlOptionDoc(
                flag = "-v",
                longFlag = "--verbose",
                category = "Debug",
                summaryJa = "通信の詳細ログ (ハンドシェイク、送受信ヘッダー) を表示",
                summaryEn = "Make operation more talkative",
                detailJa = "DNS解決、TLSハンドシェイク、送信した全ヘッダー (> で表示)、受信した全ヘッダー (< で表示) を逐一確認できます。",
                detailEn = "Outputs full debugging info: TLS handshake, sent/received headers, and connection diagnostics.",
                example = "curl -v https://api.github.com"
            ),
            CurlOptionDoc(
                flag = "-o",
                longFlag = "--output",
                category = "Output",
                summaryJa = "レスポンスをファイルに保存",
                summaryEn = "Write to file instead of stdout",
                detailJa = "標準出力ではなく指定したパスのファイルへレスポンスボディを書き込みます。",
                detailEn = "Write output to filename instead of stdout.",
                example = "curl -o download.zip https://example.com/archive.zip"
            ),
            CurlOptionDoc(
                flag = "-k",
                longFlag = "--insecure",
                category = "SSL / Security",
                summaryJa = "SSL/TLS 証明書の検証をスキップ",
                summaryEn = "Allow insecure server connections",
                detailJa = "オレオレ証明書や開発用自己署名証明書、有効期限切れ証明書のエラーを無視して接続します（本番環境では注意）。",
                detailEn = "Proceed and operate even for server connections otherwise considered insecure.",
                example = "curl -k https://localhost:8443"
            ),
            CurlOptionDoc(
                flag = "-A",
                longFlag = "--user-agent",
                category = "Headers",
                summaryJa = "User-Agent ヘッダーを指定",
                summaryEn = "Send User-Agent to server",
                detailJa = "ブラウザや特定クライアントに偽装・指定するための User-Agent 文字列を設定します。",
                detailEn = "Specify the User-Agent string to send to the HTTP server.",
                example = "curl -A \"Mozilla/5.0\" https://example.com"
            ),
            CurlOptionDoc(
                flag = "-m",
                longFlag = "--max-time",
                category = "Network",
                summaryJa = "処理全体の最大許容時間 (秒)",
                summaryEn = "Maximum time allowed for the transfer",
                detailJa = "接続から受信完了までのトータル制限時間を秒数で指定します。タイムアウトすると終了コード28で中断します。",
                detailEn = "Maximum time in seconds that you allow the whole operation to take.",
                example = "curl -m 10 https://slow-api.example.com"
            ),
            CurlOptionDoc(
                flag = "--connect-timeout",
                longFlag = "",
                category = "Network",
                summaryJa = "接続確立までの最大許容時間 (秒)",
                summaryEn = "Maximum time allowed for connection",
                detailJa = "サーバーとのTCP/TLS接続が完了するまでのタイムアウト秒数を指定します。",
                detailEn = "Maximum time in seconds that you allow curl's connection to take.",
                example = "curl --connect-timeout 5 https://example.com"
            ),
            CurlOptionDoc(
                flag = "-F",
                longFlag = "--form",
                category = "Request Body",
                summaryJa = "multipart/form-data によるファイルアップロード",
                summaryEn = "Specify multipart MIME data",
                detailJa = "ブラウザのファイルアップロードフォームと同じ multipart/form-data リクエストを送信します。ファイルは @filename で指定します。",
                detailEn = "Emulates form submission with file attachments using multipart/form-data.",
                example = "curl -F \"file=@photo.png\" -F \"desc=Avatar\" https://upload.example.com"
            ),
            CurlOptionDoc(
                flag = "-b",
                longFlag = "--cookie",
                category = "Cookies",
                summaryJa = "Cookie文字列またはファイルを送信",
                summaryEn = "Send cookies from string/file",
                detailJa = "サーバーへ送信する Cookie ヘッダーの値を \"name=val\" 形式またはファイル名で指定します。",
                detailEn = "Pass the data to the HTTP server in the Cookie header.",
                example = "curl -b \"session_id=abc123xyz\" https://example.com"
            ),
            CurlOptionDoc(
                flag = "-c",
                longFlag = "--cookie-jar",
                category = "Cookies",
                summaryJa = "受信したCookieをファイルへ保存",
                summaryEn = "Write cookies to file after operation",
                detailJa = "レスポンスで受け取った Set-Cookie の内容をファイルに保存し、次回リクエストで再利用できるようにします。",
                detailEn = "Specify to which file you want curl to write all cookies after completing operation.",
                example = "curl -c cookies.txt https://example.com/login"
            ),
            CurlOptionDoc(
                flag = "--compressed",
                longFlag = "",
                category = "Optimization",
                summaryJa = "gzip/deflate/brotli 圧縮レスポンスを要求＆自動解凍",
                summaryEn = "Request compressed response",
                detailJa = "Accept-Encoding: gzip, deflate, br を自動付与し、受信した圧縮データを自動解凍して表示します。帯域を大幅に節約できます。",
                detailEn = "Request a compressed response using one of the algorithms curl supports, and decompress transparently.",
                example = "curl --compressed https://example.com"
            )
        )
    }
}
