import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.ahmedmani.pairipfix"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.ahmedmani.pairipfix"
        minSdk = 26
        targetSdk = 34
        // CI passes these (-PversionCode / -PversionName); local builds get a dev version.
        versionCode = (findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = (findProperty("versionName") as String?) ?: "dev"
    }

    // ReleaseKey.properties is written by CI from repository secrets. Without it, release builds
    // fall back to the debug key so local and fork builds still work -- but such an APK has a
    // different certificate and cannot update a properly signed install.
    val keystorePropertiesFile = rootProject.file("ReleaseKey.properties")
    var releaseSigning = signingConfigs.getByName("debug")
    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
        releaseSigning = signingConfigs.create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    } else {
        logger.warn("ReleaseKey.properties not found; release build will fall back to debug signing.")
    }

    buildTypes {
        release {
            // Must stay off: LSPosed loads mainHook by the name in assets/xposed_init.
            isMinifyEnabled = false
            signingConfig = releaseSigning
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Provided at runtime by LSPosed; must never be bundled into the APK.
    compileOnly("de.robv.android.xposed:api:82")
}
