pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TreeLayoutKMP-Android-Sample"

includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.github.linde9821:treelayout-kmp")).using(project(":library"))
    }
}
