package com.queueshub.buildsrc

object Versions {
    const val ktLint = "0.45.2"
    const val kotlin = "1.7.10"
    const val hilt = "2.46.1"
    const val hilt_compose_navigation = "1.0.0"
    const val navigation = "2.5.3"
    const val compose = "1.2.1"
    const val constraintCompose = "1.0.1"
    const val coroutinesTest = "1.6.4"
    const val composeActivity = "1.6.0"
    const val composeViewModel = "2.5.1"
    const val composeCoil = "2.1.0"
    const val uiAutomator = "2.2.0"
    const val junit = "4.13.2"
    const val junitKtx = "1.1.3"
    const val mockito = "4.8.0"
    const val robolectric = "4.8"
    const val rules = "1.4.0"
    const val mockWebServer = "4.9.3"
    const val mockk = "1.13.2"
    const val gson = "2.9.0"
    const val retrofit = "2.9.0"
    const val okhttp = "4.9.3"
    const val room = "2.4.3"
    const val preferences = "1.1.1"
    const val timber = "5.0.1"
    const val arrow = "1.0.1"
    const val splash = "1.0.0"
    const val cameraX = "1.1.0"
    const val cameraxView = "1.2.0-rc01"
    const val textRecognition = "18.0.2"
    const val firebaseBom = "31.2.1"
    const val mlkit = "24.0.3"
    const val barcode = "17.0.3"
    const val lottieVersion = "6.0.0"
}


object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.2.2"

    object Kotlin {
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    }

    object Hilt {
        const val android = "com.google.dagger:hilt-android:${Versions.hilt}"
        const val androidCompiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt}"
        const val compiler = "androidx.hilt:hilt-compiler:1.0.0"
        const val androidTesting = "com.google.dagger:hilt-android-testing:${Versions.hilt}"
        const val gradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}"
        const val navigation =
            "androidx.hilt:hilt-navigation-compose:${Versions.hilt_compose_navigation}"
    }


    object Navigation {
        const val compose = "androidx.navigation:navigation-compose:${Versions.navigation}"
    }

    object Compose {
        const val ui = "androidx.compose.ui:ui:${Versions.compose}"
        const val material = "androidx.compose.material:material:${Versions.compose}"
        const val icons = "androidx.compose.material:material-icons-extended:${Versions.compose}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout-compose:${Versions.constraintCompose}"
        const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview:${Versions.compose}"
        const val uiTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
        const val uiTestManifest = "androidx.compose.ui:ui-test-manifest:${Versions.compose}"
        const val uiTestJUnit4 = "androidx.compose.ui:ui-test-junit4:${Versions.compose}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutinesTest}"

        const val activity = "androidx.activity:activity-compose:${Versions.composeActivity}"

        const val viewModel =
            "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.composeViewModel}"

        const val coil = "io.coil-kt:coil-compose:${Versions.composeCoil}"
        const val coilSvg = "io.coil-kt:coil-svg:${Versions.composeCoil}"
    }

    object Testing {

        const val uiAutomator = "androidx.test.uiautomator:uiautomator:${Versions.uiAutomator}"
        const val jUnit = "junit:junit:${Versions.junit}"
        const val rules = "androidx.test:rules:${Versions.rules}"
        const val junitKtx = "androidx.test.ext:junit-ktx:${Versions.junitKtx}"
        const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
        const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
        const val mockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.mockWebServer}"
    }

    object Network {
        const val gson = "com.google.code.gson:gson:${Versions.gson}"
        const val gsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
        const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    }

    object GoogleServices {
        const val textRecognition = "com.google.android.gms:play-services-mlkit-text-recognition:${Versions.textRecognition}"
        const val barcodeScanning = "com.google.mlkit:barcode-scanning:${Versions.barcode}"
    }
    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:${Versions.firebaseBom}"
        const val analytics = "com.google.firebase:firebase-analytics"
        const val mlkit = "com.google.firebase:firebase-ml-vision:${Versions.mlkit}"
        const val functions = "com.google.firebase:firebase-functions-ktx:20.2.1"
        const val auth = "com.google.firebase:firebase-auth-ktx"
    }

    object Lottie {
        const val compose = "com.airbnb.android:lottie-compose:${Versions.lottieVersion}"
    }

    object CameraX {
        const val camera2 = "androidx.camera:camera-camera2:${Versions.cameraX}"
        const val lifecycle = "androidx.camera:camera-lifecycle:${Versions.cameraX}"
        const val view = "androidx.camera:camera-view:${Versions.cameraX}"
    }

    object Cache{
        const val runtime = "androidx.room:room-runtime:${Versions.room}"
        const val preferences = "androidx.preference:preference:${Versions.preferences}"
        const val ktx = "androidx.room:room-ktx:${Versions.room}"
        const val compiler = "androidx.room:room-compiler:${Versions.room}"
    }
    object Others{
        const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
        const val arrow = "io.arrow-kt:arrow-core:${Versions.arrow}"
        const val splash = "androidx.core:core-splashscreen:${Versions.splash}"
    }
}