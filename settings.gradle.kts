// Dosya: settings.gradle.kts

pluginManagement {
    repositories {
        google()
        // <<< EKLENECEK EN ÖNEMLİ SATIR >>>
        // Eklentilerin bulunduğu ana depo burasıdır.
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HeyU"
include(":app")
