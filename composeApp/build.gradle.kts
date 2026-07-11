import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
}

kotlin {
    // Compile with a JDK 17 toolchain (auto-provisioned by Gradle if none is installed),
    // so builds don't depend on the ambient JDK — a JRE-only PATH would otherwise fail
    // Android's compileDebugJavaWithJavac.
    jvmToolchain(17)

    @Suppress("DEPRECATION")
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "17" }
        }
    }

    @Suppress("DEPRECATION")
    jvm("desktop") {
        compilations.all {
            kotlinOptions { jvmTarget = "17" }
        }
    }

    sourceSets {
        val desktopMain by getting

        // Shared JVM code between Android and desktop (jsoup is JVM-only).
        val jvmShared by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.jsoup)
            }
        }
        androidMain.get().dependsOn(jvmShared)
        desktopMain.dependsOn(jvmShared)

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.rssparser)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.sqldelight.sqlite.driver)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.core)
        }

        val desktopTest by getting
        desktopTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.kotlinx.coroutines.core)
        }

        // Android unit tests run on the local JVM (testDebugUnitTest) and reuse commonTest,
        // so the shared data/RSS/extractor coverage matches the Linux desktop run.
        val androidUnitTest by getting
        androidUnitTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

// Single source of truth for the app version. In CI it's derived from the git tag
// (-PappVersion=X.Y.Z from a v* tag); defaults to 1.0.0 for local/main builds.
val appVersion = (findProperty("appVersion") as String?)?.takeIf { it.isNotBlank() } ?: "1.0.0"
val appVersionCode = (findProperty("versionCode") as String?)?.toIntOrNull() ?: 1

android {
    namespace = "org.dev.minoreader"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.dev.minoreader"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersion
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

sqldelight {
    databases {
        create("MinoDb") {
            packageName.set("org.dev.minoreader.db")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.dev.minoreader.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "minoreader"
            packageVersion = appVersion
            // JDK modules the minimized (jlink) runtime must include.
            // java.sql: SQLite (JDBC) driver; the rest come from suggestRuntimeModules.
            modules("java.instrument", "java.net.http", "java.sql", "jdk.unsupported")
        }
    }
}
