# Mycode 引き継ぎ仕様書 — v4.0 / versionCode 4 時点

新しいチャットで開発を継続するときは、このファイルを含むソースzipを添付し
「Mycodeの更新を継続します。HANDOFF.mdのルールを厳守してください」と伝えること。

---

## 0. 重要な前提(名前のねじれ)

このプロジェクトは HelloApp として作り始めたため、**表示名だけが Mycode** で、
フォルダ名・パッケージ名は helloapp のまま。**変更しないこと**(変更すると別アプリ扱いになりデータが消える)。

| 項目 | 値 |
|---|---|
| アプリ表示名 | Mycode(`res/values/strings.xml`) |
| Termuxのフォルダ | `~/HelloApp` |
| GitHubリポジトリ | `Sekiguchi-Takashi/HelloApp` |
| applicationId / namespace | `com.sekiguchi.helloapp` |
| Kotlinのパッケージ | `com.sekiguchi.helloapp` |

---

## 1. 開発環境と規約(必ず守る)

- スマホのTermuxのみで開発。**ローカルでコンパイルしない**。GitHub Actionsが全ビルドを行う
- **gradle-wrapperなし**(`gradlew`を置かない)。Actions内で `gradle-version: '8.9'` を明示指定
- AGP **8.5.2** / Kotlin **2.0.20** / Gradle **8.9** / compileSdk・targetSdk **34** / minSdk **26** / Java **17**
- **外部ライブラリ依存ゼロ**(`app/build.gradle.kts` に dependencies ブロックを持たない)
- **XMLレイアウトなし**。UIは全てKotlinのプログラマティック生成。res配下はアイコンと文字列のみ
- **画像アセットゼロ**。ランチャーアイコンはベクター(`ic_launcher_foreground.xml`)+背景色のアダプティブアイコン
- **`app/debug.keystore` をリポジトリに同梱**して署名を固定する
  → アンインストール不要で上書きインストールでき、データも保持される
  → **更新zipにこのファイルを含めないこと**(上書きすると署名が変わりインストール失敗する)
- ビルドコマンド: `gradle assembleDebug` / Actionsのartifact名: `apk`

## 2. 更新の出し方

1. `versionCode` を +1、`versionName` も上げる
2. **変更したファイルだけ**を、リポジトリルートからの相対パス(`app/src/main/...`)でzip化して渡す
3. Termux側の手順:
```bash
cp ~/storage/downloads/Mycode-vN.zip ~/
unzip -o ~/Mycode-vN.zip -d ~/HelloApp
cd ~/HelloApp
git add .
git commit -m "vN 内容"
git push
```
4. Actionsが緑になったら Artifacts の `apk` をダウンロード → 解凍 → `app-debug.apk` を上書きインストール

---

## 3. ファイル構成

```
.github/workflows/build.yml           Actions定義(Java17 + Gradle8.9固定)
settings.gradle.kts                   google()/mavenCentral()、include(":app")
build.gradle.kts                      AGP 8.5.2 / Kotlin 2.0.20 を apply false で宣言
app/build.gradle.kts                  署名固定(signingConfigs "shared")、version定義
app/debug.keystore                    固定署名鍵(storePass/keyPass=android, alias=androiddebugkey)
app/src/main/AndroidManifest.xml      5つのActivity登録。テーマ=Theme.Material.Light.NoActionBar
app/src/main/java/com/sekiguchi/helloapp/
  ├ MainActivity.kt    画面1 メニュー+ステータス
  ├ EntryActivity.kt   画面2 新規登録
  ├ ListActivity.kt    画面3 一覧
  ├ MemoryActivity.kt  画面4 メモリー登録(写真)
  ├ HabitActivity.kt   画面5 習慣
  ├ Store.kt           メモ/メモリーの永続化
  └ HabitStore.kt      習慣の永続化
app/src/main/res/
  ├ values/strings.xml                 app_name = Mycode
  ├ values/ic_launcher_background.xml  #FFD54F(黄色)
  ├ drawable/ic_launcher_foreground.xml スマイル顔(ポップ路線)
  └ mipmap-anydpi-v26/ic_launcher(_round).xml
```

---

## 4. データ永続化

すべて `SharedPreferences("mycode")` にJSON文字列で保存。DBは使わない。

**キー `entries`(Store.kt)** — 画面2と画面4の登録を1つの配列で共有

| フィールド | 内容 |
|---|---|
| id | `System.currentTimeMillis()`。写真ファイル名にも使う |
| date | `yyyy-MM-dd`。並び替えは文字列比較で行う |
| memo | 本文 |
| deleteDate | `yyyy-MM-dd`。**メモリーは空文字**(自動削除の対象外) |
| type | `"normal"`(画面2) / `"memory"`(画面4) |
| photo | 写真の絶対パス。なければ空文字 |

