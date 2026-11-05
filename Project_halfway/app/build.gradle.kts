import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dokka)
    id("com.google.gms.google-services")
}

// The Google Maps API key lives in local.properties (git-ignored), never in the repo:
//     MAPS_API_KEY=AIza...
// It is put into AndroidManifest.xml through the ${MAPS_API_KEY} placeholder. Without a
// key the app still builds and runs, but maps show an empty grey grid.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val mapsApiKey: String =
    localProperties.getProperty("MAPS_API_KEY") ?: System.getenv("MAPS_API_KEY") ?: ""

if (mapsApiKey.isBlank()) {
    logger.warn("MAPS_API_KEY is missing from local.properties. Maps will show a blank grid.")
}

android {
    namespace = "com.example.project_halfway"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.project_halfway"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("com.google.firebase:firebase-auth")
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("com.google.firebase:firebase-firestore")
    // Google Maps in Compose: map previews and the location picker
    implementation(libs.maps.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.media3:media3-common:1.11.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
// Dokka: builds HTML documentation from the KDoc comments in the code.
// Run  ./gradlew :app:dokkaGenerate  and open app/build/dokka/html/index.html
dokka {
    moduleName.set("Project_halfway")
    dokkaSourceSets.configureEach {
        // Warn about public classes and functions that have no KDoc comment yet
        reportUndocumented.set(true)
    }
}
