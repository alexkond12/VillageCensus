# Manual test checklist

This is the in-game checklist used before every release. Run it on each loader (Fabric, NeoForge, Forge)
and on each supported Minecraft version.

## 1. Start a dev client

IntelliJ: pick **3. Run MC <version> → <Loader> Client** in the run configuration drop-down and press Run.
Command line: `./gradlew :fabric:runClient -Pmc_target=26.3 -Pvc_loaders=fabric` (use `neoforge` or `forge` for the others).

Create a world: *Creative*, *Superflat*, *Generate Structures: OFF*.

## 2. Build a small test village

Paste these commands into chat one by one:

```mcfunction
/difficulty peaceful
/give @s villagecensus:census_ledger
/setblock ~4 ~ ~3 minecraft:red_bed[facing=east,part=foot]
/setblock ~5 ~ ~3 minecraft:red_bed[facing=east,part=head]
/setblock ~4 ~ ~5 minecraft:blue_bed[facing=east,part=foot]
/setblock ~5 ~ ~5 minecraft:blue_bed[facing=east,part=head]
/setblock ~-4 ~ ~3 minecraft:lectern
/setblock ~-4 ~ ~6 minecraft:composter
/summon villager ~ ~ ~5
/summon villager ~1 ~ ~5
/summon villager ~-1 ~ ~5 {VillagerData:{profession:"minecraft:nitwit",level:1,type:"minecraft:plains"}}
/summon villager ~ ~ ~6 {Age:-24000}
/summon iron_golem ~8 ~ ~
```

Wait about 15 seconds so the villagers can claim beds and workstations.

## 3. Checks

| # | Action | Expected |
|---|---|---|
| 1 | Right-click with the ledger | Report with 4 villagers (3 adults, 1 child), professions, beds/workstations with claimed/free counts, 1 iron golem, and yellow hints. |
| 2 | Point at a villager and right-click it | Villager card (profession and level, bed and workstation coordinates, last sleep, breeding, golem status). **No trading screen.** `/data merge entity @e[type=villager,limit=1,sort=nearest] {NoAI:1b}` keeps a villager still while you aim. |
| 3 | Right-click a villager with an **empty hand** | The normal trading screen opens, so the mod does not get in the way. |
| 4 | Sneak + right-click | Chat shows the free bed/workstation counts; free beds sparkle green and free workstations white for 10 s. Tip: *Options → Controls → Sneak: Toggle* makes this easier to test. |
| 5 | Hover the ledger in the inventory | Three tooltip lines. The ledger is in the *Tools & Utilities* creative tab. |
| 6 | `/recipe take @s villagecensus:census_ledger`, then `/give @s minecraft:emerald` | A "New recipes unlocked" toast appears (the recipe advancement works). Book + emerald + feather + ink sac crafts the ledger. |
| 7 | `/kill @e[type=iron_golem]`, `/time set 13000`, wait ~30 s, `/time set 1000`, right-click | "Ready to summon an iron golem" shows the villagers that slept; with 3+ ready the green golem hint appears. |
| 8 | *Options → Language* → Русский / Українська / 简体中文, repeat 1 and 2 | Every line is translated, and profession names come from vanilla. |
| 9 | Check `runs/client/logs/latest.log` | No errors or warnings that mention `villagecensus`, and no `Couldn't parse data file` lines. (Errors about `authlib`/`401` are normal: the dev client is not logged in.) |
| 10 | `/setblock ~-4 ~ ~9 minecraft:smithing_table`, then right-click the jobless villager **right away** | Workstation shows *claimed, on the way* while it walks to the new block, then its coordinates. The report does not count it as jobless. |
