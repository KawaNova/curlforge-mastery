package com.kawanova.curlforge.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawanova.curlforge.data.CurlPreset
import com.kawanova.curlforge.ui.theme.*

@Composable
fun CurlPresetsScreen(
    viewModel: CurlViewModel,
    onPresetLoaded: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(uiState.presets) {
        listOf("All") + uiState.presets.map { it.category }.distinct()
    }

    val filteredPresets = remember(uiState.presets, selectedCategory) {
        if (selectedCategory == "All") {
            uiState.presets
        } else {
            uiState.presets.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        containerColor = Slate950
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ヘッダー
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "🚀 API Presets & Templates",
                    color = Slate50,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Battle-tested cURL requests for OpenAI, GitHub, Stripe & more",
                    color = CurlSky,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // カテゴリフィルター
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) CurlAmber else Slate800)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Slate950 else Slate300,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // プリセットカードリスト
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPresets) { preset ->
                    PresetCard(
                        preset = preset,
                        onLoad = {
                            viewModel.loadPreset(preset)
                            onPresetLoaded()
                        },
                        onCopy = {
                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clip.setPrimaryClip(ClipData.newPlainText("Curl Preset", preset.curlCommand))
                            viewModel.setSnackBar("Copied '${preset.nameEn}' cURL!")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PresetCard(
    preset: CurlPreset,
    onLoad: () -> Unit,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val methodColor = when (preset.method.uppercase()) {
                    "GET" -> CurlEmerald
                    "POST" -> CurlBlue
                    "PUT" -> CurlAmber
                    "DELETE" -> CurlRose
                    else -> Slate400
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(methodColor.copy(alpha = 0.2f))
                        .border(1.dp, methodColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        preset.method.uppercase(),
                        color = methodColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = preset.nameEn,
                    color = Slate100,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Slate800)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = preset.category,
                    color = Slate400,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${preset.nameJa} — ${preset.descriptionJa}",
            color = Slate300,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // cURL コードプレビュー
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TerminalBg)
                .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = preset.curlCommand,
                color = CodeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                maxLines = 4
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCopy,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy cURL", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onLoad,
                colors = ButtonDefaults.buttonColors(containerColor = CurlCyan, contentColor = Slate950),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Load into Generator", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
