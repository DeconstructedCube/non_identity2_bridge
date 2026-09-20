# Needs of Nature × Identity2 Bridge (`non_identity2_bridge`)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg)](https://minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.18.2-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

An ultra-lightweight, zero-overhead Fabric compatibility bridge between **Needs of Nature (NoN)** and **Identity2** on Minecraft 1.21.11.

---

## 📖 Overview & Problem Statement

In an unbridged environment, when a player morphs into an animal (such as a Wolf, Cat, Fox, Bee, etc.) via **Identity2** and triggers a **Needs of Nature (NoN)** animation:
1. **Model Reversion**: NoN directly queries the raw entity type (`minecraft:player`) during animation candidate matching. Consequently, morphed players instantly revert to their default human player model inside animations.
2. **Role Slot Incompatibility**: Morphed players cannot match or fill animal-specific animation roles defined in NoN animation packs.
3. **Geometry Desynchronization**: The server and client fail to negotiate the appropriate animal GeckoLib model roots, leading to missing or corrupted rendering.

### The Solution
This bridge connects NoN's actor matching, server model broadcasting, and client render state pipelines:
- **Zero Asset Overhead**: Completely reuses NoN's native GeckoLib animal models and animations without introducing duplicate files or extra render layers.
- **Transparent Actor Redirection**: Supplies the morphed entity type, variant details, baby status, and custom actor tags directly to NoN's animation engine.
- **Fail-Fast & Streamlined**: Free of defensive exception swallowing, enforced with compiler-level static analysis and Checkstyle rule gates.

---

## 🛠️ Architecture & How It Works

```
[Morphed Player (Identity2)]
            │
            ▼
 1. MatchActor Redirection ─────► Redirects entity.getType(), isBaby(), and variant
            │                     to the morphed target entity (e.g. minecraft:wolf)
            ▼
 2. Dynamic Actor Tags ─────────► Injects actor.morph, actor.feral, and species tags
            │
            ▼
 3. Server Model Negotiation ───► Broadcasts matching animal model roots to clients
            │
            ▼
 4. Client Render Takeover ─────► Injects morphed Identifier into AnimationRenderStateAccess,
                                  prompting GeckoReplacedRender to load native animal models!
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

The compiled mod JAR will be generated under `build/libs/`.

---

## 📜 License

This project is licensed under the terms of the [MIT License](LICENSE).
