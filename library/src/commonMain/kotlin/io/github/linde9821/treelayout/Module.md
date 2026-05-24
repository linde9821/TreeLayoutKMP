# Module TreeLayoutKMP

A Kotlin Multiplatform library for computing tidy tree layouts using the Walker/Buchheim algorithm in O(n) time.
Includes transformation utilities for adapting coordinates to any rendering target, animation support for smooth
transitions between layout states, and zero-dependency JSON serialization for caching or transmitting results.

## Getting Started

Create a [TreeAdapter][io.github.linde9821.treelayout.TreeAdapter] for your tree structure, then pass it
to [WalkerTreeLayout][io.github.linde9821.treelayout.walker.WalkerTreeLayout]:

```kotlin
val layout = WalkerTreeLayout(adapter, WalkerLayoutConfiguration(horizontalDistance = 2f))
val result = layout.layout()
val position = result.getPosition(myNode)
```

## Layout Algorithms

| Algorithm         | Class                                                                                                      | Use Case                          |
|-------------------|------------------------------------------------------------------------------------------------------------|-----------------------------------|
| Walker (Buchheim) | [WalkerTreeLayout][io.github.linde9821.treelayout.walker.WalkerTreeLayout]                                 | Standard tidy tree layout         |
| Radial Walker     | [RadialWalkerTreeLayout][io.github.linde9821.treelayout.radial.walker.RadialWalkerTreeLayout]              | Concentric ring layout            |
| Direct Angular    | [DirectAngularPlacementLayout][io.github.linde9821.treelayout.radial.angular.DirectAngularPlacementLayout] | Proportional angular partitioning |

# Package io.github.linde9821.treelayout

Core interfaces and data types for tree layout computation.

# Package io.github.linde9821.treelayout.walker

Walker/Buchheim O(n) tidy tree layout algorithm.

# Package io.github.linde9821.treelayout.radial.walker

Radial variant of the Walker algorithm placing nodes on concentric rings.

# Package io.github.linde9821.treelayout.radial.angular

Direct angular partitioning layout for radial trees.

# Package io.github.linde9821.treelayout.result

Layout result types, transformation utilities, and bounding box helpers.
[TreeLayoutResult][io.github.linde9821.treelayout.result.TreeLayoutResult] is a concrete class providing chainable
coordinate transforms (`mapped`, `centered`, `scaledTo`, `normalized`, `translated`).
[LayoutTransition][io.github.linde9821.treelayout.result.LayoutTransition] enables animated interpolation between
two layout states. Extension functions `toJson` and `fromJson` provide zero-dependency JSON serialization.
