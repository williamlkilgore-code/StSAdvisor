# STS Advisor

A native Slay the Spire mod that provides real-time in-game advice for card picks, map paths, and deck synergies — rendered directly on the game's UI using libGDX.

## Features

- **Card reward screen** — tier badge (S/A/B/C/D), base score, synergy bonus, recommendation (TAKE / CONSIDER / SKIP), and synergy reasons drawn above each card
- **Map screen** — path scores on starting nodes showing elite/rest/shop breakdown and a reason for the top recommendation
- **Synergy engine** — scans your current deck and boosts scores for cards that fit your emerging archetype
- **All four characters** supported: Ironclad, Silent, Defect, Watcher
- **Skip-all banner** — warns you when none of the three card reward options fit your deck

## Requirements

- Slay the Spire (Steam)
- [ModTheSpire](https://github.com/kiooeht/ModTheSpire/releases) — mod loader
- [BaseMod](https://steamcommunity.com/sharedfiles/filedetails/?id=1605833019) — required dependency (Steam Workshop)

## Installation

### Option A — Download the prebuilt jar (recommended)

1. Download `STSAdvisorMod.jar` from the [Releases](../../releases) page
2. Place it in your Slay the Spire `mods/` folder:
   - **Windows:** `C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire\mods\`
   - **Linux:** `~/.steam/steam/steamapps/common/SlayTheSpire/mods/`
   - **macOS:** `~/Library/Application Support/Steam/steamapps/common/SlayTheSpire/mods/`
3. Launch Slay the Spire via **Play with Mods** in Steam
4. Enable **BaseMod** and **STS Advisor** in the mod list

### Option B — Build from source

#### Prerequisites

- Java 17+ (for building) — install via [SDKMAN](https://sdkman.io/):
  ```bash
  curl -s "https://get.sdkman.io" | bash
  sdk install java 17.0.11-tem
  sdk install gradle
  ```
- The game compiles targeting Java 8 for compatibility with STS's bundled JRE

#### Build steps

```bash
git clone https://github.com/williamkilgore-code/StSAdvisor
cd STSAdvisorMod
gradle build
```

The built jar will be at `build/libs/STSAdvisorMod-1.0.0.jar`.

Copy it to your mods folder:
```bash
# Linux
cp build/libs/STSAdvisorMod-1.0.0.jar \
   ~/.steam/steam/steamapps/common/SlayTheSpire/mods/
```

#### Project structure

```
src/main/java/sts/advisor/
├── STSAdvisorMod.java      — main class, BaseMod hooks, rendering
├── CardScorer.java         — scores cards using character data + synergy engine
├── SynergyEngine.java      — scans deck, computes synergy bonuses
├── AdvisorResult.java      — holds score, tier, bonus, reasons for one card
├── MapAdvisor.java         — path scoring for the map screen
└── data/
    ├── WatcherData.java    — Watcher card scores, tags, archetype detection
    ├── IroncladData.java
    ├── SilentData.java
    └── DefectData.java
```

## How card scores work

Each card has a **base score** (0–100) sourced from community tier lists, plus a **synergy bonus** (up to +25) calculated by scanning your current deck:

- Cards that share tags with 2+ cards already in your deck get a bonus
- Cards that complete an emerging archetype (e.g. Exhaust Engine, Stance Cycling) get a bonus
- Cards that fill a missing role (no AoE, no draw, no block) get a bonus
- Duplicate cards get a penalty

The final score determines the tier and recommendation:

| Score | Tier | Recommendation |
|-------|------|---------------|
| 85–99 | S    | TAKE          |
| 70–84 | A    | TAKE          |
| 55–69 | B    | CONSIDER      |
| 35–54 | C    | SKIP          |
| 0–34  | D    | SKIP          |

## How map scores work

On the map screen, each starting node shows the score of the **best full path** reachable from that node all the way to the boss. The score weighs:

- **Elites** — good for relics, dangerous at low HP; back-to-back elites with no rest are penalised
- **Fights** — card reward opportunities, weighted more heavily in Act 1
- **Rest sites** — more valuable at low HP and in later acts
- **Shops** — scaled by your current gold
- **Events** — positive in Act 1, increasingly risky in Acts 2–3
- **Act** — priorities shift across acts (Act 1: build deck; Act 2: balance; Act 3: survive)

If you have **Winged Boots**, all nodes on the next floor are shown as options rather than only connected nodes.

## Unknown card logging

If a card shows the default score of 40, its card ID may not be in our data yet. Unknown card IDs are logged to:

```
~/.steam/steam/steamapps/common/SlayTheSpire/sts_advisor_unknown_cards.log
```

Please open an issue or PR with the contents of that file to help expand coverage.

## Contributing

Pull requests welcome — especially:
- Corrected card scores or missing card IDs
- Improved synergy tag coverage
- Support for modded characters

## License

MIT
