package io.github.linde9821.treelayout.radial.angular

/**
 * Configuration for the direct angular placement layout algorithm.
 *
 * @property layerDistance Radial distance between concentric depth rings.
 * @property rotation Angular offset (in radians) applied to all nodes.
 */
public data class DirectAngularPlacementConfiguration(
    public val layerDistance: Float = 1.0f,
    public val rotation: Float = 0.0f,
)
