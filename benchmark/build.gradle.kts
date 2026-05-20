plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":library"))
            implementation(libs.lets.plot.kotlin.jvm)
            implementation(libs.lets.plot.image.export)
            implementation(libs.lets.plot.platf.awt)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.register<JavaExec>("runBenchmark") {
    description = "Run the TreeLayoutKMP benchmark"
    mainClass.set("io.github.linde9821.treelayout.benchmark.MainKt")
    classpath = kotlin.jvm().compilations["main"].runtimeDependencyFiles +
            kotlin.jvm().compilations["main"].output.allOutputs
    jvmArgs = listOf("-Xmx8g", "-XX:+UseG1GC", "-XX:+AlwaysPreTouch")
}
