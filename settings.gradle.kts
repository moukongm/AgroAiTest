pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }

        // 官方仓库
        google()
        mavenCentral()

        // 第三方仓库
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://artifact.bytedance.com/repository/Volcengine/") }
    }
}

rootProject.name = "AgroAi"
include(":app")

// Foundation
include(":foundation:common")
include(":foundation:network")
include(":foundation:storage")
include(":foundation:webview")
include(":foundation:uikit")

// Business - Demo
include(":business:demo")

// Business - Main
include(":business:main:api")
include(":business:main:impl")

// Business - Detection
include(":business:detection:api")
include(":business:detection:impl")

// Business - Community
include(":business:community:api")
include(":business:community:impl")

// Business - User
include(":business:user:api")
include(":business:user:impl")
