package com.kawanova.curlforge.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawanova.curlforge.data.HeaderItem
import com.kawanova.curlforge.data.HttpMethod
import com.kawanova.curlforge.data.TargetLanguage
import com.kawanova.curlforge.ui.components.AdaptiveBannerAd
import com.kawanova.curlforge.ui.components.SisterAppsBanner
import com.kawanova.curlforge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurlConverterScreen(
    viewModel: CurlViewModel,
    onNavigateToPresets: () -> Unit = {},
    onNavigateToGlossary: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveTitleInput by remember { mutableStateOf("") }
    var saveNoteInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackBarMessage) {
        uiState.snackBarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackBar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Slate950
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ヘッダーバー
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CurlCyan.copy(alpha = 0.15f))
                                    .border(1.dp, CurlCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚡", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CurlForge",
                                    color = Slate50,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "cURL to Multi-Language Generator",
                                    color = CurlSky,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = onNavigateToPresets,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate800)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Presets",
                                    tint = CurlAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onNavigateToGlossary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate800)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "Glossary",
                                    tint = CurlCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // cURL 入力カード (Terminal ターミナル風)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TerminalBg)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(CurlRose))
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(CurlAmber))
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(CurlEmerald))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "cURL Command Input",
                                color = Slate400,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // クリップボードから貼り付け
                            IconButton(
                                onClick = {
                                    val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                    val text = clip.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!text.isNullOrBlank()) {
                                        viewModel.updateCurlInput(text)
                                        viewModel.setSnackBar("Pasted from clipboard")
                                    }
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = CurlSky,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            // クリア
                            IconButton(
                                onClick = { viewModel.updateCurlInput("") },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.rawCurlInput,
                        onValueChange = { viewModel.updateCurlInput(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CodeText,
                            unfocusedTextColor = CodeText,
                            focusedContainerColor = TerminalCard,
                            unfocusedContainerColor = TerminalCard,
                            focusedBorderColor = CurlCyan,
                            unfocusedBorderColor = TerminalBorder,
                            cursorColor = CurlCyan
                        ),
                        placeholder = {
                            Text(
                                "curl -X POST https://api.example.com/data \\\n  -H \"Content-Type: application/json\" \\\n  -d '{\"hello\": \"world\"}'",
                                color = Slate500,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // パース結果のクイックバッジ表示 & GUIビルダー展開トグル
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val method = uiState.parsedRequest.method.uppercase()
                            val methodColor = when (method) {
                                "GET" -> CurlEmerald
                                "POST" -> CurlBlue
                                "PUT" -> CurlAmber
                                "DELETE" -> CurlRose
                                "PATCH" -> CurlPurple
                                else -> Slate400
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(methodColor.copy(alpha = 0.2f))
                                    .border(1.dp, methodColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(method, color = methodColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (uiState.parsedRequest.headers.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Slate800)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                    Text(
                                        "${uiState.parsedRequest.headers.size} Headers",
                                        color = Slate300,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            if (!uiState.parsedRequest.body.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (uiState.parsedRequest.isJsonBody) CurlCyan.copy(alpha = 0.2f) else Slate800)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (uiState.parsedRequest.isJsonBody) "JSON Body" else "Raw Body",
                                        color = if (uiState.parsedRequest.isJsonBody) CurlCyan else Slate300,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }

                        // GUIビルダー切り替えボタン
                        TextButton(
                            onClick = { viewModel.toggleReverseBuilder() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                if (uiState.isReverseBuilderOpen) Icons.Default.ExpandLess else Icons.Default.Tune,
                                contentDescription = "GUI Builder",
                                tint = CurlSky,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (uiState.isReverseBuilderOpen) "Hide Builder" else "GUI Builder",
                                color = CurlSky,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // リバース GUI ビルダー (展開時)
            if (uiState.isReverseBuilderOpen) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate900)
                            .border(1.dp, CurlCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "🛠️ Visual Request Builder (Reverse cURL)",
                            color = CurlCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Edit fields below to dynamically update the cURL command & target code.",
                            color = Slate400,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // メソッド選択
                        Text("HTTP Method", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD").forEach { m ->
                                val isSelected = uiState.builderMethod.equals(m, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CurlCyan else Slate800)
                                        .clickable { viewModel.updateBuilderMethod(m) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = m,
                                        color = if (isSelected) Slate950 else Slate300,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // URL
                        Text("Target URL", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = uiState.builderUrl,
                            onValueChange = { viewModel.updateBuilderUrl(it) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate200,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            placeholder = { Text("https://api.example.com/v1/resource", color = Slate500, fontSize = 12.sp) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // ヘッダー管理
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Headers (${uiState.builderHeaders.size})", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            TextButton(
                                onClick = { viewModel.addHeader() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Header", tint = CurlSky, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Add", color = CurlSky, fontSize = 11.sp)
                            }
                        }

                        uiState.builderHeaders.forEachIndexed { index, header ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = header.key,
                                    onValueChange = { viewModel.updateHeader(index, it, header.value, header.enabled) },
                                    modifier = Modifier.weight(0.45f),
                                    placeholder = { Text("Header", color = Slate500, fontSize = 11.sp) },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                )
                                OutlinedTextField(
                                    value = header.value,
                                    onValueChange = { viewModel.updateHeader(index, header.key, it, header.enabled) },
                                    modifier = Modifier.weight(0.45f),
                                    placeholder = { Text("Value", color = Slate500, fontSize = 11.sp) },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                )
                                IconButton(
                                    onClick = { viewModel.removeHeader(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = CurlRose, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // リクエストボディ
                        Text("Request Body (JSON / Form / Raw)", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = uiState.builderBody,
                            onValueChange = { viewModel.updateBuilderBody(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 70.dp, max = 150.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate200,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            placeholder = { Text("{\"key\": \"value\"}", color = Slate500, fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.5.sp, fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            }

            // ターゲット言語選択バー (横スクロールチップ)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Target Language & Library",
                        color = Slate300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TargetLanguage.values().forEach { lang ->
                            val isSelected = uiState.selectedLanguage == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) CurlCyan else Slate850)
                                    .border(
                                        1.dp,
                                        if (isSelected) CurlCyan else Slate700,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.selectLanguage(lang) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = lang.displayName,
                                    color = if (isSelected) Slate950 else Slate200,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 生成コード表示カード
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TerminalBg)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Generated Code",
                                color = Slate200,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate800)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    uiState.selectedLanguage.extension.uppercase(),
                                    color = CurlSky,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // 保存ボタン
                            IconButton(
                                onClick = {
                                    saveTitleInput = uiState.parsedRequest.url.takeIf { it.isNotBlank() } ?: "API Request"
                                    showSaveDialog = true
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Slate800)
                            ) {
                                Icon(
                                    Icons.Default.BookmarkBorder,
                                    contentDescription = "Save Snippet",
                                    tint = CurlAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // 共有ボタン
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Generated Code (${uiState.selectedLanguage.displayName})")
                                        putExtra(Intent.EXTRA_TEXT, uiState.generatedCode)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Code via"))
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Slate800)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Slate200,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // コピーボタン
                            Button(
                                onClick = {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("CurlForge Code", uiState.generatedCode))
                                    viewModel.setSnackBar("Copied ${uiState.selectedLanguage.displayName} code!")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CurlCyan,
                                    contentColor = Slate950
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // コード本文
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalCard)
                            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = uiState.generatedCode.ifBlank { "// Enter a valid cURL command above to generate code" },
                            style = CodeBlockStyle,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 広告バナー
            item {
                AdaptiveBannerAd(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }

            // 姉妹アプリバナー
            item {
                SisterAppsBanner(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    isJapanese = true
                )
            }
        }
    }

    // スニペット保存ダイアログ
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save to Snippets", color = Slate100, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Give a title to this cURL snippet for fast access anytime.", color = Slate300, fontSize = 12.sp)
                    OutlinedTextField(
                        value = saveTitleInput,
                        onValueChange = { saveTitleInput = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = saveNoteInput,
                        onValueChange = { saveNoteInput = it },
                        label = { Text("Memo / Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentSnippet(saveTitleInput, saveNoteInput)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CurlCyan, contentColor = Slate950)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}
