plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sekiguchi.helloapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sekiguchi.helloapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// 外部ライブラリ依存ゼロ(標準SDKのみ)。ビルドが速く、失敗要因が最小になります。
