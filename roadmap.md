# Feature Summary — Making TreeLayoutKMP Production-Viable

## Current State

TreeLayoutKMP is a functional KMP library implementing the Buchheim–Jünger–Leipert (Walker) tree layout algorithm in O(n) time. It ships with:

- Pure Kotlin `commonMain` implementation (zero platform dependencies)
- `TreeAdapter<T>` interface for zero-copy integration with any tree model
- Configurable horizontal/vertical spacing
- Full multiplatform target coverage (JVM, Android, iOS, Linux x64, JS, Wasm)
- CI via GitHub Actions (build + test on all targets)
- Maven Central publishing

## Features Needed for Developer Adoption

### 1. Variable Node Sizes

**Priority: High**

Currently all nodes are treated as dimensionless points. Real-world trees have nodes with varying widths and heights (labels, icons, content boxes). The layout must account for node extents to prevent overlap.

- Add a `NodeExtentProvider<T>` interface with `width(node: T): Float` and `height(node: T): Float`
- Integrate extents into spacing calculations during the Walker passes
- Default to uniform size (1×1) for backward compatibility

### 2. Layout Orientation

**Priority: High**

The algorithm currently only produces top-down layouts. Developers need flexibility for different UI contexts.

- Support four orientations: `TopToBottom`, `BottomToTop`, `LeftToRight`, `RightToLeft`
- Add an `orientation` property to `WalkerLayoutConfiguration`
- Transform coordinates in the second walk based on orientation

### 3. Alignment Options

**Priority: Medium**

Allow control over how parent nodes align relative to their children.

- Center (current default)
- Left-aligned
- Right-aligned
- Custom anchor point

### 4. Bounds and Normalization

**Priority: Medium**

- Add `getBounds(): Rect` to `TreeLayoutResult` returning the bounding box of the entire layout
- Add a `normalize()` option to shift coordinates so the top-left is at (0, 0)
- Simplifies viewport/camera setup in rendering code

### 5. Error Handling and Validation

**Priority: High**

- Validate adapter consistency (e.g., `parent(child) == node` for all children)
- Detect cycles and throw a clear exception
- Provide meaningful error messages for malformed trees

### 6. Documentation and Samples

**Priority: High**

- KDoc on all public API members (already partially done)
- Published API docs (Dokka → GitHub Pages)
- Platform-specific rendering samples:
  - Jetpack Compose (Android/Desktop)
  - SwiftUI (iOS)
  - Canvas (JS/Wasm)
  - SVG export utility

### 7. Performance Benchmarks

**Priority: Low**

- Add benchmarks for trees of 1K, 10K, 100K nodes
- Publish results in README or docs
- Helps developers evaluate suitability for their scale

### 8. Stable API

**Priority: High**

- Remove the "under active development" warning once the above features stabilize
- Follow semantic versioning strictly
- Annotate experimental APIs with `@ExperimentalTreeLayoutApi`
- Provide migration guides between breaking versions


