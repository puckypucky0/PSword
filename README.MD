# PSword

A Paper plugin that gives swords their own **effects**: particles, sounds and lightning that fire on
combat triggers such as critical hits and kills. Swords are defined in `config.yml` and handed out with a
command.

> **Status:** early development (v1.0.0). Abilities are planned for v2.

## Requirements

- Paper **1.21.4**
- Java **21**

## Installation

1. Download the jar (or build it, see below) and put it in your server's `plugins/` folder.
2. Start the server once to generate `plugins/PSword/config.yml`.
3. Define your swords in the config and run `/psword reload`.

## Commands & Permissions

| Command | Description | Permission |
|---|---|---|
| `/psword giveSword <id>` | Gives you the sword with the given id | `psword.givesword` |
| `/psword reload` | Reloads the configuration | `psword.reload` |

`/ps` is an alias for `/psword`. Using the root command requires `psword.use`.

## Default Configuration

```yaml
title: "<b><gradient:#BB5444:#214452>PSWORD</gradient></b> <dark_gray> >"

settings:
  particleOffsetX: 0.6
  particleOffsetY: 0.3
  particleOffsetZ: 0.6
  # How much should particles spill in X, Y and Z angles.

  particleOffsetExtra: 0.2
  # How speedy should particles be in every angle.

  # Change these settings and run /ps reload to see the effects in-game
  effectShowOnPlayersOnly: true

swords:
  "test_sword":
    name: "<b><rainbow>TEST SWORD"
    lore:
      - "<red>Red"
      - "<green>Green"
      - "<blue>Blue"
    material: DIAMOND_SWORD
    effects:
      # Particles must be this type: https://minecraft.wiki/w/Particles
      # Trigger should be one of these: ON_HIT_CRIT, ON_KILL, ON_DAMAGE_DEAL, ON_DAMAGE_RECEIVE
      - type: PARTICLE
        trigger: ON_HIT_CRIT
        particle_type: soul
        multiplier: 1.0

      # LIGHTNING effect type doesn't have any options, only needs a trigger just like any other effect type.
      - type: LIGHTNING
        trigger: ON_KILL

      # Sounds must be this type: https://minecraft.wiki/w/Sounds.json/Java_Edition_values
      - type: SOUND
        trigger: ON_HIT_CRIT
        sound: block.trial_spawner.eject_item
        volume: 1.0
        pitch: 0.5
```

### Effect types

| Type | Options |
|---|---|
| `PARTICLE` | `particle_type` ([list](https://minecraft.wiki/w/Particles)), `multiplier` (particle amount). Particles that need extra data (`dust`, `block`, `item`) are not supported yet. |
| `SOUND` | `sound` ([list](https://minecraft.wiki/w/Sounds.json/Java_Edition_values)), `volume`, `pitch` |
| `LIGHTNING` | none, visual only (deals no damage) |

### Triggers

| Trigger | Fires when the sword holder… |
|---|---|
| `ON_DAMAGE_DEAL` | hits an entity |
| `ON_HIT_CRIT` | lands a critical hit |
| `ON_KILL` | kills an entity |
| `ON_DAMAGE_RECEIVE` | is hit by a player |

When `effectShowOnPlayersOnly` is `true`, `ON_DAMAGE_DEAL`, `ON_HIT_CRIT` and `ON_KILL` only fire against players.

Invalid values in the config are logged to the console and replaced with a default (or the effect is skipped)
instead of crashing the plugin.

## Building

```bash
mvn clean package
```

The jar is created in `target/`.

## License

Released under the [MIT License](LICENSE).
