# botany-trees – grow trees in Botany Pots (data pack)

[Botany Trees](https://github.com/Darkhax-Minecraft/BotanyTrees) by Darkhax is the add-on that lets
Botany Pots grow trees. It has no release for Minecraft 26.1, but the mod is essentially data only
(two empty mod classes plus JSON recipes and loot tables), and the `botanypots:block_derived_crop`
recipe format is unchanged in this 26.1 port. So the data can be used directly as a **data pack** –
no client download needed, works on dedicated servers and in single-player worlds.

This folder contains the vanilla trees from the Botany Trees `1.21.1` branch, unmodified except for
`pack.mcmeta` and the added pale oak (`pale_oak`, copied from oak without the apple drop):
oak, spruce, birch, jungle, acacia, dark oak, cherry, mangrove, pale oak.
License: [LGPL-2.1](../../LICENSE), same as Botany Trees and Botany Pots.

## Usage

1. Copy this folder to `<world>/datapacks/botany-trees` (server: `world/datapacks/…`).
2. Run `/reload` (or re-enter the world). The server log should report nine more recipes.
3. Put a sapling into a Botany Pot on dirt (the soil is derived from the sapling block). JEI lists
   the crops under *Uses* of the Botany Pot – the items themselves stay vanilla saplings, so the
   `@Botany` mod filter will not show them.

Growth time is 2400 ticks (2 minutes). Each harvest drops 1–4 logs, a 5 % chance of a sapling and,
for oak and dark oak, a 5 % chance of an apple. Shears in the harvest-tool slot additionally drop
1–3 leaves. A hopper pot on a chest makes a fully automatic wood farm.

## Adding more trees

One recipe file (`data/botanytrees/recipe/<mod>/<tree>.json`) plus one loot table
(`data/botanytrees/loot_table/tree_drops/<mod>/<tree>.json`) per tree. Trees from other mods can be
taken from the upstream Botany Trees repository the same way; the `bookshelf:block_exists` load
condition skips files whose mod is not installed.
