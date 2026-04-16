plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}

android {
    namespace = "com.detection"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        kapt {
            arguments {
                arg("AROUTER_MODULE_NAME", "module_detection")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    configurations.all {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
}


dependencies {
    implementation(project(":foundation:common"))
    implementation(project(":foundation:network"))
    implementation(project(":foundation:storage"))
    implementation(project(":foundation:webview"))
    implementation(project(":foundation:uikit"))

    implementation(project(":business:main:api"))
    implementation(project(":business:detection:api"))
    implementation(project(":business:community:api"))
    implementation(project(":business:user:api"))


    //markdon
    implementation(libs.markwon.core)
    implementation(libs.markwon.highlight)
    implementation(libs.markwon.html)


    // CameraX
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // BlurView
    implementation(libs.blurview)

    implementation(libs.tflite.core)
    implementation(libs.tflite.support)

    // 可选：GPU 加速（如果设备支持）
    implementation(libs.tflite.gpu)

    // 可选：Select TF Ops（某些模型需要）
    implementation(libs.tflite.select.tf.ops)


    kapt(libs.arouter.compiler)
}

