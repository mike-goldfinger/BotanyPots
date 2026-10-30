#!/usr/bin/env python3
"""Regenerates data/ from tiers.json (crop name -> [tier, type], taken from Mystical Agriculture's ModCrops.java)."""
import json, os, shutil
S = os.path.dirname(os.path.abspath(__file__)); OUT = S
tiers = json.load(open(os.path.join(S, "tiers.json")))  # name -> (TIER, TYPE)
for sub in ("data",): shutil.rmtree(os.path.join(OUT, sub), ignore_errors=True)
crop_dir = os.path.join(OUT, "data/botanypots/recipe/mysticalagriculture/crop")
soil_dir = os.path.join(OUT, "data/botanypots/recipe/mysticalagriculture/soil")
tag_dir  = os.path.join(OUT, "data/botanypots/tags/item/soil")
for d in (crop_dir, soil_dir, tag_dir): os.makedirs(d)
def w(path, obj): json.dump(obj, open(path, "w"), indent=2, ensure_ascii=False)

farmlands = ["inferium", "prudentium", "tertium", "imperium", "supremium", "awakened_supremium"]
for tier, name in enumerate(farmlands, start=1):
    block = f"mysticalagriculture:{name}_farmland"
    w(os.path.join(soil_dir, f"{name}_farmland.json"), {
        "bookshelf:load_conditions": [{"type": "bookshelf:block_exists", "values": [block]}],
        "type": "botanypots:block_derived_soil", "block": block,
        "growth_modifier": round(0.10 + 0.05 * tier, 2),
        "yield_modifier": round(0.5 * (tier - 1), 2)})
w(os.path.join(tag_dir, "dirt.json"), {"replace": False,
  "values": [{"id": f"mysticalagriculture:{n}_farmland", "required": False} for n in farmlands]})

n = 0
for name, (tier, ctype) in sorted(tiers.items()):
    block = f"mysticalagriculture:{name}_crop"
    items = [{"result": {"id": f"mysticalagriculture:{name}_essence", "count": 1}},
             {"result": {"id": f"mysticalagriculture:{name}_seeds", "count": 1}, "chance": 0.1}]
    if name != "inferium":
        items.append({"result": {"id": "mysticalagriculture:fertilized_essence", "count": 1}, "chance": 0.1})
    w(os.path.join(crop_dir, f"{name}.json"), {
        "bookshelf:load_conditions": [{"type": "bookshelf:block_exists", "values": [block]}],
        "type": "botanypots:block_derived_crop", "block": block,
        "drops": [{"type": "botanypots:items", "items": items}]})
    n += 1
w(os.path.join(OUT, "pack.mcmeta"), {"pack": {
    "description": "Mystical Agriculture crops in Botany Pots: essence drops and Essence Farmland soils (data from the mod's getDrops logic)",
    "min_format": 101, "max_format": 101}})
print("crops", n, "soils", len(farmlands))