- `Store.load()` は読み込みのたびに `deleteDate < 今日` の通常メモを消して保存し直す(= 削除日の翌日に消える挙動)
- `Store.removeIds()` は写真ファイル(`filesDir/mem_<id>.jpg`)も併せて削除する

**キー `habits`(HabitStore.kt)** — `id` / `day`(0=土曜〜6=金曜) / `text`

---

## 5. 画面仕様

### 画面1 MainActivity(メニュー)
- 背景 `#FFF8E1`、見出し「Mycode」ピンク `#F06292`
- ボタン4つ: 新規(`#F06292`)→画面2 / 一覧へ(`#4FC3F7`)→画面3 / 習慣(`#66BB6A`)→画面5 / メモリー(`#AB47BC`)→画面4
- その下にステータス表示(通常メモのみ。**メモリーは表示しない**)
- **ステータスの見出し文字をタップすると並び替えが切り替わる**(専用の並び替えボタンは廃止済み)
  - デフォルト: 削除日が近い順(`sortedBy { deleteDate }`)
  - 1回タップ: 日付が新しい順(`sortedByDescending { date }`)
  - もう1回タップ: デフォルトに戻る
- `onResume()` で毎回 refresh するので、他画面からの戻りで内容が最新化される

### 画面2 EntryActivity(新規登録)
- 日付(初期値=今日、タップで`DatePickerDialog`)、メモ(複数行)、削除日(タップで選択)
- 登録: 未入力チェック(メモ必須/削除日必須/削除日は今日以降)→ 保存 → 画面3へ遷移して `finish()`
- クリア: 入力を初期化した上で **`finish()` して画面1へ戻る**

### 画面3 ListActivity(一覧)
- 通常メモとメモリーを両方表示。**日付が新しい順**固定
- 通常メモは `📅 日付 メモ / 削除日:`、メモリーは `📷 日付 メモ` と表示
- 写真があれば右に200x200のサムネイル。**タップで`Dialog`による拡大表示**、再タップで閉じる
- 各行にチェックボックス。「チェックした項目を削除」でまとめて削除(メモリーの削除手段はここだけ)
- 「戻る」で `finish()`
- 画像は `inSampleSize` で縮小してから読む(OOM対策。この方式を維持すること)

### 画面4 MemoryActivity(メモリー登録)
- 背景 `#F3E5F5`、見出し紫 `#AB47BC`
- 日付・メモ・「写真をアップロード」ボタン
- 写真取得は `Intent.ACTION_GET_CONTENT` + `createChooser`(**権限宣言不要**。この方式を維持すること)
- 選択後はプレビュー表示。登録時に `filesDir/mem_<id>.jpg` へコピーしてパスを保存
- 登録: `type="memory"` / `deleteDate=""` で保存 → 画面3へ
- クリア: 入力を消して画面1へ戻る

### 画面5 HabitActivity(習慣)
- 背景 `#E8F5E9`、見出し緑 `#43A047`
- **土曜日→日曜日→月〜金曜日**の順に縦に見出しを並べ、その下に該当する習慣を表示
- 見出し色: 土=`#1E88E5` / 日=`#E53935` / 平日=`#43A047`。習慣がない曜日は「(なし)」
- 「追加」→ `AlertDialog` のポップアップで曜日を`Spinner`選択+内容入力 → 登録
- 「チェックした項目を削除」で選択削除、「戻る」で `finish()`

---

## 6. 過去にハマった落とし穴(再発防止)

- **`git init` はプロジェクトフォルダの中で行う**。ホームで実行すると `.bash_history` のトークンごとコミットされ、GitHubのPush Protection(GH013)で弾かれる
- コマンド中の `<TOKEN>` の `< >` はプレースホルダ。**そのまま貼るとbashのリダイレクトと解釈されてエラーになる**(実際に発生)
- unzipは必ず `-o`(上書き)。`-d ~/HelloApp` を忘れない
- `nothing to commit` が出たら、zipの解凍ができていない。`git status` で確認する
- トークンを再発行したら `git remote set-url origin https://<新トークン>@github.com/Sekiguchi-Takashi/HelloApp.git`
- 署名鍵を固定式に切り替えた v2 のときだけ、旧アプリのアンインストールが必要だった。以降は不要

---

## 7. 未実装のアイデア(希望があれば)

- 習慣に週次のチェック記録(やった/やってない)を付ける
- メモリー専用の一覧画面を画面3から分離する
- Releasesに直接APKを添付して、ブラウザから直リンクでダウンロードできるようにする
