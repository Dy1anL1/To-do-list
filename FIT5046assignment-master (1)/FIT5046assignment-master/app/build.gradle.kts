plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)

//    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fit5046assignment" // Make sure this matches your actual namespace
    compileSdk = 35 // Updated from 34 to 35

    defaultConfig {
        applicationId = "com.example.fit5046assignment"
        minSdk = 24 // Or your desired minimum SDK
        targetSdk = 35 // Updated from 34 to 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // Ensure this version is compatible with your Kotlin plugin version
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM and core components
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.material.icons.core)
    implementation(libs.material.icons.extended)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Compose Preview & Tooling
    debugImplementation(libs.ui.tooling)
    implementation(libs.ui.tooling.preview)

    // Kotlin Standard Library
    implementation(libs.kotlin.stdlib.jdk8)

    // Navigation for Compose
    implementation(libs.androidx.navigation.compose)

    // Room components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Vico Charts
    implementation(libs.vico.core)
    implementation(libs.vico.compose.m3)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform("com.google.firebase:firebase-bom:30.2.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth:22.3.1")

    // Google Sign-In dependency
    implementation("com.google.android.gms:play-services-auth:20.7.0")

// Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Google Sign-In (compatible with Kotlin 1.9.0)
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    // Calendar API client
    implementation("com.google.apis:google-api-services-calendar:v3-rev411-1.25.0")
    // OAuth2 support
    implementation("com.google.auth:google-auth-library-credentials:0.20.0")
}

// force version
configurations.all {
    resolutionStrategy {
        force("com.google.firebase:firebase-auth:22.3.1")
        force("com.google.firebase:firebase-auth-ktx:22.3.1")
    }
}
