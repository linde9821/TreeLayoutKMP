plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":library"))
            implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.13.0")
            implementation("org.jetbrains.lets-plot:lets-plot-image-export:4.8.2")
            implementation("org.jetbrains.lets-plot:platf-awt:4.9.0")
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
