package io.github.linde9821.treelayout.radial.walker

import kotlin.math.PI

/**
 * Configuration for the radial Walker layout algorithm.
 *
 * @property layerDistance Distance between concentric depth rings.
 * @property margin Angular margin (in radians) subtracted from the full circle to avoid overlap.
 * @property rotation Angular offset (in radians) applied to all nodes.
 */
public data class RadialWalkerLayoutConfiguration(
    public val layerDistance: Float = 1.0f,
    public val margin: Float = 0.0f,
    public val rotation: Float = 0.0f,
) {
    internal val usableAngle: Float = 2 * PI.toFloat() - margin
}
