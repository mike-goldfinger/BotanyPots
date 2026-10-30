# botany-mystical – Mystical Agriculture crops in Botany Pots (data pack)

[Mystical Agriculture](https://github.com/BlakeBr0/MysticalAgriculture) crops have **no loot tables**:
essence, the second-seed chance and the Essence Farmland bonuses are computed in Java
(`MysticalCropBlock.getDrops`). Botany Pots harvests through loot tables only and otherwise falls back
to a single seed, so a plain `block_derived_crop` for these crops yields no essence at all. This data
pack supplies explicit drops and registers the Essence Farmlands as soils.

## Contents

- **136 crops** (`data/botanypots/recipe/mysticalagriculture/crop/*.json`), each a
  `botanypots:block_derived_crop` with explicit `botanypots:items` drops: 1 essence (100 %), 1 seed (10 %)
  and, except for Inferium, 1 Fertilized Essence (10 %). This mirrors the mod's behaviour on Essence
  Farmland with default config (`secondarySeedDrops = true`, `fertilizedEssenceChance = 0.1`). Crops
  whose target mod is missing are skipped by the `bookshelf:block_exists` condition.
- **6 soils** (`…/soil/*_farmland.json`): Inferium … Awakened Supremium Farmland as
  `botanypots:block_derived_soil`. For tier *t*: `growth_modifier = 0.10 + 0.05·t` (vanilla farmland is
  0.10) and `yield_modifier = 0.5·(t − 1)`, the mod's Inferium-on-Essence-Farmland formula (tier 2 ≈ 1.5
  essence on average). Yield only applies in **hopper** pots, which roll the harvest several times.
- **Tag** `botanypots:soil/dirt` extended by the six farmlands, so every crop that uses the default
  soil (dirt or farmland) also accepts them.

## Usage

1. Copy this folder to `<world>/datapacks/botany-mystical` (server: `world/datapacks/…`).
2. `/reload`. Put dirt, farmland or an Essence Farmland into a Botany Pot and add the seeds; the seed
   tooltip shows growth time and yield. JEI lists the recipes under *Uses* of the Botany Pot.

`generate.py` regenerates `data/` from `tiers.json` (crop name → tier/type, taken from the mod's
`ModCrops.java`, branch 26.1). Tested with Mystical Agriculture 9.0.11 on Minecraft 26.1.2 / NeoForge.
