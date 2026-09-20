# Needs of Nature × Identity2 Bridge (`non_identity2_bridge`)

[![Latest Release](https://img.shields.io/github/v/release/DeconstructedCube/non_identity2_bridge?color=blue)](https://github.com/DeconstructedCube/non_identity2_bridge/releases/latest)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg)](https://minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.18.2-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

An ultra-lightweight, non-invasive Fabric compatibility bridge between **Needs of Nature (NoN)** and **Identity2** on Minecraft 1.21.11.

---

## 📖 Overview & Problem Statement

In an unbridged environment, when a player morphs into an animal (such as a Wolf, Cat, Fox, Bee, etc.) via **Identity2** and interacts with another player:
1. **Role Mismatch**: NoN queries raw entity types (`minecraft:player`) during animation candidate matching, causing the right-click interaction menu to only match human player animations instead of animal roles.
2. **Missing Animal Candidate Entries**: Animal-specific animation roles defined in NoN animation packs cannot be triggered because the player's active morph identity is ignored during matching.
3. **Geometry Negotiation Desync**: The server broadcasts default player model roots instead of the corresponding animal GeckoLib model roots to clients.

### The Solution
This bridge connects NoN's actor matching and server model broadcasting pipelines with zero invasive client overrides:
- **Zero Asset Overhead**: Directly reuses NoN's native GeckoLib animal models and animations without introducing duplicate files or extra render layers.
- **Direct Live Instance Detection**: Checks the live morph entity instance from Identity2 (`IdentityApi.getCurrentMorph`) rather than depending on unsynced NBT tags.
- **Minimal Non-Invasive Footprint**: Comprises only 2 targeted redirects on the server and data layer, with zero fragile client-side mixin-on-mixins.
- **Strict Quality Control**: Enforced with automated compiler-level static analysis (`-Xlint:all -Werror`) and Checkstyle validation on every build.

---

## 🛠️ Architecture & How It Works

```
[Morphed Player (Identity2)]
            │
            ▼
 1. MatchActor Redirection ─────► Redirects entity.getType() and isBaby() in
            │                     AnimationDefinitions$MatchActor to the live morph entity
            ▼
 2. Dynamic Actor Tags ─────────► Injects actor.morph, actor.feral, species tags,
            │                     and inherits gender tags to satisfy definition constraints
            ▼
 3. Server Model Negotiation ───► Broadcasts matching animal GeckoLib model roots
                                  to clients via ServerAnimationController
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

The compiled mod JAR will be generated under `build/libs/non_identity2_bridge-1.0.3+1.21.11.jar`.

---

## 📜 License

This project is licensed under the terms of the [MIT License](LICENSE).
