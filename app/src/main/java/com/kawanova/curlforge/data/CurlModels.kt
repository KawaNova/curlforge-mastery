package com.kawanova.curlforge.data

import kotlinx.serialization.Serializable

enum class HttpMethod(val colorHex: Long) {
    GET(0xFF10B981),    // Emerald
    POST(0xFF0284C7),   // Sky Blue
    PUT(0xFFF59E0B),    // Amber
    DELETE(0xFFEF4444), // Red
    PATCH(0xFFA855F7),  // Purple
    HEAD(0xFF64748B),   // Slate
    OPTIONS(0xFF6366F1) // Indigo
}

enum class TargetLanguage(val displayName: String, val syntaxHighlight: String, val extension: String) {
    PYTHON_REQUESTS("Python (requests)", "python", "py"),
    PYTHON_HTTPX("Python (httpx async)", "python", "py"),
    JS_FETCH("JavaScript (Fetch API)", "javascript", "js"),
    JS_AXIOS("Node.js / TS (Axios)", "typescript", "ts"),
    GO_NET_HTTP("Go (net/http)", "go", "go"),
    KOTLIN_OKHTTP("Kotlin (OkHttp)", "kotlin", "kt"),
    KOTLIN_KTOR("Kotlin (Ktor Client)", "kotlin", "kt"),
    PHP_CURL("PHP (curl_exec)", "php", "php"),
    JAVA_HTTP_CLIENT("Java 11+ (HttpClient)", "java", "java"),
    RUST_REQWEST("Rust (reqwest)", "rust", "rs")
}

@Serializable
data class HeaderItem(
    val key: String,
    val value: String,
    val enabled: Boolean = true
)

@Serializable
data class ParsedCurlRequest(
    val method: String = "GET",
    val url: String = "",
    val headers: List<HeaderItem> = emptyList(),
    val body: String? = null,
    val isJsonBody: Boolean = false,
    val authUser: String? = null,
    val authPass: String? = null,
    val followRedirects: Boolean = false,
    val timeoutSeconds: Int? = null,
    val rawCommand: String = ""
)

@Serializable
data class CurlPreset(
    val id: String,
    val nameJa: String,
    val nameEn: String,
    val descriptionJa: String,
    val descriptionEn: String,
    val category: String,
    val method: String,
    val curlCommand: String
)

@Serializable
data class SavedSnippet(
    val id: String,
    val title: String,
    val note: String = "",
    val method: String,
    val curlCommand: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class CurlOptionDoc(
    val flag: String,
    val longFlag: String = "",
    val category: String,
    val summaryJa: String,
    val summaryEn: String,
    val detailJa: String,
    val detailEn: String,
    val example: String
)
