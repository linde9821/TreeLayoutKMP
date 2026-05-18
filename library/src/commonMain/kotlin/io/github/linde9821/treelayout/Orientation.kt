package io.github.linde9821.treelayout

/**
 * Determines the direction in which the tree grows from root to leaves.
 */
public enum class Orientation {
    /** Root at top, leaves grow downward. */
    TopToBottom,
    /** Root at bottom, leaves grow upward. */
    BottomToTop,
    /** Root at left, leaves grow rightward. */
    LeftToRight,
    /** Root at right, leaves grow leftward. */
    RightToLeft,
}
