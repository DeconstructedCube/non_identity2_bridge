# 自然之需 × 身份识别2 兼容桥接模组 (Needs of Nature × Identity2 Bridge)

[![我的世界版本](https://img.shields.io/badge/我的世界-1.21.11-brightgreen.svg)](https://minecraft.net/)
[![Fabric 加载器](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.18.2-blue.svg)](https://fabricmc.net/)
[![Java 版本](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![开源协议](https://img.shields.io/badge/开源协议-MIT-green.svg)](LICENSE)

一个极简、无冗余开销的 Fabric 兼容桥接模组，专为在 **我的世界 1.21.11** 环境下打通 **自然之需 (Needs of Nature, NoN)** 与 **身份识别2 (Identity2)** 而构建。

---

## 📖 解决的问题

在未安装本兼容模组前，玩家使用 **Identity2** 变身为动物（如狼、猫、狐狸、蜜蜂等）并触发 **Needs of Nature (NoN)** 动画时，存在以下问题：
1. **形态强制还原**：NoN 在动画匹配期直接读取玩家实体的底层类型（`minecraft:player`），导致处于变身状态的玩家在进入动画瞬间被强制打回默认人类形态。
2. **动物角色槽位不匹配**：变身后的玩家无法作为对应的动物类型参与 NoN 动画包中定义的动物专属角色槽位。
3. **动画模型不同步**：服务端与客户端无法协商出正确的动物模型几何根，导致渲染错位。

### 本模组的解决方式
本模组在底层打通了 NoN 的实体角色匹配、服务端模型广播与客户端渲染接管链路：
- **零额外资源包袱**：直接复用 NoN 内置的原生动物 GeckoLib 模型与动画，不引入任何重复的模型文件与贴图层。
- **透明形态重定向**：无缝向 NoN 动画引擎提供玩家当前变身实体的形态类型、变种信息、幼年状态以及专属角色标签。
- **快速失败设计**：去除所有防御性伪装代码，配备编译器级静态分析与代码规范约束。

---

## 🛠️ 工作原理

```
【Identity2 变身玩家】
         │
         ▼
 1. MatchActor 角色匹配重定向 ───► 将实体的类型、幼年状态与变种重定向为变身目标形态（例如 minecraft:wolf）
         │
         ▼
 2. 角色标签动态供给 ───────────► 自动注入 actor.morph、actor.feral 及动物专属角色标签
         │
         ▼
 3. 服务端模型根广播 ───────────► 确保服务端向下分发的模型根路径与变身动物形态一致
         │
         ▼
 4. 客户端渲染接管 ─────────────► 将变身形态标识注入动画渲染状态，由 NoN 原生渲染器直接加载对应的动物动画模型！
```

---

## 📦 前置依赖

运行本模组需要以下环境与前置组件：

| 依赖项 | 最低版本要求 | 说明 |
|---|---|---|
| **我的世界 (Minecraft)** | `~1.21.11` | Fabric 环境 |
| **Fabric Loader** | `>=0.18.2` | 模组加载器 |
| **Fabric API** | 对应版本 | 基础 API 支持 |
| **Needs of Nature (NoN)** | `>=1.5.0` | **必需硬依赖** |
| **Identity2** | `>=2.2.0` | **必需硬依赖** |

---

## 🚀 源码构建

如需从源码自行编译本模组，请执行以下命令：

```bash
# 克隆仓库
git clone https://github.com/DeconstructedCube/non_identity2_bridge.git
cd non_identity2_bridge

# 执行全自动静态分析、代码规范校验与构建打包
./gradlew build
```

编译完成后的模组 Jar 文件位于 `build/libs/` 目录下。

---

## 📜 开源协议

本项目采用 [MIT 开源协议](LICENSE) 进行分发。
