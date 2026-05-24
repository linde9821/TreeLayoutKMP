plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kover) apply false
}

allprojects {
    plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().yarnLockMismatchReport =
            org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport.NONE
        the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().yarnLockAutoReplace = true
    }
}
