# Contributing to Village Census

Thanks for helping! This file explains how the project is organised and how to do the common tasks.

## Ground rules

* **Gameplay code goes into `common/`.** Loader projects (`fabric/`, `neoforge/`, `forge/`) only register the item and forward events to `common`. If you find yourself writing logic in a loader project, it probably belongs in `common`.
* **Every visible text is translatable.** Add a key to `text/LangKeys.java` and to *all* files in `common/src/main/resources/assets/villagecensus/lang/` (or to `scripts/translations.py`, then run it).
* **Data files are shared by every Minecraft version.** Recipes, advancements and models in `common/` are used as-is on 26.1.2, 26.2 and 26.3, so only use JSON formats that are the same in all of them (for example, `minecraft:recipe_unlocked` is not). `DataFilesTest` checks this, but always start one client per version after changing a data file.
* **Run the tests before you open a pull request:** `./gradlew :common:test`, then `./gradlew build` for at least the default Minecraft version.
* Code style: 4 spaces, UTF-8, LF line endings (see `.editorconfig`).

## Architecture in one minute

```
player right-clicks ──► CensusLedgerItem.use()
                           │
                           ├─► VillageScanner.scan()      reads entities + points of interest (server)
                           │      └─► VillagerFacts.of()  per villager: bed, job, sleep, golem memory
                           │      └─► CensusReport        plain numbers + hints()  (pure, unit-tested)
                           │
                           ├─► CensusMessages.report()    turns the report into translated chat lines
                           └─► HighlightManager.start()   (when sneaking) particles for one player,
                                                          ticked by CensusLedgerItem.inventoryTick()

player right-clicks a villager ──► loader event (Fabric UseEntityCallback / NeoForge & Forge
                                   PlayerInteractEvent) ──► CensusLedgerItem.useOnVillager()
```

The villager click goes through a loader event, because vanilla would otherwise open the trading screen
before the item gets a chance to react.

## Adding a new Minecraft version

1. Copy `versions/26.3.properties` to `versions/<new>.properties` and update every version in it
   (links to the version lists are in the file).
2. Build it: `./gradlew build -Pmc_target=<new>` and fix compile errors in `common/` first.
3. Add the version to `scripts/build-all.*`, both workflows (`.github/workflows/build.yml` and `release.yml`), `scripts/release-notes.sh` and the table in `README.md`.
4. Optionally add IntelliJ run configurations in `.run/` (copy an existing file and change `-Pmc_target`).
5. Change the default `mc_target` in `gradle.properties` if the new version should become the default.

## Translations

* Language files: `common/src/main/resources/assets/villagecensus/lang/<locale>.json`.
* `scripts/translations.py` holds all translations in one place and regenerates the JSON files
  (`python scripts/translations.py`). You may also edit the JSON directly. Just keep both in sync.
* Keep every `%s` placeholder. They are filled with numbers or names, in the same order as in English.
* `LangFilesTest` fails if a language misses a key, has an extra key or a different number of placeholders.

To add a language: add a new block to `scripts/translations.py`, run it and add the locale to the
`REQUIRED` set in `LangFilesTest` and to the list in `README.md`.

## Releasing

1. Bump `mod_version` in `gradle.properties` and add a `## [x.y.z] - date` section to `CHANGELOG.md`.
2. Build and test locally: `scripts/build-all.bat` (Windows) or `scripts/build-all.sh`, then try at least one jar per
   loader in a real launcher.
3. Commit, then tag and push:
   ```bash
   git tag -a v1.0.1 -m "Village Census 1.0.1"
   git push origin main v1.0.1
   ```
   The **Release** workflow checks that the tag matches `mod_version`, builds all 9 jars and publishes the GitHub
   release with the notes from `CHANGELOG.md` (preview them with `scripts/release-notes.sh 1.0.1`).
