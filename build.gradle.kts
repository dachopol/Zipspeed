// Top-level build file where you can add configuration options common to all sub-projects/modules.
val existingHandler = Thread.getDefaultUncaughtExceptionHandler()
Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
    val isKspAwtNpe = thread.name.contains("AWT-EventQueue") &&
        throwable is NullPointerException &&
        throwable.stackTrace.any { it.className.contains("BinaryFileTypeDecompilers") || it.className.contains("ksp.com.intellij") }
    if (!isKspAwtNpe) {
        existingHandler?.uncaughtException(thread, throwable) ?: throwable.printStackTrace()
    }
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}
