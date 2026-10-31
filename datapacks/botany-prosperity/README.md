# botany-prosperity – Prosperity Shards from a Botany Pot (data pack)

[Mystical Agriculture](https://github.com/BlakeBr0/MysticalAgriculture) has no crop for Prosperity Shards, and its
Prosperity Seed Base cannot be planted. In Skyblock worlds without Prosperity Ore that leaves only the sieve. This data pack
adds one Botany Pots crop that makes the shards renewable.

## Contents

- **1 crop** (`data/botanypots/recipe/mysticalagriculture/crop/prosperity_shard.json`): the Prosperity Seed Base is the seed,
  a Prosperity Block grows in the pot and every harvest drops 1 Prosperity Shard.
- Grow time is **2 minutes** (2400 ticks, twice the default), the soil is the default dirt tag. With
  [`botany-mystical`](../botany-mystical) installed the Essence Farmlands work too and speed it up.
- The seed stays in the pot, so one Seed Base (4 shards + wheat seeds) pays for itself after four harvests.
- The recipe is skipped by the `bookshelf:load_conditions` check if Mystical Agriculture is missing.

## Usage

1. Copy this folder to `<world>/datapacks/botany-prosperity` (server: `world/datapacks/…`).
2. `/reload`. Put dirt or farmland into a Botany Pot and add a Prosperity Seed Base. Use a hopper pot to automate it.

Tested with Mystical Agriculture 9.0.11 on Minecraft 26.1.2 / NeoForge.
