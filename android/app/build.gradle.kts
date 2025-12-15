plugins {
    alias(libs.plugins.android.application)
    // firebase
    // il tutorial di firebase direbbe di aggiungere questa riga ma l'ho tolta perché mi dava un errore
    // id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ids"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ids"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // firebase
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
}