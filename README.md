<p align="center">
  <img src="common/src/main/resources/assets/villagecensus/icon.png" width="96" alt="Village Census icon">
</p>

<h1 align="center">Village Census</h1>

<p align="center">
  A Minecraft mod that adds a single item, the <b>Census Ledger</b>. It tells you what is going on in the village around you.<br>
  Fabric · NeoForge · Forge — Minecraft Java 26.1.2, 26.2 and 26.3
</p>

---

## Why

Iron farms that stop working, villagers that won't breed, librarians that never take their lectern…
In vanilla Minecraft you have to guess why. The Census Ledger shows you what the villagers actually know:
who has a bed, who has a job, who slept recently and who is able to summon an iron golem.

## Features

| Action | What you get |
|---|---|
| **Right-click** | A report in chat about the village within 48 blocks: villagers (adults/children), professions, beds and workstations (total / claimed / free), how many villagers are ready to breed and to summon an iron golem, iron golems nearby, and advice on what to fix. |
| **Sneak + right-click** | Free beds (green sparkles) and free workstations (white sparks) are highlighted for 10 seconds. Only you see them. |
| **Right-click a villager** | A card for that villager: profession and level, coordinates of its bed and workstation, when it last slept, whether it can breed and whether it can summon a golem (and if not, why). The trading screen does not open. |

**Recipe (shapeless):** Book + Emerald + Feather + Ink Sac → Census Ledger.
The ledger is also in the *Tools & Utilities* creative tab.

Install the mod on both the client and the server, because it adds a new item.

### Languages

All text in the mod is translated into **15 languages**:
English, Русский, Українська, Беларуская, Polski, Deutsch, Français, Español, Português (Brasil), Italiano, Türkçe,
简体中文, 繁體中文, 日本語, 한국어.

The translations were written without review by native speakers. Corrections are very welcome, see [CONTRIBUTING.md](CONTRIBUTING.md#translations).

## Download / install

| Minecraft | Fabric | NeoForge | Forge |
|---|---|---|---|
| 26.3   | `villagecensus-fabric-1.0.0+mc26.3.jar` | `villagecensus-neoforge-1.0.0+mc26.3.jar` | `villagecensus-forge-1.0.0+mc26.3.jar` |
| 26.2   | `villagecensus-fabric-1.0.0+mc26.2.jar` | `villagecensus-neoforge-1.0.0+mc26.2.jar` | `villagecensus-forge-1.0.0+mc26.2.jar` |
| 26.1.2 | `villagecensus-fabric-1.0.0+mc26.1.2.jar` | `villagecensus-neoforge-1.0.0+mc26.1.2.jar` | `villagecensus-forge-1.0.0+mc26.1.2.jar` |

* **Fabric** needs [Fabric API](https://modrinth.com/mod/fabric-api).
* Java 25 is required. This is the same version Minecraft 26.x itself needs.
* The 26.1 line is built against 26.1.2, the latest hotfix of 26.1. Please update 26.1/26.1.1 to 26.1.2.

## Building from source

Requirements: nothing except an internet connection. The Gradle wrapper downloads Gradle, and Gradle downloads JDK 25 automatically if you don't have it.

```bash
# all three loaders for the default Minecraft version (26.3)
./gradlew build

# all three loaders for another Minecraft version
./gradlew build -Pmc_target=26.1.2

# every loader for every version -> dist/
./scripts/build-all.sh        # Windows: scripts\build-all.bat
```

The jars are copied to `dist/` (and are also in `<loader>/build/libs/`).

> **First build takes a while.** Each loader sets up its own copy of Minecraft (download, decompile, recompile).
> That is roughly 15–20 minutes per Minecraft version the first time. Later builds take well under a minute.

### Testing

* `./gradlew :common:test` runs the JUnit tests. They cover the golem and hint logic, which is checked against the vanilla
  `Villager` code, the translations (all 15 languages, same keys, same `%s` placeholders) and the data files
  (recipe, advancement, item model and texture), which Minecraft would otherwise only check when a world loads.
* For an in-game check, start a dev client (`./gradlew :fabric:runClient`, `:neoforge:runClient` or `:forge:runClient`, or use the
  IntelliJ run configurations) and use the commands from [docs/TESTING.md](docs/TESTING.md) to set up a test village.

### IntelliJ IDEA

1. *File → Open…* and choose the project folder. Gradle import starts on its own.
2. Ready-made run configurations are in the run configuration drop-down (they are stored in `.run/`):
   * **1. Build:** builds the jars for one Minecraft version, or cleans `dist/`.
   * **2. Test:** unit tests (logic + translation checks).
   * **3. Run MC …:** starts a development client for Fabric, NeoForge or Forge.

### Useful Gradle properties

| Property | Default | Meaning |
|---|---|---|
| `mc_target` | `26.3` | Minecraft version to build against; must match a file in `versions/`. |
| `vc_loaders` | `fabric,neoforge,forge` | Which loader projects to include. Handy for faster iteration, e.g. `-Pvc_loaders=fabric`. |

## Project layout

```
VillageCensus/
├── common/                 ← all gameplay code and every resource (loader-independent)
│   ├── src/main/java/…/villagecensus/
│   │   ├── VillageCensus.java          constants (mod id, radius, timings)
│   │   ├── census/                     scanning the world + plain data/logic
│   │   │   ├── VillageScanner.java     reads villagers, beds, workstations, golems
│   │   │   ├── VillagerFacts.java      what one villager "knows" (bed, job, sleep…)
│   │   │   ├── CensusReport.java       the numbers + advice (pure Java, unit-tested)
│   │   │   └── GolemRules.java         vanilla iron golem rules (pure Java, unit-tested)
│   │   ├── item/CensusLedgerItem.java  the item: right-click, sneak, villager click
│   │   ├── highlight/                  per-player particle highlight
│   │   └── text/                       translation keys + chat formatting
│   ├── src/main/resources/             lang files, texture, model, recipe, advancement
│   └── src/test/java/                  JUnit tests
├── fabric/   neoforge/   forge/        ← only the entry point + registration + one event each
├── versions/<mc>.properties            ← dependency versions per Minecraft version
├── build-logic/                        ← shared Gradle conventions
├── scripts/                            ← build-all, one-time git setup, translation generator
├── ci/github-workflow-build.yml        ← GitHub Actions workflow (installed by scripts/setup-git)
├── docs/TESTING.md                     ← in-game test checklist
└── .run/                               ← IntelliJ run configurations
```

How the build works: `common` is compiled against vanilla Minecraft, using ModDevGradle in NeoForm mode, so the IDE can check it.
Each loader project then compiles the `common` sources again together with its own code and produces **one self-contained jar**.
Read [CONTRIBUTING.md](CONTRIBUTING.md) for how to add a Minecraft version, a language or a feature.

## Putting the project on GitHub

Run `scripts/setup-git.bat` once (or `scripts/setup-git.sh`). It creates the Git repository with a first commit and
installs the CI workflow into `.github/workflows/`. It does not push anything. Then:

```bash
git remote add origin https://github.com/alexkond12/VillageCensus.git
git push -u origin main
```

From then on, every push builds and tests all 9 jars on GitHub Actions and attaches them to the run.

## License

[MIT](LICENSE) © 2026 alexkond12
