package com.kawanova.curlforge.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
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
import com.kawanova.curlforge.data.CurlOptionDoc
import com.kawanova.curlforge.ui.theme.*

@Composable
fun CurlGlossaryScreen(
    viewModel: CurlViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filteredOptions = remember(uiState.optionDocs, uiState.glossarySearchQuery) {
        val q = uiState.glossarySearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            uiState.optionDocs
        } else {
            uiState.optionDocs.filter { doc ->
                doc.flag.lowercase().contains(q) ||
                doc.longFlag.lowercase().contains(q) ||
                doc.category.lowercase().contains(q) ||
                doc.summaryJa.lowercase().contains(q) ||
                doc.summaryEn.lowercase().contains(q) ||
                doc.detailJa.lowercase().contains(q)
            }
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
            // ヘッダー & 検索
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "📖 cURL Flags & Options Glossary",
                    color = Slate50,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Complete handbook for cURL commands, flags & best practices",
                    color = CurlSky,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.glossarySearchQuery,
                    onValueChange = { viewModel.setGlossarySearch(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search flags (e.g. -H, --data, cookie, auth)...", color = Slate500, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = CurlCyan, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (uiState.glossarySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setGlossarySearch("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate200,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = CurlCyan,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // フラグ解説カードリスト
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOptions) { doc ->
                    OptionDocCard(
                        doc = doc,
                        onCopyExample = {
                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clip.setPrimaryClip(ClipData.newPlainText("cURL Example", doc.example))
                            viewModel.setSnackBar("Copied example for ${doc.flag}!")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OptionDocCard(
    doc: CurlOptionDoc,
    onCopyExample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CurlCyan.copy(alpha = 0.2f))
                        .border(1.dp, CurlCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = doc.flag,
                        color = CurlCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (doc.longFlag.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = doc.longFlag,
                        color = Slate300,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Slate800)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = doc.category,
                    color = Slate400,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = doc.summaryJa,
            color = Slate100,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = doc.detailJa,
            color = Slate400,
            fontSize = 11.5.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Example Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TerminalBg)
                .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = doc.example,
                color = CodeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onCopyExample,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy Example",
                    tint = CurlSky,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
