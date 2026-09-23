# Video Quality Enhancement (Android TV)

Inspired by the **Video Quality Enhancer** Chrome extension (锐化 / 亮度 / 伽马 / 色彩修复).

## What was added

| File | Purpose |
|------|---------|
| `core/model/.../VideoEnhancementSettings.kt` | Serializable settings + HD/FHD/2K/4K presets |
| `feature/player/.../enhancement/VideoEnhancementEffect.kt` | Builds Media3 `GlEffect` list from settings |
| `feature/player/.../ui/VideoEnhancementPanel.kt` | Compose panel (D-pad friendly for Android TV) |
| `PlayerPreferences.videoEnhancement` | Persisted preference field |
| `media3-effect` dependency | Required for `Brightness`, `Contrast`, `HslAdjustment`, `RgbMatrix` |

## How to apply effects at runtime

In `PlayerService` (or wherever you hold the `ExoPlayer` instance):

```kotlin
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dev.anilbeesetti.nextplayer.feature.player.enhancement.VideoEnhancementEffect

@OptIn(UnstableApi::class)
fun ExoPlayer.applyVideoEnhancement(settings: VideoEnhancementSettings) {
    setVideoEffects(VideoEnhancementEffect.createEffects(settings))
}
```

Call this when:
- Player is created / preferences load
- User changes a slider or preset in `VideoEnhancementPanel`

Example after building the player in `PlayerService.onCreate()`:

```kotlin
val enhancement = playerPreferences.videoEnhancement
if (enhancement.enabled) {
    player.setVideoEffects(VideoEnhancementEffect.createEffects(enhancement))
}
```

## UI integration (Android TV)

Show the panel from the player controls (e.g. a new button in the control bar or a long-press menu item). Suggested placement in `MediaPlayerScreen` / controls:

```kotlin
var showEnhancement by remember { mutableStateOf(false) }

// Button (TV focusable)
IconButton(onClick = { showEnhancement = true }) {
    Icon(Icons.Default.Tune, contentDescription = "画质增强")
}

if (showEnhancement) {
    VideoEnhancementPanel(
        settings = currentSettings,
        onSettingsChange = { newSettings ->
            // 1. update ViewModel / PreferencesRepository
            // 2. player.setVideoEffects(VideoEnhancementEffect.createEffects(newSettings))
        },
        onDismiss = { showEnhancement = false },
    )
}
```

## Presets (same naming as the Chrome extension)

| Name | Sharpness | Blur | Highlights | Gamma | Shadows | Saturation |
|------|-----------|------|------------|-------|---------|------------|
| Off  | 0         | 0    | 0          | 0     | 0       | 0          |
| HD   | 30        | 100  | 5          | 20    | 10      | 5          |
| FHD  | 40        | 100  | 8          | 30    | 20      | 5          |
| 2K   | 55        | 100  | 11         | 40    | 30      | 5          |
| 4K   | 70        | 100  | 15         | 50    | 40      | 0          |

## Notes / limitations

- **No true AI 4K upscale** – the Chrome extension’s “4K” is also a filter preset (sharpness + tone), not neural super-resolution. Real AI SR needs models (e.g. Real-ESRGAN) + significant GPU cost and is not included.
- Effects run on the Media3 video pipeline (OpenGL). Prefer **software decoder** or devices with good GPU for high-res content if you enable heavy filters.
- Custom unsharp-mask GLSL can be added later by extending `BaseGlShaderProgram`; current version uses built-in Media3 effects for stability across Media3 1.11.x.
- Persist changes via existing `PreferencesRepository.updatePlayerPreferences { copy(videoEnhancement = newSettings) }`.

## Build

```bash
./gradlew :feature:player:assembleDebug
```

Ensure `libs.androidx.media3.effect` is resolved (added to `libs.versions.toml` and `feature/player/build.gradle.kts`).
EOF