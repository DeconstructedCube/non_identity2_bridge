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
1. **Model Reversion & T-Pose Freeze**: NoN client-side render pipeline queries the raw entity type (`minecraft:player`). It loads the default human GeckoLib model (`player.m.geo.json`) during animal animations. Because the human model lacks animal bone keyframes, the player's limbs remain frozen at default rotations (T-Pose / standing freeze).
2. **Texture UV Misalignment**: NoN forces the player's 64x64 human skin onto the animal geometry, corrupting the UV mapping.
3. **Role Slot Mismatch**: NoN server-side candidate matching ignores the player's active morph identity, causing right-click menus to only list human interactions.

### The Solution
This bridge coordinates NoN's actor matching, server broadcasting, and client render pipelines:
- **Zero Asset Overhead**: Directly reuses NoN's native GeckoLib animal models and animations (`wolf.m.geo.json`, etc.) without introducing duplicate assets.
- **Accurate Model & Texture Resolution**: Directs NoN's client render state to load matching animal geometry and textures, eliminating T-Pose freezes, UV distortions, and form reversions.
- **Direct Live Instance Detection**: Evaluates the live morph entity from Identity2 (`IdentityApi.getCurrentMorph`) on both client and server sides.
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
            │                     to clients via ServerAnimationController
            ▼
 4. Client Model & UV Takeover ─► Injects morphed EntityType into NoN render state
                                  and suppresses human skin override, ensuring proper animal
                                  GeckoLib models and textures are loaded!
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

The compiled mod JAR will be generated under `build/libs/non_identity2_bridge-1.0.4+1.21.11.jar`.

---

## 📜 License

This project is licensed under the terms of the [MIT License](LICENSE).
