package dev.anilbeesetti.nextplayer.feature.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dev.anilbeesetti.nextplayer.core.model.VideoEnhancementSettings
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.feature.player.enhancement.VideoEnhancementEffect

@OptIn(UnstableApi::class)
@Composable
fun BoxScope.VideoEnhancementSelectorView(
    modifier: Modifier = Modifier,
    show: Boolean,
    settings: VideoEnhancementSettings,
    player: Player?,
    onSettingsChange: (VideoEnhancementSettings) -> Unit,
) {
    OverlayView(
        modifier = modifier,
        show = show,
        title = stringResource(R.string.video_enhancement),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusable(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.video_enhancement_enable),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = { enabled ->
                        applyAndNotify(settings.copy(enabled = enabled), player, onSettingsChange)
                    },
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.video_enhancement_preset),
                style = MaterialTheme.typography.labelLarge,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoEnhancementSettings.PRESETS) { (name, preset) ->
                    val selected = settings == preset ||
                        (
                            name != "Off" &&
                                settings.enabled &&
                                settings.sharpness == preset.sharpness &&
                                settings.gamma == preset.gamma
                            )
                    FilterChip(
                        selected = selected,
                        onClick = {
                            applyAndNotify(preset, player, onSettingsChange)
                        },
                        label = { Text(name) },
                        modifier = Modifier.focusable(),
                    )
                }
            }

            if (settings.enabled) {
                EnhancementSlider(
                    label = stringResource(R.string.video_enhancement_sharpness),
                    value = settings.sharpness,
                    valueRange = 0f..100f,
                    onValueChange = {
                        applyAndNotify(settings.copy(sharpness = it), player, onSettingsChange)
                    },
                )
                EnhancementSlider(
                    label = stringResource(R.string.video_enhancement_blur),
                    value = settings.blur,
                    valueRange = 0f..100f,
                    onValueChange = {
                        applyAndNotify(settings.copy(blur = it), player, onSettingsChange)
                    },
                )
                EnhancementSlider(
                    label = stringResource(R.string.video_enhancement_highlights),
                    value = settings.highlights,
                    valueRange = -100f..100f,
                    onValueChange = {
                        applyAndNotify(settings.copy(highlights = it), player, onSettingsChange)
                    },
                )
                EnhancementSlider(
                    label = stringResource(R.string.video_enhancement_gamma),
                    value = settings.gamma,
                    valueRange = -100f..100f,
                    onValueChange = {
                        applyAndNotify(settings.copy(gamma = it), player, onSettingsChange)
                    },
                )
                EnhancementSlider(
                    label = stringResource(R.string.video_enhancement_shadows),
                    value = settings.shadows,
                    valueRange = -100f..100f,
                    onValueChange = {
                        applyAndNotify(settings.copy(shadows = it), player, onSettingsChange)
                    },
                )
                EnhancementSlider(
                    label = stringResource(R.string.video_enhancement_saturation),
                    value = settings.saturation,
                    valueRange = -100f..100f,
                    onValueChange = {
                        applyAndNotify(settings.copy(saturation = it), player, onSettingsChange)
                    },
                )
                EnhancementSlider(
                    label = stringResource(R.string.video_enhancement_brightness),
                    value = settings.brightness,
                    valueRange = -100f..100f,
                    onValueChange = {
                        applyAndNotify(settings.copy(brightness = it), player, onSettingsChange)
                    },
                )
                EnhancementSlider(
                    label = stringResource(R.string.video_enhancement_contrast),
                    value = settings.contrast,
                    valueRange = -100f..100f,
                    onValueChange = {
                        applyAndNotify(settings.copy(contrast = it), player, onSettingsChange)
                    },
                )

                TextButton(
                    onClick = {
                        applyAndNotify(VideoEnhancementSettings.DEFAULT, player, onSettingsChange)
                    },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(R.string.video_enhancement_reset))
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
private fun applyAndNotify(
    settings: VideoEnhancementSettings,
    player: Player?,
    onSettingsChange: (VideoEnhancementSettings) -> Unit,
) {
    (player as? ExoPlayer)?.setVideoEffects(VideoEnhancementEffect.createEffects(settings))
    onSettingsChange(settings)
}

@Composable
private fun EnhancementSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .focusable(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
