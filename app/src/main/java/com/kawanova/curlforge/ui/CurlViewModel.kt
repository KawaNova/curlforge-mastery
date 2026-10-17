package com.kawanova.curlforge.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kawanova.curlforge.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class CurlUiState(
    val rawCurlInput: String = "curl -X POST https://api.openai.com/v1/chat/completions \\\n  -H \"Content-Type: application/json\" \\\n  -H \"Authorization: Bearer YOUR_OPENAI_API_KEY\" \\\n  -d '{\"model\": \"gpt-4o-mini\", \"messages\": [{\"role\": \"user\", \"content\": \"Hello CurlForge!\"}]}'",
    val parsedRequest: ParsedCurlRequest = ParsedCurlRequest(),
    val selectedLanguage: TargetLanguage = TargetLanguage.PYTHON_REQUESTS,
    val generatedCode: String = "",
    val isReverseBuilderOpen: Boolean = false,
    val builderMethod: String = "POST",
    val builderUrl: String = "https://api.openai.com/v1/chat/completions",
    val builderHeaders: List<HeaderItem> = listOf(
        HeaderItem("Content-Type", "application/json"),
        HeaderItem("Authorization", "Bearer YOUR_OPENAI_API_KEY")
    ),
    val builderBody: String = "{\"model\": \"gpt-4o-mini\", \"messages\": [{\"role\": \"user\", \"content\": \"Hello CurlForge!\"}]}",
    val builderAuthUser: String = "",
    val builderAuthPass: String = "",
    val builderFollowRedirects: Boolean = false,
    val builderTimeout: Int? = null,
    val savedSnippets: List<SavedSnippet> = emptyList(),
    val presets: List<CurlPreset> = emptyList(),
    val optionDocs: List<CurlOptionDoc> = emptyList(),
    val glossarySearchQuery: String = "",
    val snackBarMessage: String? = null
)

class CurlViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CurlRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(CurlUiState())
    val uiState: StateFlow<CurlUiState> = _uiState.asStateFlow()

    init {
        val initialPresets = CurlEngine.getPresets()
        val initialDocs = CurlEngine.getCurlOptionDocs()
        val initialParsed = CurlEngine.parseCurl(_uiState.value.rawCurlInput)
        val initialCode = CurlEngine.generateCode(initialParsed, _uiState.value.selectedLanguage)

        _uiState.update {
            it.copy(
                parsedRequest = initialParsed,
                generatedCode = initialCode,
                presets = initialPresets,
                optionDocs = initialDocs
            )
        }

        // 保存スニペット監視
        viewModelScope.launch {
            repository.savedSnippetsFlow.collect { snippets ->
                _uiState.update { it.copy(savedSnippets = snippets) }
            }
        }

        // 直前のcURL復元
        viewModelScope.launch {
            repository.lastCurlFlow.firstOrNull()?.let { lastCurl ->
                if (!lastCurl.isNullOrBlank()) {
                    updateCurlInput(lastCurl, saveToPrefs = false)
                }
            }
        }

        // 直前のターゲット言語復元
        viewModelScope.launch {
            repository.lastTargetLangFlow.firstOrNull()?.let { lastLangName ->
                val targetLang = TargetLanguage.values().firstOrNull { it.name == lastLangName }
                if (targetLang != null) {
                    selectLanguage(targetLang)
                }
            }
        }
    }

    fun updateCurlInput(newInput: String, saveToPrefs: Boolean = true) {
        val parsed = CurlEngine.parseCurl(newInput)
        val code = CurlEngine.generateCode(parsed, _uiState.value.selectedLanguage)
        _uiState.update {
            it.copy(
                rawCurlInput = newInput,
                parsedRequest = parsed,
                generatedCode = code,
                builderMethod = parsed.method,
                builderUrl = parsed.url,
                builderHeaders = if (parsed.headers.isNotEmpty()) parsed.headers else listOf(HeaderItem("", "")),
                builderBody = parsed.body ?: "",
                builderAuthUser = parsed.authUser ?: "",
                builderAuthPass = parsed.authPass ?: "",
                builderFollowRedirects = parsed.followRedirects,
                builderTimeout = parsed.timeoutSeconds
            )
        }
        if (saveToPrefs) {
            viewModelScope.launch {
                repository.setLastCurl(newInput)
            }
        }
    }

    fun selectLanguage(lang: TargetLanguage) {
        val code = CurlEngine.generateCode(_uiState.value.parsedRequest, lang)
        _uiState.update {
            it.copy(
                selectedLanguage = lang,
                generatedCode = code
            )
        }
        viewModelScope.launch {
            repository.setLastTargetLang(lang.name)
        }
    }

    fun toggleReverseBuilder() {
        _uiState.update { it.copy(isReverseBuilderOpen = !it.isReverseBuilderOpen) }
    }

    fun updateBuilderMethod(method: String) {
        _uiState.update { it.copy(builderMethod = method) }
        rebuildFromBuilder()
    }

    fun updateBuilderUrl(url: String) {
        _uiState.update { it.copy(builderUrl = url) }
        rebuildFromBuilder()
    }

    fun updateBuilderBody(body: String) {
        _uiState.update { it.copy(builderBody = body) }
        rebuildFromBuilder()
    }

    fun updateBuilderAuth(user: String, pass: String) {
        _uiState.update { it.copy(builderAuthUser = user, builderAuthPass = pass) }
        rebuildFromBuilder()
    }

    fun updateBuilderRedirect(follow: Boolean) {
        _uiState.update { it.copy(builderFollowRedirects = follow) }
        rebuildFromBuilder()
    }

    fun updateBuilderTimeout(seconds: Int?) {
        _uiState.update { it.copy(builderTimeout = seconds) }
        rebuildFromBuilder()
    }

    fun addHeader(key: String = "", value: String = "") {
        val current = _uiState.value.builderHeaders.toMutableList()
        current.add(HeaderItem(key, value))
        _uiState.update { it.copy(builderHeaders = current) }
        rebuildFromBuilder()
    }

    fun removeHeader(index: Int) {
        val current = _uiState.value.builderHeaders.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.update { it.copy(builderHeaders = current) }
            rebuildFromBuilder()
        }
    }

    fun updateHeader(index: Int, key: String, value: String, enabled: Boolean = true) {
        val current = _uiState.value.builderHeaders.toMutableList()
        if (index in current.indices) {
            current[index] = HeaderItem(key, value, enabled)
            _uiState.update { it.copy(builderHeaders = current) }
            rebuildFromBuilder()
        }
    }

    private fun rebuildFromBuilder() {
        val s = _uiState.value
        val req = ParsedCurlRequest(
            method = s.builderMethod,
            url = s.builderUrl,
            headers = s.builderHeaders.filter { it.key.isNotBlank() },
            body = s.builderBody.ifBlank { null },
            isJsonBody = s.builderBody.trim().let { (it.startsWith("{") && it.endsWith("}")) || (it.startsWith("[") && it.endsWith("]")) },
            authUser = s.builderAuthUser.ifBlank { null },
            authPass = s.builderAuthPass.ifBlank { null },
            followRedirects = s.builderFollowRedirects,
            timeoutSeconds = s.builderTimeout
        )
        val newCurl = CurlEngine.buildCurl(req)
        val code = CurlEngine.generateCode(req, s.selectedLanguage)
        _uiState.update {
            it.copy(
                rawCurlInput = newCurl,
                parsedRequest = req,
                generatedCode = code
            )
        }
        viewModelScope.launch {
            repository.setLastCurl(newCurl)
        }
    }

    fun loadPreset(preset: CurlPreset) {
        updateCurlInput(preset.curlCommand)
        _uiState.update { it.copy(snackBarMessage = "Preset '${preset.nameEn}' loaded!") }
    }

    fun saveCurrentSnippet(title: String, note: String = "") {
        val s = _uiState.value
        val finalTitle = title.ifBlank {
            s.parsedRequest.url.takeIf { it.isNotBlank() } ?: "cURL ${s.parsedRequest.method}"
        }
        val snippet = SavedSnippet(
            id = UUID.randomUUID().toString(),
            title = finalTitle,
            note = note,
            method = s.parsedRequest.method,
            curlCommand = s.rawCurlInput
        )
        viewModelScope.launch {
            repository.saveSnippet(snippet)
            _uiState.update { it.copy(snackBarMessage = "Saved to snippets!") }
        }
    }

    fun deleteSnippet(id: String) {
        viewModelScope.launch {
            repository.deleteSnippet(id)
            _uiState.update { it.copy(snackBarMessage = "Snippet removed") }
        }
    }

    fun loadSnippet(snippet: SavedSnippet) {
        updateCurlInput(snippet.curlCommand)
        _uiState.update { it.copy(snackBarMessage = "'${snippet.title}' restored!") }
    }

    fun setGlossarySearch(query: String) {
        _uiState.update { it.copy(glossarySearchQuery = query) }
    }

    fun clearSnackBar() {
        _uiState.update { it.copy(snackBarMessage = null) }
    }

    fun setSnackBar(msg: String) {
        _uiState.update { it.copy(snackBarMessage = msg) }
    }
}
