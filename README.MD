# PSword

A Paper plugin that gives swords their own **effects** and **abilities**. Effects are visual: particles, sounds and
lightning that fire on combat triggers such as critical hits and kills. Abilities change the fight: potion effects on
you, your target or everyone around you, and lightning bolts that deal extra damage, each with its own cooldown.
Swords are defined in `config.yml` and handed out with a command.

> **Status:** stable (v2.2.0). Please report bugs on GitHub.

## Requirements

- Paper **1.21.4**
- Java **21**
- [ParticleEngineLibrary](https://github.com/puckypucky0/ParticleEngineLibrary/releases) **1.1.0** or newer
  (required since v2.2.0, PSword does not load without it)

## Installation

1. Download the PSword jar (or build it, see below) and the ParticleEngineLibrary jar, and put both in your server's
   `plugins/` folder.
2. Start the server once to generate `plugins/PSword/config.yml`.
3. Define your swords in the config and run `/psword reload`.

> Updating from v1? `config.yml` is not overwritten on update. Add `abilitiesFireOnPlayersOnly` to `settings` and an
> `abilities` list to your swords yourself, or delete the old config to get the new default one.
>
> Updating from v2.1? Your config keeps working. `dust_data` and `radius_highlight_particle` (see below) are new and
> optional. Without `radius_highlight_particle`, radius abilities show a green / red ring by default.

## Commands & Permissions

| Command | Description | Permission |
|---|---|---|
| `/psword giveSword <id>` | Gives you the sword with the given id | `psword.givesword` |
| `/psword reload` | Reloads the configuration and resets all cooldowns | `psword.reload` |

`/ps` is an alias for `/psword`. Using the root command requires `psword.use`.

## Default Configuration

The default config contains one example sword per effect and ability type, plus a showcase sword that combines
them. Give any of them with `/psword giveSword <id>` to see what it does.
```yaml
title: "<b><gradient:#BB5444:#214452>PSWORD</gradient></b> <dark_gray> >"

# Change these settings and run /ps reload to see the effects in-game
settings:
  particleOffsetX: 0.6
  particleOffsetY: 0.3
  particleOffsetZ: 0.6
  # How much should particles spill in X, Y and Z angles.

  particleOffsetExtra: 0.2
  # How speedy should particles be in every angle.

  effectShowOnPlayersOnly: true
  abilitiesFireOnPlayersOnly: true
  # In its name.

  rightClickIgnoreInteractables: true
  # If true, a player with sword that has a right click trigger ability won't be able to use
  # their ability if they right-click an interactive block such as chests, buttons or doors etc.

# Every sword below shows ONE effect or ability so you can see what each one does.
# Give one with /ps giveSword <id>, then copy the parts you like into your own swords.
swords:

  # ===================== EFFECTS (visual only) =====================

  "particle_sword":
    name: "<aqua>Particle Effect"
    lore:
      - "<gray>Effect: <white>PARTICLE"
      - "<gray>Releases souls on every hit."
    material: IRON_SWORD
    effects:
      - type: PARTICLE
        trigger: ON_DAMAGE_DEAL
        particle_type: soul       # https://minecraft.wiki/w/Particles
        multiplier: 1.0           # 1.0 = 25 particles

  "dust_sword":
    name: "<light_purple>Dust Particle Effect"
    lore:
      - "<gray>Effect: <white>PARTICLE (dust)"
      - "<gray>Colored dust on every hit."
    material: IRON_SWORD
    effects:
      - type: PARTICLE
        trigger: ON_DAMAGE_DEAL
        particle_type: dust
        dust_data:
          color: "#B9A7F0"        # hex color, always in quotes
          scale: 1.5              # particle size, max 4.0
        multiplier: 1.0

  "sound_sword":
    name: "<gold>Sound Effect"
    lore:
      - "<gray>Effect: <white>SOUND"
      - "<gray>Rings on critical hits."
    material: GOLDEN_SWORD
    effects:
      - type: SOUND
        trigger: ON_HIT_CRIT
        sound: block.trial_spawner.eject_item   # https://minecraft.wiki/w/Sounds.json/Java_Edition_values
        volume: 1.0
        pitch: 0.5

  "lightning_sword":
    name: "<yellow>Lightning Effect"
    lore:
      - "<gray>Effect: <white>LIGHTNING"
      - "<gray>Lightning strikes where your victim falls."
      - "<dark_gray>Visual only, deals no damage."
    material: DIAMOND_SWORD
    effects:
      - type: LIGHTNING           # no options, only a trigger
        trigger: ON_KILL

  # ===================== ABILITIES (gameplay) =====================
  # Every ability needs: type, name (shown on the action bar while on cooldown), trigger and cooldown_millis.
  # Potion abilities: effect_type (https://minecraft.wiki/w/Effect), duration_seconds, level (1 = I, 2 = II ...)

  "self_effect_sword":
    name: "<green>Speed Ability"
    lore:
      - "<gray>Ability: <white>EFFECT_SELF"
      - "<gray>Critical hits give you Speed II for 5s."
      - "<dark_gray>Cooldown: 10s"
    material: IRON_SWORD
    abilities:
      - type: EFFECT_SELF
        name: "<green>Speed"
        trigger: ON_HIT_CRIT
        cooldown_millis: 10000
        effect_type: speed
        duration_seconds: 5
        level: 2

  "single_effect_sword":
    name: "<dark_green>Poison Ability"
    lore:
      - "<gray>Ability: <white>EFFECT_SINGLE"
      - "<gray>Poisons the target for 4s."
      - "<dark_gray>Cooldown: 8s"
    material: STONE_SWORD
    abilities:
      - type: EFFECT_SINGLE
        name: "<dark_green>Poison"
        trigger: ON_DAMAGE_DEAL
        cooldown_millis: 8000
        effect_type: poison
        duration_seconds: 4
        level: 1

  "radius_effect_sword":
    name: "<dark_aqua>Levitation Ability"
    lore:
      - "<gray>Ability: <white>EFFECT_RADIUS"
      - "<gray>Critical hits lift everyone within 5 blocks"
      - "<gray>into the air for 2s (not you)."
      - "<dark_gray>Cooldown: 20s"
    material: DIAMOND_SWORD
    abilities:
      - type: EFFECT_RADIUS
        name: "<aqua>Levitation"
        trigger: ON_HIT_CRIT
        cooldown_millis: 20000
        effect_type: levitation
        duration_seconds: 2
        level: 1
        radius: 5                 # blocks
        radius_highlight_particle: # A ring of particles shows the radius when the ability is used.
          successful-ability-highlight-enabled: true # Ability lands on min. 1 entity / player
          failed-ability-highlight-enabled: true # Ability lands on no one (only possible with RIGHT_CLICK).
          successful-colors: # Successful particle gradient colors
            - "#A7E8F0"
            - "#5BA4CF"
          fail-colors: # Fail particle gradient colors
            - "#4d0005"
            - "#860005"

  "lightning_bolt_sword":
    name: "<yellow>Lightning Ability"
    lore:
      - "<gray>Ability: <white>LIGHTNING_BOLT"
      - "<gray>Critical hits call down lightning"
      - "<gray>for 2 extra hearts of damage."
      - "<dark_gray>Cooldown: 15s"
    material: NETHERITE_SWORD
    abilities:
      - type: LIGHTNING_BOLT
        name: "<yellow>⚡ Lightning"
        trigger: ON_HIT_CRIT
        cooldown_millis: 15000
        lightning_damage: 4.0     # 1 heart = 2 damage

  "lightning_bolt_radius_sword":
    name: "<yellow>Radius Lightning Ability"
    lore:
      - "<gray>Ability: <white>LIGHTNING_BOLT_RADIUS"
      - "<gray>Right click calls down lightning"
      - "<gray>for 2 extra hearts of damage"
      - "<gray>to everyone around you with a radius of 3 blocks"
      - "<dark_gray>Cooldown: 15s"
    material: NETHERITE_SWORD
    abilities:
      - type: LIGHTNING_BOLT_RADIUS
        name: "<yellow>⚡ Lightning"
        trigger: RIGHT_CLICK
        radius_highlight_particle: # Some circle-shaped particles will appear when some conditions happen.
          successful-ability-highlight-enabled: true # Ability lands on min. 1 entity / player
          failed-ability-highlight-enabled: true # Ability lands on no one.
          successful-colors: # Successful particle gradient colors
            - "#F2FBFF"
            - "#A7E8F0"
            - "#8FB8F2"
            - "#B9A7F0"
          fail-colors: # Fail particle gradient colors
            - "#4d0005"
            - "#700005"
            - "#860005"
        cooldown_millis: 15000
        lightning_damage: 4.0     # 1 heart = 2 damage
        radius: 3

  # ===================== EVERYTHING TOGETHER =====================

  "showcase_sword":
    name: "<b><gradient:#5BC0EB:#2E4057>Showcase Sword"
    lore:
      - "<gray>Effects and abilities on one sword."
      - ""
      - "<gray>Hit: <white>soul particles"
      - "<gray>Critical hit: <white>blue flames, thunder sound,"
      - "<white>  Strength I for you"
      - "<gray>Kill: <white>lightning + level up sound"
      - "<gray>Hit by a player: <white>they start glowing"
    material: NETHERITE_SWORD
    effects:
      - type: PARTICLE
        trigger: ON_DAMAGE_DEAL
        particle_type: soul
        multiplier: 0.6
      - type: PARTICLE
        trigger: ON_HIT_CRIT
        particle_type: soul_fire_flame
        multiplier: 1.5
      - type: SOUND
        trigger: ON_HIT_CRIT
        sound: item.trident.thunder
        volume: 0.7
        pitch: 1.2
      - type: LIGHTNING
        trigger: ON_KILL
      - type: SOUND
        trigger: ON_KILL
        sound: entity.player.levelup
        volume: 1.0
        pitch: 1.0
    abilities:
      - type: EFFECT_SELF
        name: "<red>Strength"
        trigger: ON_HIT_CRIT
        cooldown_millis: 12000
        effect_type: strength
        duration_seconds: 5
        level: 1
      - type: EFFECT_SINGLE
        name: "<white>Glowing"
        trigger: ON_DAMAGE_RECEIVE  # the target is the player who hit you
        cooldown_millis: 10000
        effect_type: glowing
        duration_seconds: 5
        level: 1
```

### Effect types

| Type | Options |
|---|---|
| `PARTICLE` | `particle_type` ([list](https://minecraft.wiki/w/Particles)), `multiplier` (particle amount). `dust` also takes `dust_data` (see below). Other particles that need extra data (`block`, `item`) are not supported yet. |
| `SOUND` | `sound` ([list](https://minecraft.wiki/w/Sounds.json/Java_Edition_values)), `volume`, `pitch` |
| `LIGHTNING` | none, visual only (deals no damage) |

`dust_data` for `particle_type: dust`:

| Option  | Description                                                                         |
|---------|-------------------------------------------------------------------------------------|
| `color` | Hex color such as `"#B9A7F0"`. Always quote it, an unquoted `#` starts a YAML comment. Default `#f0f0f0`. |
| `scale` | Particle size, up to `4.0`. Default `2.0`.                                           |

### Ability types

| Type                    | What it does                                                                        | Options                                                                    |
|-------------------------|-------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| `EFFECT_SELF`           | Gives **you** a potion effect                                                       | `effect_type`, `duration_seconds`, `level`                                 |
| `EFFECT_SINGLE`         | Gives **the target** a potion effect                                                | `effect_type`, `duration_seconds`, `level`                                 |
| `EFFECT_RADIUS`         | Gives **everyone around you** (except you) a potion effect                          | `effect_type`, `duration_seconds`, `level`, `radius` (blocks, default `5`) |
| `LIGHTNING_BOLT`        | Strikes the target with lightning that deals extra damage                           | `lightning_damage` (1 heart = 2 damage)                                    |
| `LIGHTNING_BOLT_RADIUS` | Strikes **everyone around you** (except you) with lightning that deals extra damage | `lightning_damage` (1 heart = 2 damage), `radius` (blocks, default `3`)    |

Every ability also needs:

| Option            | Description                                                                                                                            |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `name`            | **Required.** Shown on the action bar while the ability is on cooldown. Supports MiniMessage. Abilities without a name are not loaded. |
| `trigger`         | When the ability fires (see [Triggers](#triggers)).                                                                                    |
| `cooldown_millis` | Cooldown in milliseconds (`1500` = 1.5 seconds). Each ability has its own cooldown per player.                                         |

Potion options:

- `effect_type`: a potion effect key such as `speed`, `strength`, `poison` or `slowness`
  ([list](https://minecraft.wiki/w/Effect)).
- `duration_seconds`: how long the effect lasts.
- `level`: `1` = level I, `2` = level II, and so on.

Radius highlight (`EFFECT_RADIUS` and `LIGHTNING_BOLT_RADIUS`): a ring of particles shows the radius when the ability
is used. Set it under `radius_highlight_particle`:

| Option                                 | Description                                                                 |
|----------------------------------------|-----------------------------------------------------------------------------|
| `successful-ability-highlight-enabled` | Show the ring when the ability hits at least one entity. Default `true`.    |
| `failed-ability-highlight-enabled`     | Show the ring when nobody is in range (only possible with `RIGHT_CLICK`). Default `true`. |
| `successful-colors`                    | List of hex colors, blended as a gradient. Default `["#13eb4c"]`.           |
| `fail-colors`                          | List of hex colors, blended as a gradient. Default `["#870000"]`.           |

While at least one ability of the held sword is on cooldown, the action bar shows every running cooldown with its name
and the remaining seconds. When a cooldown ends, a short subtitle and a sound tell you the ability is ready again.

### Triggers

| Trigger             | Fires when the sword holder…      |
|---------------------|-----------------------------------|
| `ON_DAMAGE_DEAL`    | hits an entity                    |
| `ON_HIT_CRIT`       | lands a critical hit              |
| `ON_KILL`           | kills an entity                   |
| `ON_DAMAGE_RECEIVE` | is hit by a player                |
| `RIGHT_CLICK`       | when right click (only abilities) |

When `effectShowOnPlayersOnly` / `abilitiesFireOnPlayersOnly` is `true`, `ON_DAMAGE_DEAL`, `ON_HIT_CRIT` and `ON_KILL`
only fire against players.

For `ON_DAMAGE_RECEIVE`, "the target" of an ability is the player who hit you. With `ON_KILL` the target is already
dead, so use `EFFECT_SELF` or `EFFECT_RADIUS` there.

Invalid values in the config are logged to the console and replaced with a default (or the effect / ability is
skipped) instead of crashing the plugin.

## Building

```bash
mvn clean package
```

The jar is created in `target/`. ParticleEngineLibrary is pulled from JitPack automatically.

## License

Released under the [MIT License](LICENSE).
