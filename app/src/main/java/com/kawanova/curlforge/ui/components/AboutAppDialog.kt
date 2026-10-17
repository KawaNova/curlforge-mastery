package com.kawanova.curlforge.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawanova.curlforge.ui.theme.*
import java.net.URLEncoder

@Composable
fun AboutAppDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    val privacyPolicyUrl = "https://kawanova.github.io/privacy-policy/android-curl-converter.html"
    val appNameForForm = "CurlForge (cURLコードジェネレータ)"
    val envInfo = "Android ${Build.VERSION.RELEASE} / ${Build.MANUFACTURER} ${Build.MODEL} (App v1.0.0)"
    
    val feedbackBaseUrl = "https://docs.google.com/forms/d/e/1FAIpQLSeAKUuGOlBBI4bR7DwF0Ji6xPk20A19-asM1VWQG1pcAG4BLg/viewform"
    val encodedApp = URLEncoder.encode(appNameForForm, "UTF-8")
    val encodedEnv = URLEncoder.encode(envInfo, "UTF-8")
    val feedbackUrl = "$feedbackBaseUrl?usp=pp_url&entry.447629162=$encodedApp&entry.670312804=$encodedEnv"

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Slate900,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CurlCyan.copy(alpha = 0.2f))
                        .border(1.dp, CurlCyan, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CurlForge",
                        color = Slate50,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.0.0 (Android 16 Ready)",
                        color = CurlSky,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "cURLコマンドからPython、JS、Go、Kotlin等の実行可能コードを完全ローカルで生成する開発者特化型ツールです。",
                    color = Slate300,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                // プライバシー保護バッジ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% オンデバイス処理 / APIキーや通信内容は外部サーバーへ一切送信されません",
                        color = Slate300,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // アクションボタン群
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CurlCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📄 プライバシーポリシーを表示", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(feedbackUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CurlCyan, contentColor = Slate950)
                ) {
                    Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("💬 ご意見・不具合報告 (Googleフォーム)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "© 2026 KawaNova. All rights reserved.",
                        color = Slate500,
                        fontSize = 10.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("閉じる", color = CurlSky, fontWeight = FontWeight.Bold)
            }
        }
    )
}
