# CurlForge (036_curl-converter) アプリ仕様書

## 1. アプリケーション基本情報
- **アプリ名**: 
  - 日本語: cURLコードジェネレータ - APIリクエスト変換 ＆ コード即時生成
  - 英語: CurlForge - cURL to Python, JS, Go, Kotlin & PHP
- **パッケージ名**: `com.kawanova.curlforge`
- **バージョン**: Version 1.0.0 (VersionCode 1)
- **対応OS**: Android 7.0 (API 24) 以上 / Android 16 (API 36) Ready
- **リポジトリ**: `https://github.com/KawaNova/curlforge-mastery`

## 2. 目的・コンセプト
- **課題**: DevTools や Swagger からコピーした cURL コマンドを、Python requests や JavaScript Fetch、Go などのアプリケーションコードに手作業で移植するのは時間がかかり、シンタックスエラーやエスケープミスの原因になる。また、Webの変換サービスに社内APIトークンを貼り付けるのはセキュリティ上危険。
- **解決策**: 完全端末内完結（オフライン・100%プライベート）で動作する高速 cURL ⇄ 多言語コード変換エンジンを提供。さらにGUIビルダーでの逆生成、実務APIテンプレート、cURLフラグ辞典を統合。

## 3. 機能要件

### 3.1 cURL 解析エンジン (CurlEngine)
- **トークン分割 (`tokenize`)**:
  - bash/zsh 形式のバックスラッシュ改行（`\`）を正規化
  - シングルクオート（`'`）、ダブルクオート（`"`）内のスペースを安全に保持
  - エスケープ文字（`\"`, `\'`）のアンエスケープ処理
- **パース機能 (`parseCurl`)**:
  - メソッド解析: `-X`, `--request`（デフォルト: GET、`-d` 付与時は POST）
  - ヘッダー解析: `-H`, `--header`（`Key: Value` の分割と保持）
  - ボディ解析: `-d`, `--data`, `--data-raw`, `--data-binary`
  - 認証情報: `-u`, `--user`（`user:pass` を抽出）
  - リダイレクト: `-L`, `--location`
  - タイムアウト: `-m`, `--max-time`, `--connect-timeout`
  - URL抽出: 引数以外のURLトークンを抽出
  - JSON自動判定: ボディが `{}` または `[]` の場合に判定

### 3.2 多言語コード生成 (TargetLanguage)
1. **Python (requests)**: `import requests`, `headers={}`, `json=` or `data=`, `response = requests.post(...)`
2. **Python (httpx async)**: `import httpx`, `async with httpx.AsyncClient() as client:`, `await client.post(...)`
3. **JavaScript (Fetch API)**: `fetch(url, { method: 'POST', headers: {...}, body: ... })`
4. **Node.js / TS (Axios)**: `import axios from 'axios'`, `await axios(config)`
5. **Go (net/http)**: `http.NewRequest(...)`, `req.Header.Add(...)`, `client.Do(req)`
6. **Kotlin (OkHttp)**: `OkHttpClient()`, `Request.Builder()`, `execute()`
7. **Kotlin (Ktor Client)**: `HttpClient(CIO)`, `client.request(url)`
8. **PHP (cURL)**: `curl_init()`, `curl_setopt_array()`, `curl_exec()`
9. **Java 11+ (HttpClient)**: `HttpRequest.newBuilder()`, `HttpClient.newHttpClient()`
10. **Rust (reqwest)**: `reqwest::Client::new()`, `client.post(...).send().await?`

### 3.3 リバース GUI リクエストビルダー
- メソッド選択（GET, POST, PUT, DELETE, PATCH, HEAD）
- URL入力
- ヘッダーのキー/値の追加・編集・削除
- リクエストボディ編集（JSON / Form / Raw）
- 変更内容を cURL コマンドおよび生成コードへ即時反映

### 3.4 実務 API プリセット
- OpenAI Chat Completions (GPT-4o)
- GitHub REST API (ユーザー取得)
- Stripe API (PaymentIntent)
- Slack Incoming Webhook
- GraphQL Query (Countries)
- OAuth 2.0 (Client Credentials)
- Open-Meteo Weather API
- JSONPlaceholder

### 3.5 cURL オプション完全辞典
- 30種以上の主要フラグの解説、使用例、リアルタイム検索機能

### 3.6 スニペット保存機能
- DataStore Preferences による安全な端末内保存
- タイトル・メモ付き管理、エディタへのワンタップ復元

## 4. セキュリティ ＆ プライバシー
- 外部APIへの変換データ送信なし（完全オンデバイス）
- インターネット権限は広告表示（AdMob）のみに使用
