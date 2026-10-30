package dev.pucky.config;

import dev.pucky.particle.Gradient;
import dev.pucky.sword.PSword;
import dev.pucky.sword.Trigger;
import dev.pucky.sword.ability.SwordAbility;
import dev.pucky.sword.ability.SwordAbilityType;
import dev.pucky.sword.effect.SwordEffect;
import dev.pucky.sword.effect.SwordEffectType;
import dev.pucky.util.CheckUtil;
import dev.pucky.util.MMUtil;
import dev.pucky.util.logs.Logger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Getter
public final class ConfigManager {

    private final JavaPlugin plugin;

    private final Map<String, Sword> swords = new LinkedHashMap<>();

    private double particleOffsetX = 0.0;
    private double particleOffsetY = 0.0;
    private double particleOffsetZ = 0.0;
    private double particleOffsetExtra = 0.0;
    private boolean effectShowOnPlayersOnly = true;
    private boolean abilitiesFireOnPlayersOnly = true;
    private boolean rightClickIgnoreInteractables = true;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        particleOffsetX = config.getDouble("settings.particleOffsetX", 0.3);
        particleOffsetY = config.getDouble("settings.particleOffsetY", 0.8);
        particleOffsetZ = config.getDouble("settings.particleOffsetZ", 0.3);
        particleOffsetExtra = config.getDouble("settings.particleOffsetExtra", 0.02);
        effectShowOnPlayersOnly = config.getBoolean("settings.effectShowOnPlayersOnly", true);
        abilitiesFireOnPlayersOnly = config.getBoolean("settings.abilitiesFireOnPlayersOnly", true);
        rightClickIgnoreInteractables = config.getBoolean("settings.rightClickIgnoreInteractables", true);

        swords.clear();
        loadSwords();

