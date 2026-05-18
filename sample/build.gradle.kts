plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project(":library"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.linde9821.treelayout.sample.MainKt"
    }
}
