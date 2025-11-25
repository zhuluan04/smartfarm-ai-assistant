pluginManagement {
    repositories {
        // 官方源优先
        google()
        mavenCentral()
        gradlePluginPortal()

        // 第三方
        maven { url = uri("https://jitpack.io")}

        // 阿里云镜像（加速用，可选）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 官方源优先
        google()
        mavenCentral()

        // 阿里云镜像（加速用，可选）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
    }
}

//rootProject.name = "MyApplication"
rootProject.name = "recognize"
include(":app")
