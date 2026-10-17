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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
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
import com.kawanova.curlforge.data.SavedSnippet
import com.kawanova.curlforge.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavedCurlScreen(
    viewModel: CurlViewModel,
    onSnippetLoaded: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                    text = "💾 Saved Snippets (${uiState.savedSnippets.size})",
                    color = Slate50,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your custom cURL requests saved securely on this device",
                    color = CurlSky,
                    fontSize = 11.5.sp
                )
            }

            if (uiState.savedSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No saved snippets yet",
                            color = Slate300,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You can save your frequently used cURL commands directly from the Generator screen using the bookmark icon.",
                            color = Slate400,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.savedSnippets, key = { it.id }) { snippet ->
                        SavedSnippetCard(
                            snippet = snippet,
                            dateStr = dateFormat.format(Date(snippet.createdAt)),
                            onLoad = {
                                viewModel.loadSnippet(snippet)
                                onSnippetLoaded()
                            },
                            onCopy = {
                                val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clip.setPrimaryClip(ClipData.newPlainText("Saved cURL", snippet.curlCommand))
                                viewModel.setSnackBar("Copied '${snippet.title}'!")
                            },
                            onDelete = {
                                viewModel.deleteSnippet(snippet.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedSnippetCard(
    snippet: SavedSnippet,
    dateStr: String,
    onLoad: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
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
                val methodColor = when (snippet.method.uppercase()) {
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
                        snippet.method.uppercase(),
                        color = methodColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = snippet.title,
                    color = Slate100,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CurlRose,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (snippet.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = snippet.note,
                color = Slate300,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // cURL Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TerminalBg)
                .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Text(
                text = snippet.curlCommand,
                color = CodeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateStr,
                color = Slate500,
                fontSize = 10.5.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onCopy,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 11.sp)
                }

                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = CurlCyan, contentColor = Slate950),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
