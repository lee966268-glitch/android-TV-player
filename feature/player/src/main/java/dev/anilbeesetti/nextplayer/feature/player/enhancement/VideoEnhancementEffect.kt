package dev.anilbeesetti.nextplayer.feature.player.enhancement

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.common.Effect
import androidx.media3.effect.RgbMatrix
import dev.anilbeesetti.nextplayer.core.model.VideoEnhancementSettings

/**
 * Builds Media3 [GlEffect] list from [VideoEnhancementSettings].
 *
 * Mirrors practical filters of the Video Quality Enhancer Chrome extension:
 * brightness, contrast, saturation, gamma / shadows / highlights approximation.
 *
 * Apply with:
 * ```
 * (player as ExoPlayer).setVideoEffects(VideoEnhancementEffect.createEffects(settings))
 * ```
 */
@OptIn(UnstableApi::class)
object VideoEnhancementEffect {

    fun createEffects(settings: VideoEnhancementSettings): List<Effect> {
        if (!settings.enabled || settings.isDefault()) return emptyList()

        val effects = mutableListOf<Effect>()

        // Brightness (-1..1)
        val brightnessNorm = (settings.brightness / 100f * 0.35f).coerceIn(-0.5f, 0.5f)
        if (brightnessNorm != 0f) {
            effects.add(Brightness(brightnessNorm))
        }

        // Contrast (-1..1)
        val contrastNorm = (settings.contrast / 100f * 0.5f).coerceIn(-0.8f, 0.8f)
        if (contrastNorm != 0f) {
            effects.add(Contrast(contrastNorm))
        }

        // Extra perceived sharpness via mild contrast when sharpness is high
        if (settings.sharpness > 20f) {
            val extra = (settings.sharpness / 100f * 0.25f).coerceIn(0f, 0.4f)
            effects.add(Contrast(extra))
        }

        // Saturation via HSL
        val satAdj = (settings.saturation / 100f * 80f).coerceIn(-100f, 100f)
        if (satAdj != 0f) {
            effects.add(
                HslAdjustment.Builder()
                    .adjustSaturation(satAdj)
                    .build(),
            )
        }

        // Gamma + Shadows + Highlights via RgbMatrix
        val matrix = buildToneMatrix(settings)
        if (matrix != null) {
            effects.add(
                object : RgbMatrix {
                    override fun getMatrix(presentationTimeUs: Long, useHdr: Boolean): FloatArray =
                        matrix
                },
            )
        }

        return effects
    }

    private fun buildToneMatrix(s: VideoEnhancementSettings): FloatArray? {
        if (s.gamma == 0f && s.shadows == 0f && s.highlights == 0f) return null

        val gammaScale = (1f - s.gamma / 100f * 0.3f).coerceIn(0.6f, 1.4f)
        val shadowsLift = (s.shadows / 100f * 0.2f).coerceIn(-0.25f, 0.25f)
        val highlightsGain = (1f + s.highlights / 100f * 0.25f).coerceIn(0.7f, 1.4f)
        val scale = gammaScale * ((highlightsGain + 1f) / 2f)

        return floatArrayOf(
            scale, 0f, 0f, 0f, shadowsLift,
            0f, scale, 0f, 0f, shadowsLift,
            0f, 0f, scale, 0f, shadowsLift,
            0f, 0f, 0f, 1f, 0f,
        )
    }
}
