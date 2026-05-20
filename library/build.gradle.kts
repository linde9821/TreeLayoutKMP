@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.linde9821"
version = "0.2.1"

kotlin {
    explicitApi()
    jvmToolchain(17)
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
                }
            }
        }
    }
    android {
        namespace = "org.github.linde9821.treelayout"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
    }
    linuxX64()
    mingwX64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    val useChrome = System.getenv("CI") == "true"
    js {
        browser {
            testTask {
                useKarma {
                    if (useChrome) useChromeHeadless() else useSafari()
                }
            }
        }
        nodejs()
    }
    wasmJs {
        browser {
            testTask {
                useKarma {
                    if (useChrome) useChromeHeadless() else useSafari()
                }
            }
        }
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    signAllPublications()

    coordinates("io.github.linde9821", "treelayout-kmp", "$version")

    pom {
        name = "TreeLayoutKMP"
        description =
            "A Kotlin Multiplatform library for computing tidy tree layouts using the Walker/Buchheim algorithm in O(n) time."
        inceptionYear = "2026"
        url = "https://github.com/linde9821/TreeLayoutKMP"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "linde9821"
                name = "Moritz Lindner"
                url = "https://github.com/linde9821"
            }
        }
        scm {
            url = "https://github.com/linde9821/TreeLayoutKMP"
            connection = "scm:git:git://github.com/linde9821/TreeLayoutKMP.git"
            developerConnection = "scm:git:ssh://git@github.com/linde9821/TreeLayoutKMP.git"
        }
    }
}
