plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.azrag.heyu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.azrag.heyu"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // ----- TEMEL ANDROID VE JETPACK -----
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.activity:activity-compose:1.9.0") // activity-ktx'i zaten içerir

    // ----- JETPACK COMPOSE UI -----
    implementation(platform(libs.androidx.compose.bom)) // Compose Bill of Materials
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended-android:1.6.8") // Tekrarlanan silindi
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha13")

    // ----- FIREBASE -----
    // Firebase BOM, tüm Firebase kütüphaneleri için uyumlu sürümleri otomatik olarak yönetir.
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    // Not: Artık -ktx uzantılı kütüphaneler ana kütüphanelerle birleşti, ayrıca eklemeye gerek yok.

    // ----- HILT (DEPENDENCY INJECTION) -----
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ----- YARDIMCI KÜTÜPHANELER -----
    implementation("io.coil-kt:coil-compose:2.6.0") // Resim yükleme için
    // DİKKAT: Accompanist-FlowLayout artık güncel değil. Yerine Compose'un kendi FlowRow/FlowColumn'u geldi.
    // Şimdilik sorun çıkarmaz, ancak ileride kaldırmayı düşünebilirsin.
    implementation("com.google.accompanist:accompanist-flowlayout:0.17.0")

    // ----- TEST KÜTÜPHANELERİ -----
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview) // tooling.preview daha doğru
    debugImplementation(libs.androidx.ui.test.manifest)
}

