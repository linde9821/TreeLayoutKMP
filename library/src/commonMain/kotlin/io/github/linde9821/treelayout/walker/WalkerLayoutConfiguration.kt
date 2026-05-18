package io.github.linde9821.treelayout.walker

import io.github.linde9821.treelayout.Orientation

/**
 * Configuration for the Walker layout algorithm.
 *
 * @property horizontalDistance Minimum horizontal spacing between sibling nodes.
 * @property verticalDistance Vertical spacing between depth levels.
 * @property orientation Direction in which the tree grows from root to leaves.
 */
public data class WalkerLayoutConfiguration(
    public val horizontalDistance: Float = 1.0f,
    public val verticalDistance: Float = 1.0f,
    public val orientation: Orientation = Orientation.TopToBottom,
)