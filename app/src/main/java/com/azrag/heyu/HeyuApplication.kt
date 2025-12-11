package com.azrag.heyu  // Paket adının bu olduğundan emin ol

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HeyuApplication : Application() {
    // Bu dosyanın içi genellikle boştur.
    // Görevi sadece @HiltAndroidApp anotasyonunu taşımaktır.
}
