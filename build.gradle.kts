// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    alias(libs.plugins.android.application) apply false
//}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
// 顶层 build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
        // 阿里云镜像（可选，加速）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        // 百度地图
        maven { url = uri("https://maven.baidu.com/nexus/content/groups/public/") }
//        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")

    }
}

