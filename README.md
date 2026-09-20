# Needs of Nature × Identity2 Bridge (`non_identity2_bridge`)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg)](https://minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.18.2-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

An ultra-lightweight, zero-overhead Fabric compatibility bridge between **Needs of Nature (NoN)** and **Identity2** on Minecraft 1.21.11.

---

## 📖 Background & Issues Resolved (背景与解决的问题)

### The Problem (原版痛点)
When a player morphs into an animal (e.g. Wolf, Cat, Fox, Bee, etc.) using **Identity2** and triggers a **Needs of Nature (NoN)** animation:
1. **Model Reversion**: NoN queries the raw entity type (`minecraft:player`) during animation matching, causing morphed players to instantly snap back to their default human player model inside the animation.
2. **Role Incompatibility**: Morphed players cannot match or occupy animal animation slots defined by NoN animation packs.
3. **Model Desync**: Server and client fail to negotiate the correct animal GeckoLib geometry roots.

### The Solution (解决方案)
This bridge intercepts NoN's entity matching, server model root negotiation, and client render state pipelines:
- **Zero Resource Burden**: Fully reuses NoN's native GeckoLib animal models (`wolf.m.geo.json`, `cat.m.geo.json`, etc.) without introducing duplicated geometry or bloated asset layers.
- **Clean Actor Redirection**: Transparently provides the player's morphed entity identity, variant, baby state, and actor tags (`actor.<entity_type>`, `actor.feral`, `actor.morph`) to NoN's animation engine.
- **Fail-Fast Architecture**: Zero defensive exception swallowing, equipped with automated compiler-level static analysis and Checkstyle rule gates.

---

## 🛠️ How It Works (工作原理)

```
[Morphed Player (Identity2)]
            │
            ▼
 1. MatchActorMixin ─────────────► Redirects entity.getType(), isBaby(), and variant
            │                      into Morphed EntityType (e.g. minecraft:wolf)
            ▼
 2. NonActorEvents ──────────────► Appends actor.morph, actor.feral, and animal tags
            │
            ▼
 3. ServerAnimationController ───► Broadcasts correct animal model roots to clients
            │
            ▼
 4. EntityRenderDispatcher ──────► Injects morphed Identifier into AnimationRenderStateAccess,
                                   causing GeckoReplacedRender to render the native animal model!
```

---

## 📦 Requirements (前置依赖)

| Dependency | Minimum Version | Note |
|---|---|---|
| **Minecraft** | `~1.21.11` | Fabric |
| **Fabric Loader** | `>=0.18.2` | |
| **Fabric API** | `*` | |
| **Needs of Nature** | `>=1.5.0` | **Hard Dependency** |
| **Identity2** | `>=2.2.0` | **Hard Dependency** |

---

## 🚀 Building from Source (构建方法)

```bash
# Clone the repository
git clone https://github.com/bsfdsagfadg/non_identity2_bridge.git
cd non_identity2_bridge

# Build with automated static analysis & checkstyle validation
./gradlew build
```

Built jars will be located under `build/libs/`.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
