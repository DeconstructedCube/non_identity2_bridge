# Needs of Nature × Identity2 Bridge (`non_identity2_bridge`)

[![Latest Release](https://img.shields.io/github/v/release/DeconstructedCube/non_identity2_bridge?color=blue)](https://github.com/DeconstructedCube/non_identity2_bridge/releases/latest)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg)](https://minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.18.2-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

An ultra-lightweight, high-performance Fabric compatibility bridge between **Needs of Nature (NoN)** and **Identity2** on Minecraft 1.21.11.

---

## 📖 Overview & Problem Statement

In an unbridged environment, when a player morphs into an animal (such as a Wolf, Cat, Fox, Bee, etc.) via **Identity2** and interacts with another player or mob:
1. **Model Reversion & Interaction Mismatch**: NoN queries the underlying entity type as `minecraft:player`. You cannot trigger animal animations from menus, and entering an animation reverts you back into human form.
2. **Camera Rotation Drift**: Because Identity2 syncs the player's live mouse yaw to the morph entity every frame, turning the camera during an animation causes the animal model to spin along with the camera view.
3. **Animal Variants & Texture Packs**: Vanilla 1.21.11 features extensive mob variants (9 wolf biome variants, cat breeds, dyed sheep, etc.) alongside custom resource packs that standard hardcoded fallback paths fail to reflect.

### The Solution
This bridge coordinates NoN's actor matching, server broadcasting, and client render pipelines:
- **Zero Asset Overhead**: Directly reuses NoN's native GeckoLib animal models and animations (`wolf.m.geo.json`, etc.) without introducing duplicate assets.
- **Orientation Lock During Animation**: Locks `bodyRot`, `yRot`, and `xRot` to the animation's anchor orientation on the client render state, completely preventing the animal model from spinning when turning the camera.
- **Dynamic 1.21.11 Animal Variants & Resource Pack Support**: Resolves textures directly through the morph's vanilla `LivingEntityRenderer` pipeline, preserving all 9 wolf variants, cat breeds, tamed collars, dyed sheep, and active resource pack overrides.
- **Zero-Allocation Actor Tag Cache**: Eliminates heap churn and GC pauses during per-tick candidate scanning by caching immutable tag sets per morph type and gender.
- **Strict Fabric Mixin Compliance**: Exclusively targets standard Minecraft classes (`EntityRenderDispatcher`) and public classes with deterministic ordering (`priority = 1500`), avoiding nested mixin issues.

---

## 🛠️ Architecture & How It Works

```
[Morphed Player (Identity2)]
            │
            ▼
 1. MatchActor Redirection ─────► Redirects entity.getType(), isBaby(), and
            │                     EntityVariants.resolveVariant() to the live morph entity
            ▼
 2. Zero-Allocation Tags ───────► Resolves actor tags with high-performance concurrent cache;
            │                     injects actor.morph, actor.feral, and inherits gender tags
            ▼
 3. Server Model Negotiation ───► Broadcasts matching animal GeckoLib model roots
            │                     to clients via ServerAnimationController
            ▼
 4. Orientation & Render Hook ──► Injects into EntityRenderDispatcher (priority = 1500)
            │                     to lock bodyRot/yRot to animation angles and re-prepare GeckoLib
            ▼
 5. Dynamic Variant Texture ────► Resolves actual animal variant/resource pack texture via
                                  the morph's LivingEntityRenderer, filtering out human skins!
```

---

## 📦 Requirements

| Dependency | Minimum Version | Type |
|---|---|---|
| **Minecraft** | `~1.21.11` | Fabric Environment |
| **Fabric Loader** | `>=0.18.2` | Mod Loader |
| **Fabric API** | Compatible release | Required |
| **Needs of Nature (NoN)** | `>=1.5.0` | **Hard Dependency** |
| **Identity2** | `>=2.2.0` | **Hard Dependency** |

---

## 🚀 Building from Source

To compile the mod from source code:

```bash
# Clone the repository
git clone https://github.com/DeconstructedCube/non_identity2_bridge.git
cd non_identity2_bridge

# Execute automated static analysis, code style checks, and build
./gradlew build
```

The compiled mod JAR will be generated under `build/libs/non_identity2_bridge-1.0.8+1.21.11.jar`.

---

## 📜 License

This project is licensed under the terms of the [MIT License](LICENSE).
