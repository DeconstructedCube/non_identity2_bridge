# Needs of Nature × Identity2 Bridge (`non_identity2_bridge`)

[![Latest Release](https://img.shields.io/github/v/release/DeconstructedCube/non_identity2_bridge?color=blue)](https://github.com/DeconstructedCube/non_identity2_bridge/releases/latest)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg)](https://minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.18.2-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A Fabric compatibility layer between **Needs of Nature (NoN)** and **Identity2**.

## Technical Details

This bridge resolves conflicts between NoN's player-centric animation and rendering assumptions and Identity2's morph mechanics:

- **Entity Matching**: Intercepts `entity.getType()`, `isBaby()`, and `EntityVariants.resolveVariant()` to return the morph's data instead of `minecraft:player`. This ensures NoN assigns animal animation clips (e.g., `p1_fox`) instead of human clips during multi-actor animations.
- **Texture Resolution**: Reuses NoN's `RenderState` and utilizes generic type erasure via `LivingEntityRenderer` to extract vanilla morph textures (including variants and resource packs) without reflection.
- **Render State Correction**: Bypasses NoN's `resolveDestroyedSkinBaseTexture` and skin-part cube-hiding for morphed players, preventing human textures from being mapped onto animal models.
- **Orientation Lock**: Locks `bodyRot`, `yRot`, and `xRot` on the client render state to the animation's anchor orientation, preventing the model from rotating with the camera.
- **Actor Tags Cache**: Implements a concurrent cache for morph actor tags (`actor.morph`, `actor.feral`, gender traits) to prevent allocation overhead during tick scans.
- **Mixin Compliance**: Targets standard Minecraft classes and public classes with deterministic ordering (`priority = 1500`) to minimize mixin conflicts.

## Architecture Pipeline

```mermaid
graph TD
    Player[Morphed Player] --> |MatchActorMixin| Type[Redirect Entity Type & Variant]
    Type --> |Identity2ActorHelper| Tags[Inject Actor Tags Cache]
    Tags --> |ServerAnimationController| Broadcast[Broadcast Animal GeckoLib Model]
    Broadcast --> |EntityRenderDispatcherMixin| Orientation[Lock Animation Orientation]
    Orientation --> |Identity2ClientActorHelper| Texture[Resolve Native Morph Texture]
    Texture --> |NeedsOfNatureClientMixin| Render[Bypass Human Skin Overrides]
```
---

## 📦 Requirements

| Component | Minimum Version | Note |
| :--- | :--- | :--- |
| **Minecraft** | `~1.21.11` | Fabric Environment |
| **Fabric Loader** | `>=0.18.2` | |
| **Needs of Nature (NoN)** | `>=1.5.0` | Required |
| **Identity2** | `>=2.2.0` | Required |

---

## 🚀 Building from Source
```bash
git clone https://github.com/DeconstructedCube/non_identity2_bridge.git
cd non_identity2_bridge
./gradlew build
```

---

## 📜 License

This project is licensed under the terms of the [MIT License](LICENSE).
