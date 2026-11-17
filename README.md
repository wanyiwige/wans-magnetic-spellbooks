# Wan's Magnetic Spellbooks

Minecraft 1.20.1 / Forge 47.x 的磁化洞穴法术附属模组，从 Wan's Iron Expansion 当前源码提取。独立模组 ID 为 `wans_magnetic_spellbooks`，Java 包为 `cn.wanyiwige.magneticspellbooks`。无需安装 Wan's Iron Expansion。

原附属模组的源码和已安装 JAR 保持原样。独立版用于另行使用；两者同时安装会出现两套磁化内容，并同时适配 TravelOptics 的相同法术，本次未验证这种组合。原版存档中的旧 ID 不会自动转换成独立版 ID。

## 内容

- 16 个自制法术：磁化、绯红牵引、湛蓝斥退、磁念御器、磁悬浮、诺托尔扫描、磁链坠锤、富铁聚合、特斯拉弧核、双极涡流、极性坍缩、磁轨星枪、双极压砧、磁化裂链、极性逆转、铁砂磁暴。
- TravelOptics 的电磁脉冲、磁控机兵部署、方铅碎击保留原有磁极学派适配、配置与视觉修改；这三个法术和方铅极化器本体由 TravelOptics 提供。
- 10 个注册物品：磁极卷轴、符文、升级法球、超频磁铁法术书、双极钕锭、四件磁极法师装备、磁极法师刷怪蛋。
- 磁极法师、召唤物和战斗实体，学派强度与抗性、状态效果、卷轴工作台兼容、配方、模型、贴图、动画和中英文本地化。

法术参数、装备属性与获取方式沿用提取时的原工程；本次没有新增装备配方或自然生成。

## 安装

使用 `build/libs/wans_magnetic_spellbooks-1.0.1.jar`。先在单独的 Forge 1.20.1 实例中安装以下前置及其各自声明的前置，再将本 JAR 放入 `mods`。

| 直接前置 | 声明版本 |
| --- | --- |
| Iron's Spells 'n Spellbooks | 1.20.1-3.16.3 及以上 |
| Alex's Caves | 2.0.2 及以上 |
| Citadel | 2.6.3 及以上 |
| Curios | 5.14.1 至 6.0 之前 |
| GeckoLib | 4.8.3 至 5.0 之前 |
| TravelOptics | 6.3.0-1.20.1 及以上 |

本地隔离验证使用 TravelOptics 7.1.0、GeckoLib 4.8.4；完整前置名单见 `verification/server-dependencies.json`。声明的版本范围不代表每个版本都已测试。

## 构建与检查

使用 Java 17，在工程根目录运行：

```powershell
.\gradlew.bat build --no-daemon
py -3.13 tools/verify_release.py
```

`libs/` 中是本机前置 JAR，仅供编译使用，不嵌入发布 JAR。源码压缩包不包含前置 JAR，重建前需按 `build.gradle` 的文件名准备这些依赖。

提取记录为 `extraction-manifest.json`；静态检查报告为 `verification/static-report.json`。隔离测试服在 `verification/server/`，只用于验证，不属于发布文件。客户端渲染、手持外观和实际操作需要在游戏中另行检查。

本次验证已完成构建和 `reobfJar`、资源静态检查，以及未安装原附属模组的专用服务器加载和磁极法师召唤检查。已有 9 项战斗 GameTest 已在隔离世界启动，随后 `test runfailed` 返回没有失败测试。测试日志还包含前置模组的部分标签与战利品表解析错误，详见 `verification/runtime-report.md`；这些错误未阻止服务器进入世界。

## 1.0.1 更新

绯红牵引、湛蓝斥退、磁念御器、诺托尔扫描、极性坍缩、双极压砧、磁化裂链、极性逆转在缺少有效生物目标时，于快捷栏上方显示红色提示：“未选中生物，请瞄准有效目标后施放。”牵引和斥退也会在施法前检查目标。自我增益、区域法术、可对地面施放的法术及磁轨星枪沿用原有目标规则。

1.0.1 已执行构建和资源检查；下方运行报告中的隔离服及战斗 GameTest 记录属于 1.0.0，新增提示尚未在客户端观察。
