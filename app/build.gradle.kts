import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.devnazrul.namaz"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val envPath = System.getenv("KEYSTORE_PATH")
      val keystorePath = if (!envPath.isNullOrEmpty()) envPath else "${rootDir}/debug.keystore"
      
      val ksFile = file(keystorePath)
      if (!ksFile.exists() && keystorePath.endsWith("debug.keystore")) {
        val base64File = file("${rootDir}/debug.keystore.base64")
        if (base64File.exists()) {
          try {
            val cleanBase64 = base64File.readText().replace("\\s".toRegex(), "")
            var decoded: ByteArray? = null
            try {
              decoded = Base64.getDecoder().decode(cleanBase64)
            } catch (e: Exception) {
              try {
                decoded = Base64.getMimeDecoder().decode(cleanBase64)
              } catch (e2: Exception) {
                println("Failed to decode with standard and MIME decoder: ${e2.message}")
              }
            }
            if (decoded != null) {
              ksFile.writeBytes(decoded)
              println("Successfully decoded and wrote debug.keystore of size: ${ksFile.length()} bytes")
            } else {
              println("Decoded byte array is null!")
            }
          } catch (e: Exception) {
            println("Exception during decoding: ${e.message}")
            e.printStackTrace()
          }
        } else {
          println("base64 file does not exist at: ${base64File.absolutePath}")
        }
      }

      storeFile = ksFile
      
      val envStorePass = System.getenv("STORE_PASSWORD")
      storePassword = if (!envStorePass.isNullOrEmpty()) envStorePass else "android"
      
      val envKeyAlias = System.getenv("KEY_ALIAS")
      keyAlias = if (!envKeyAlias.isNullOrEmpty()) envKeyAlias else "androiddebugkey"
      
      val envKeyPass = System.getenv("KEY_PASSWORD")
      keyPassword = if (!envKeyPass.isNullOrEmpty()) envKeyPass else "android"
    }
    create("debugConfig") {
      val ksFile = file("${rootDir}/debug.keystore")
      if (!ksFile.exists()) {
        val base64File = file("${rootDir}/debug.keystore.base64")
        if (base64File.exists()) {
          try {
            val cleanBase64 = base64File.readText().replace("\\s".toRegex(), "")
            var decoded: ByteArray? = null
            try {
              decoded = Base64.getDecoder().decode(cleanBase64)
            } catch (e: Exception) {
              try {
                decoded = Base64.getMimeDecoder().decode(cleanBase64)
              } catch (e2: Exception) {
                println("Failed to decode with standard and MIME decoder: ${e2.message}")
              }
            }
            if (decoded != null) {
              ksFile.writeBytes(decoded)
              println("Successfully decoded debug.keystore from debugConfig block")
            }
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      }
      storeFile = ksFile
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      
      val envPath = System.getenv("KEYSTORE_PATH")
      val keystorePath = if (!envPath.isNullOrEmpty()) envPath else "${rootDir}/debug.keystore"
      val ksFile = file(keystorePath)
      
      if (ksFile.exists()) {
        signingConfig = signingConfigs.getByName("release")
      } else {
        // Fallback to standard debug config to prevent validateSigningRelease fail when keystore is missing
        signingConfig = signingConfigs.getByName("debug")
      }
    }
    debug {
      val ksFile = file("${rootDir}/debug.keystore")
      if (ksFile.exists()) {
        signingConfig = signingConfigs.getByName("debugConfig")
      } else {
        // Fallback to standard debug config
        signingConfig = signingConfigs.getByName("debug")
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.database)
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.retrofit)
  implementation(libs.startapp.sdk)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
