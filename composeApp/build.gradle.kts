import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.sqldelight) // Add SQLDelight plugin
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.google.gms.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlinCocoapods)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }


    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += "-Xbinary=bundleId=com.shubham.hard75kmm"
            linkerOpts.add("-lsqlite3")
        }
    }


    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
//            implementation(libs.sqldelight.driver.android) // SQLDelight Android driver
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.koin.composeVM)

            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.crashlytics)

            implementation(libs.play.services.auth) // For Google Sign-In
            implementation(libs.firebase.analytics)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)

            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
//            implementation(libs.sqldelight.driver.native) // SQLDelight iOS driver
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)

            // SQLDelight
//            implementation(libs.sqldelight.coroutines)

            // DateTime
            implementation(libs.kotlinx.datetime)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation(libs.bundles.coil)

            implementation(libs.firebase.auth.gitlive)
            implementation(libs.firebase.firestore.gitlive)
            implementation(libs.firebase.crashlytics.gitlive)

            implementation(libs.multiplatform.settings)
            implementation(libs.uuid)

            implementation(libs.bundles.ktor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    cocoapods {
        summary = "Hard75KMM preject"
        version = "1.0"
        ios.deploymentTarget = "18.0"
        homepage = "https://example.com"
        podfile = project.file("../iosApp/Podfile")
        pod("GoogleSignIn")
        // Add the framework block here
        framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }
}

android {
    namespace = "com.shubham.hard75kmm"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.shubham.hard75kmm"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

// SQLDelight Configuration
//sqldelight {
//    databases {
//        create("AppDatabase") {
//            packageName.set("com.shubham.hard75kmm.db")
//        }
//    }
//}