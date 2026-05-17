import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.linde9821"
version = "0.1.0-SNAPSHOT"

kotlin {
    explicitApi()
    jvm()
    android {
        namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compileTaskProvider.configure{
                compilerOptions {
                    jvmTarget.set(
                        JvmTarget.JVM_11
                    )
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

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
        description = "A Kotlin Multiplatform library for computing tidy tree layouts using the Walker/Buchheim algorithm in O(n) time."
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

tasks.register<JavaExec>("runSample") {
    description = "Runs the JVM sample application demonstrating tree layout"
    val jvmJar = tasks.named("jvmJar")
    dependsOn(jvmJar)
    val runtimeClasspath = kotlin.jvm().compilations["main"].runtimeDependencyFiles
    classpath = files(jvmJar.map { (it as Jar).archiveFile }, runtimeClasspath)
    mainClass.set("io.github.linde9821.treelayout.sample.SampleAppKt")
}
