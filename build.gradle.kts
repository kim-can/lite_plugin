plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.intellij") version "1.9.0"
}

group = "com.boss.android.lite.plugin"
version = "1.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    //https://www.jetbrains.com/intellij-repository/releases/
    version.set("211.7628.21")
    // Use IntelliJ IDEA CE because it's the basis of the IntelliJ Platform:
    type.set("IC")

    // Require the Android plugin (Gradle will choose the correct version):
    plugins.set(listOf("android"))
}

tasks {
    runIde {
        // Absolute path to installed target 3.5 Android Studio to use as
        // IDE Development Instance (the "Contents" directory is macOS specific):
        ideDir.set(file("/Applications/Android Studio.app/Contents"))
    }
}
