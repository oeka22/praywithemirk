plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.praywithemirk.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.praywithemirk.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Fester Signaturschlüssel: jede neue Version lässt sich über die alte installieren
    // (egal ob in Android Studio oder auf GitHub gebaut). Nur für den privaten Gebrauch –
    // für eine Veröffentlichung im Play Store einen eigenen, geheimen Schlüssel verwenden.
    signingConfigs {
        create("praywithemirk") {
            storeFile = file("praywithemirk.keystore")
            storePassword = "emirk2026"
            keyAlias = "praywithemirk"
            keyPassword = "emirk2026"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("praywithemirk")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("praywithemirk")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Bewusst keine Zusatzbibliotheken: nur Android-Framework + Kotlin-Standardbibliothek.
}