        Logger.inform(swords.size() + " kılıç yüklendi.");
    }

    private void loadSwords() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("swords");
        if (section == null) return;

        for (String swordID : section.getKeys(false)) {
            ConfigurationSection perSwordSection = section.getConfigurationSection(swordID);
            if (perSwordSection == null) continue;

            Component swordName = MMUtil.deserialize(perSwordSection.getString("name", ""));
            List<Component> swordLore = MMUtil.deserialize(perSwordSection.getStringList("lore"));

            String swordMaterialString = perSwordSection.getString("material");
            if (swordMaterialString == null) {
                warn("<yellow>Sword material isn't set for sword " + swordID + ". Defaulting to IRON_SWORD.");
                swordMaterialString = "IRON_SWORD";
            }

            Material swordMaterial = Material.matchMaterial(swordMaterialString);
            if (swordMaterial == null || !swordMaterial.isItem()) {
                warn("<yellow>Provided sword material (" + swordMaterialString + ") for sword ID " + swordID + " is <red>invalid or AIR. Will use IRON_SWORD for default.");
                swordMaterial = Material.IRON_SWORD;
            }

            swords.put(swordID, new Sword(
                    PSword.builder()
                            .swordID(swordID)
                            .name(swordName)
                            .lore(swordLore)
                            .swordEffects(this.loadEffects(swordID, perSwordSection))
                            .swordAbilities(this.loadAbilities(swordID, perSwordSection))
                            .itemType(swordMaterial)
                            .build()
            ));
        }
    }

    private Set<SwordEffect> loadEffects(String swordID, ConfigurationSection section) {
        Set<SwordEffect> effects = new HashSet<>();
        List<Map<?, ?>> list = section.getMapList("effects");

        for (Map<?, ?> swordEffectMap : list) {
            SwordEffectType effectType = CheckUtil.validSwordEffectType(swordID, swordEffectMap.get("type"));
            if (effectType == null)
                continue;

            Trigger effectTrigger = CheckUtil.validTriggerType(swordEffectMap.get("trigger"), swordID);
            if (effectTrigger == null)
                continue;

            if (effectTrigger == Trigger.RIGHT_CLICK) {
                Logger.fatal("<dark_red>You can't use RIGHT_CLICK trigger with a sword effect! - " + swordID);
                continue;
            }

            SwordEffect.SwordEffectBuilder effectBuilder = SwordEffect.builder()
                    .effectType(effectType)
                    .effectTrigger(effectTrigger);

            switch (effectType) {
                case LIGHTNING -> {
                    // Since lightning effect has no options, it will be skipped.
                }
                case SOUND -> {
                    effectBuilder = effectBuilder.sound(
                            CheckUtil.soundVal(swordID, swordEffectMap, "entity.player.levelup")
                    );

                    effectBuilder = effectBuilder.volume(
                            CheckUtil.doubleVal(swordID, "Sword effect volume value", swordEffectMap.get("volume"), 1.0)
                    );

                    effectBuilder = effectBuilder.pitch(
                            CheckUtil.doubleVal(swordID, "Sword effect pitch value", swordEffectMap.get("pitch"), 1.0)
                    );
                }
                case PARTICLE -> {
                    Particle particle = CheckUtil.particleVal(swordID, swordEffectMap, "flame");
                    effectBuilder = effectBuilder.particleType(particle);
                    effectBuilder = effectBuilder.particleMultiplier(
                            CheckUtil.doubleVal(swordID, "Sword effect particle multiplier value", swordEffectMap.get("multiplier"), 1.0)
                    );

                    if (particle == Particle.DUST) {
                        Logger.inform("<green>Parameterized particle <dark_green>DUST <green>detected (new in v2.2.0), trying to load it - " + swordID);

                        Map<?, ?> dustDataMap = CheckUtil.mapVal(swordID, "Sword effect particle - dust options", swordEffectMap.get("dust_data"));
                        String hex = CheckUtil.stringVal(swordID, "Sword effect particle - dust color", dustDataMap.get("color"), "#f0f0f0");
                        Color color;
                        float scale = (float) CheckUtil.doubleVal(swordID, "Sword effect particle - dust scale", dustDataMap.get("scale"), 2.0);

                        if (Gradient.isValidHex(hex))
                            color = Gradient.fromHex(hex);
                        else {
                            Logger.fatal("<red>Invalid hex on dust color. Defaulting to \"#f0f0f0\" - " + swordID);
                            color = Gradient.fromHex("#f0f0f0");
                        }

                        Particle.DustOptions dustOptions = new Particle.DustOptions(color, scale);

                        effectBuilder.dustOptions(dustOptions);
                    }
                }
            }

            effects.add(effectBuilder.build());
        }
        return effects;
    }

    private Set<SwordAbility> loadAbilities(String swordID, ConfigurationSection section) {
        Set<SwordAbility> abilities = new HashSet<>();
        List<Map<?, ?>> list = section.getMapList("abilities");

        for (Map<?, ?> swordAbilityMap : list) {
            SwordAbilityType abilityType = CheckUtil.validSwordAbilityType(swordID, swordAbilityMap.get("type"));
            if (abilityType == null)
                continue;

            Trigger abilityTrigger = CheckUtil.validTriggerType(swordAbilityMap.get("trigger"), swordID);
            if (abilityTrigger == null)
                continue;

            int abilityCooldown = CheckUtil.intVal(swordID, "Sword ability - cooldown", swordAbilityMap.get("cooldown_millis"), 15 * 1000);

            String abilityName = CheckUtil.stringVal("Sword ability - name", swordAbilityMap.get("name"));
            if (abilityName == null)
                continue;

            SwordAbility.SwordAbilityBuilder abilityBuilder = SwordAbility.builder()
                    .abilityName(MMUtil.deserialize(abilityName))
                    .abilityType(abilityType)
                    .abilityTrigger(abilityTrigger)
                    .cooldownMillis(abilityCooldown);

            SwordAbility ability;

            switch (abilityType) {
                case EFFECT_SINGLE, EFFECT_RADIUS -> {
                    abilityBuilder = abilityBuilder.enemyEffect(
                            CheckUtil.validPotionEffect(swordID, swordAbilityMap)
                    );

                    abilityBuilder.enemyEffectRadius(
                        abilityType == SwordAbilityType.EFFECT_RADIUS ?
                                CheckUtil.intVal(swordID, "Sword ability - effect radius value", swordAbilityMap.get("radius"), 5) :
                                null
                    );

                    if (abilityType == SwordAbilityType.EFFECT_RADIUS) {
                        Map<?, ?> map = CheckUtil.mapVal(swordID, "Sword ability - radius hightlight map", swordAbilityMap.get("radius_highlight_particle"));

                        abilityBuilder = abilityBuilder.successRadiusHighlightEnabled(
                                CheckUtil.booleanVal(swordID, "Sword ability - successful ability radius hightlight enabled value", map.get("successful-ability-highlight-enabled"), true)
                        );

                        abilityBuilder = abilityBuilder.successRadiusHighlightColors(
                                CheckUtil.stringListVal(swordID, "Sword ability - successful ability radius highlight colors", map.get("successful-colors"), List.of("#13eb4c"))
                        );

                        abilityBuilder = abilityBuilder.failRadiusHighlightEnabled(
                                CheckUtil.booleanVal(swordID, "Sword ability - fail ability radius hightlight enabled value", map.get("failed-ability-highlight-enabled"), true)
                        );

                        abilityBuilder = abilityBuilder.failRadiusHighlightColors(
                                CheckUtil.stringListVal(swordID, "Sword ability - fail ability radius highlight colors", map.get("fail-colors"), List.of("#870000"))
                        );
                    }
                }

                case EFFECT_SELF -> abilityBuilder = abilityBuilder.selfEffect(
                        CheckUtil.validPotionEffect(swordID, swordAbilityMap)
                );

                case LIGHTNING_BOLT -> abilityBuilder = abilityBuilder.lightningDamage(
                        CheckUtil.doubleVal(swordID, "Sword ability - lightning bolt damage value", swordAbilityMap.get("lightning_damage"), 5.0)
                );

                case LIGHTNING_BOLT_RADIUS -> {
                    abilityBuilder = abilityBuilder.radiusLightningDamage(
                            CheckUtil.doubleVal(swordID, "Sword ability - radius lightning bolt damage value", swordAbilityMap.get("lightning_damage"), 5.0)
                    );

                    abilityBuilder = abilityBuilder.lightningRadius(
                            CheckUtil.intVal(swordID, "Sword ability - lightning bolt radius value", swordAbilityMap.get("radius"), 3)
                    );

                    Map<?, ?> map = CheckUtil.mapVal(swordID, "Sword ability - radius hightlight map", swordAbilityMap.get("radius_highlight_particle"));

                    abilityBuilder = abilityBuilder.successRadiusHighlightEnabled(
                            CheckUtil.booleanVal(swordID, "Sword ability - successful ability radius hightlight enabled value", map.get("successful-ability-highlight-enabled"), true)
                    );

                    abilityBuilder = abilityBuilder.successRadiusHighlightColors(
                            CheckUtil.stringListVal(swordID, "Sword ability - successful ability radius highlight colors", map.get("successful-colors"), List.of("#13eb4c"))
                    );

                    abilityBuilder = abilityBuilder.failRadiusHighlightEnabled(
                            CheckUtil.booleanVal(swordID, "Sword ability - fail ability radius hightlight enabled value", map.get("failed-ability-highlight-enabled"), true)
                    );

                    abilityBuilder = abilityBuilder.failRadiusHighlightColors(
                            CheckUtil.stringListVal(swordID, "Sword ability - fail ability radius highlight colors", map.get("fail-colors"), List.of("#870000"))
                    );
                }
            }
            ability = abilityBuilder.build();
            abilities.add(ability);
        }
        return abilities;
    }


    public record Sword(PSword swordInstance) {
    }

    public Sword getSword(String key) {
        return this.swords.get(key);
    }

    private void warn(String message) {
        Logger.fail(message);
    }
}