plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    // id("me.ele.lancet") // Temporarily disabled due to AGP 8.0 compatibility issues
}

android {
    namespace = "com.agroai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.agroai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        kapt {
            arguments {
                arg("AROUTER_MODULE_NAME", project.name)
            }
        }
    }

    signingConfigs {
        // 适配高德定位SDK的签名配置
        create("release") {
            storeFile = file("keystore/my_app.jks")
            storePassword = "123456"
            keyAlias = "mt_app_key"
            keyPassword = "123456"
        }

        named("debug") {
            storeFile = file("keystore/my_app.jks")
            storePassword = "123456"
            keyAlias = "mt_app_key"
            keyPassword = "123456"
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
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":foundation:common"))
    implementation(project(":foundation:network"))
    
    // Feature Modules
    implementation(project(":business:main:impl"))
    implementation(project(":business:detection:impl"))
    implementation(project(":business:community:impl"))
    implementation(project(":business:user:impl"))
    implementation(project(":business:demo"))

    implementation(libs.blurview)


    // Base dependencies are transitively included via lib_common (api)
    // But app might need them directly sometimes. Since we use api in lib_common, app gets them.
    
    kapt(libs.arouter.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.location)
    // CodeLocator
    implementation(libs.codelocator.core)
    // CodeLocator Lancet has compatibility issues with AGP 8.0+ and Jetifier
    // Temporarily disabled until properly configured
    // debugImplementation(libs.codelocator.lancet.all)
    
    // LeakCanary - 只在 debug 模式下检测内存泄漏
    debugImplementation(libs.leakcanary.android)
}
