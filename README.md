# HelloApp — スマホ完結 Android APK ビルド検証用

GitHub Actionsでビルドする最小Androidプロジェクト。gradle-wrapper不使用、外部ライブラリ依存ゼロ。

## 使い方
1. GitHubで新規リポジトリを作成
2. この一式をpush(下記コマンド参照)
3. Actionsタブ → 完了したrun → Artifacts「apk」をダウンロード
4. zipを解凍してapp-debug.apkをインストール

## push手順(Termux)
```
pkg install git
cd HelloApp
git init
git add .
git commit -m "initial commit"
git branch -M main
git remote add origin https://<TOKEN>@github.com/<ユーザー名>/<リポジトリ名>.git
git push -u origin main
```

## カスタマイズ箇所
- アプリ名: app/src/main/res/values/strings.xml
- アイコン: app/src/main/res/drawable/ic_launcher_foreground.xml(ベクター)と values/ic_launcher_background.xml(背景色)
- パッケージ名: app/build.gradle.kts の applicationId / namespace(変更時はKotlinファイルのpackage宣言とディレクトリも合わせる)
