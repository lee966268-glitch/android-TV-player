package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.Serializable

/**
 * Video quality enhancement settings inspired by Video Quality Enhancer Chrome extension.
 * Values are typically in range [-100, 100] or [0, 100] depending on the parameter.
 * Applied via Media3 GlEffects / ColorMatrix + custom sharpen shader.
 */
@Serializable
data class VideoEnhancementSettings(
    /** Master enable switch */
    val enabled: Boolean = false,

    /**
     * Sharpness / unsharp mask strength.
     * 0 = off, positive = sharpen, range typically 0..100 (maps to amount 0..2.0)
     */
    val sharpness: Float = 0f,

    /**
     * Blur amount used by unsharp mask (pre-blur radius).
     * Higher values with high sharpness give stronger edge contrast.
     * Range 0..100 (maps to sigma ~0..3)
     */
    val blur: Float = 0f,

    /**
     * Highlights recovery / lift. Positive brightens highlights.
     * Range -100..100
     */
    val highlights: Float = 0f,

    /**
     * Gamma correction. Positive increases mid-tone brightness (lower gamma).
     * Range -100..100  (maps to gamma ~0.5..2.0)
     */
    val gamma: Float = 0f,

    /**
     * Shadows lift / crush. Positive brightens shadows.
     * Range -100..100
     */
    val shadows: Float = 0f,

    /**
     * Saturation. 0 = unchanged, positive increases color intensity.
     * Range -100..100
     */
    val saturation: Float = 0f,

    /**
     * Overall brightness offset.
     * Range -100..100
     */
    val brightness: Float = 0f,

    /**
     * Overall contrast.
     * Range -100..100
     */
    val contrast: Float = 0f,
) {
    fun isDefault(): Boolean {
        if (!enabled) return true
        return sharpness == 0f &&
            blur == 0f &&
            highlights == 0f &&
            gamma == 0f &&
            shadows == 0f &&
            saturation == 0f &&
            brightness == 0f &&
            contrast == 0f
    }

    companion object {
        val DEFAULT = VideoEnhancementSettings()

        /** Presets matching the Chrome extension naming */
        val PRESET_HD = VideoEnhancementSettings(
            enabled = true,
            sharpness = 30f,
            blur = 100f,
            highlights = 5f,
            gamma = 20f,
            shadows = 10f,
            saturation = 5f,
        )
        val PRESET_FHD = VideoEnhancementSettings(
            enabled = true,
            sharpness = 40f,
            blur = 100f,
            highlights = 8f,
            gamma = 30f,
            shadows = 20f,
            saturation = 5f,
        )
        val PRESET_2K = VideoEnhancementSettings(
            enabled = true,
            sharpness = 55f,
            blur = 100f,
            highlights = 11f,
            gamma = 40f,
            shadows = 30f,
            saturation = 5f,
        )
        val PRESET_4K = VideoEnhancementSettings(
            enabled = true,
            sharpness = 70f,
            blur = 100f,
            highlights = 15f,
            gamma = 50f,
            shadows = 40f,
            saturation = 0f,
        )

        val PRESETS = listOf(
            "Off" to DEFAULT,
            "HD" to PRESET_HD,
            "FHD" to PRESET_FHD,
            "2K" to PRESET_2K,
            "4K" to PRESET_4K,
        )
    }
}
