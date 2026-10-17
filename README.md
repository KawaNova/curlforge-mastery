# 036_curl-converter (CurlForge)

> **cURLコードジェネレータ - APIリクエスト変換 ＆ 各種言語コード即時生成**  
> *CurlForge - cURL to Python, JS, Go, Kotlin, PHP, Java & Rust Code Generator*

[![Android](https://img.shields.io/badge/Platform-Android_16_Ready-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Privacy-100%25_On--Device-success.svg)](#privacy--security)
[![License](https://img.shields.io/badge/License-MIT-amber.svg)](LICENSE)

---

## 🌟 概要 (Overview)
**CurlForge** は、Chrome DevTools や Postman、公式ドキュメントからコピーした `cURL` コマンドを貼り付けるだけで、**Python (requests / httpx)**、**JavaScript (Fetch / Axios)**、**Go (net/http)**、**Kotlin (OkHttp / Ktor)**、**PHP (cURL)**、**Java 11+ (HttpClient)**、**Rust (reqwest)** 等の各言語・HTTPクライアントの実行可能コードへ瞬時に変換する開発者支援ツールです。

完全オンデバイスで動作するため、APIキーやBearerトークン、非公開URLが外部サーバーへ送信される心配は一切ありません。

---

## 🚀 主な機能 (Key Features)

1. **多言語コードジェネレータ (10 Target Languages)**
   - **Python**: `requests`, `httpx` (async)
   - **JavaScript / TypeScript**: `Fetch API`, `Axios`
   - **Go**: `net/http`
   - **Kotlin**: `OkHttp`, `Ktor Client`
   - **PHP**: `cURL (curl_exec)`
   - **Java 11+**: `HttpClient`
   - **Rust**: `reqwest`
2. **リバース GUI ビルダー (Visual Request Builder)**
   - HTTPメソッド、URL、カスタムヘッダー、JSONボディ、Basic認証、リダイレクト追跡、タイムアウトを直感的に設定
   - 変更内容をリアルタイムに cURL コマンドと各言語コードへ双方向同期
3. **実務 API プリセット (Battle-tested Presets)**
   - OpenAI (GPT-4o Chat Completions)
   - GitHub REST API (ユーザープロファイル取得)
   - Stripe API (PaymentIntent 決済作成)
   - Slack Incoming Webhook (通知メッセージ)
   - GraphQL API (Country Query)
   - OAuth 2.0 (Client Credentials Flow)
   - Open-Meteo Weather API (認証不要リアルタイム気象データ)
   - JSONPlaceholder (RESTモックテスト)
4. **cURL オプション＆フラグ完全辞典 (30+ Flags Glossary)**
   - `-X`, `-H`, `-d`, `--data-raw`, `-u`, `-L`, `-i`, `-I`, `-s`, `-S`, `-v`, `-o`, `-k`, `-A`, `-m`, `--connect-timeout`, `-F`, `-b`, `-c`, `--compressed` 等の日本語・英語解説と実務使用例
   - キーワードリアルタイム検索
5. **ローカルスニペット管理 (Saved Snippets)**
   - よく使うリクエストを端末内に保存し、ワンタップでエディタに復元

---

## 🔒 プライバシー ＆ セキュリティ (Privacy & Security)
- **100% ローカル処理**: cURL 文字列の構文解析およびコード生成はすべて端末内の CPU/メモリ上でのみ実行されます。
- **データ送信ゼロ**: APIキー、認証トークン、プライベートエンドポイント等の入力データは一切外部に送信されません。

---

## 🛠️ 技術スタック (Tech Stack)
- **言語**: Kotlin 2.0.21
- **UI フレームワーク**: Jetpack Compose (Material 3)
- **アーキテクチャ**: Clean Architecture + MVVM + StateFlow + Repository
- **データ永続化**: AndroidX Jetpack DataStore Preferences + Kotlinx Serialization
- **ターゲット SDK**: minSdk 24, compileSdk 36, targetSdk 36 (Android 16 Ready)
- **収益化**: Google Mobile Ads (AdMob) + 姉妹アプリクロスプロモーション

---

## 📦 ビルド手順 (Build Instructions)

```bash
# ユニットテスト実行
./gradlew testDebugUnitTest

# リリース AAB (Android App Bundle) ビルド
./gradlew bundleRelease

# リリース APK ビルド
./gradlew assembleRelease
```

---

## 📄 ライセンス (License)
MIT License. Copyright (c) 2026 KawaNova.
