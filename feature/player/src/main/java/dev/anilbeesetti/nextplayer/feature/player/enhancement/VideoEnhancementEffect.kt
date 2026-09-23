package dev.anilbeesetti.nextplayer.feature.player.enhancement

import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.RgbMatrix
import dev.anilbeesetti.nextplayer.core.model.VideoEnhancementSettings

/**
 * Builds Media3 [Effect] list from [VideoEnhancementSettings].
 *
 * Uses built-in Media3 effects only (Brightness, Contrast, HslAdjustment, RgbMatrix)
 * so the project builds cleanly against media3-effect 1.11.x.
 */
@OptIn(UnstableApi::class)
object VideoEnhancementEffect {

    fun createEffects(settings: VideoEnhancementSettings): List<Effect> {
        if (!settings.enabled || settings.isDefault()) return emptyList()

        val effects = mutableListOf<Effect>()

        // Brightness: Media3 range is -1f .. 1f
        val brightnessNorm = (settings.brightness / 100f * 0.35f).coerceIn(-1f, 1f)
        if (brightnessNorm != 0f) {
            effects.add(Brightness(brightnessNorm))
        }

        // Contrast: Media3 range is typically -1f .. 1f
        val contrastNorm = (settings.contrast / 100f * 0.5f).coerceIn(-1f, 1f)
        if (contrastNorm != 0f) {
            effects.add(Contrast(contrastNorm))
        }

        // Extra contrast for perceived sharpness
        if (settings.sharpness > 20f) {
            val extra = (settings.sharpness / 100f * 0.25f).coerceIn(0f, 0.4f)
            effects.add(Contrast(extra))
        }

        // Saturation via HSL (-100 .. 100 relative)
        val satAdj = (settings.saturation / 100f * 80f).coerceIn(-100f, 100f)
        if (satAdj != 0f) {
            effects.add(
                HslAdjustment.Builder()
                    .adjustSaturation(satAdj)
                    .build(),
            )
        }

        // Gamma / shadows / highlights via 4x4 RgbMatrix
        val matrix = buildToneMatrix4x4(settings)
        if (matrix != null) {
            effects.add(
                object : RgbMatrix {
                    override fun getMatrix(
                        presentationTimeUs: Long,
                        useHdr: Boolean,
                    ): FloatArray = matrix
                },
            )
        }

        return effects
    }

    /**
     * Media3 [RgbMatrix] expects a **4x4** column-major matrix (16 floats),
     * not a 4x5 ColorMatrix.
     */
    private fun buildToneMatrix4x4(s: VideoEnhancementSettings): FloatArray? {
        if (s.gamma == 0f && s.shadows == 0f && s.highlights == 0f) return null

        val gammaScale = (1f - s.gamma / 100f * 0.3f).coerceIn(0.6f, 1.4f)
        val shadowsLift = (s.shadows / 100f * 0.2f).coerceIn(-0.25f, 0.25f)
        val highlightsGain = (1f + s.highlights / 100f * 0.25f).coerceIn(0.7f, 1.4f)
        val scale = gammaScale * ((highlightsGain + 1f) / 2f)

        // Column-major 4x4: scale RGB + translate for shadows lift
        return floatArrayOf(
            scale, 0f, 0f, 0f,
            0f, scale, 0f, 0f,
            0f, 0f, scale, 0f,
            shadowsLift, shadowsLift, shadowsLift, 1f,
        )
    }
}
