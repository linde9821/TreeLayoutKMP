# Benchmark

Performance benchmark for the TreeLayoutKMP library.

## What it does

Generates balanced trees with increasing node counts (stepping by 15K up to ~1.1M) and measures the time to compute a
full layout for each. Results are plotted as a line chart and exported to `benchmark_results.png` using Kotlin's
lets-plot library.

## Output

A PNG chart (`benchmark_results.png`) with nodes on the x-axis and computation time (ms) on the y-axis.

## Running

From the project root:

```bash
./gradlew :benchmark:jvmRun
```

## Running tests

```bash
./gradlew :benchmark:jvmTest
```
