package dev.pucky.config;

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
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        particleOffsetX = plugin.getConfig().getDouble("settings.particleOffsetX", 0.3);
        particleOffsetY = plugin.getConfig().getDouble("settings.particleOffsetY", 0.8);
        particleOffsetZ = plugin.getConfig().getDouble("settings.particleOffsetZ", 0.3);
        particleOffsetExtra = plugin.getConfig().getDouble("settings.particleOffsetExtra", 0.02);
        effectShowOnPlayersOnly = plugin.getConfig().getBoolean("settings.effectShowOnPlayersOnly", true);
        abilitiesFireOnPlayersOnly = plugin.getConfig().getBoolean("settings.abilitiesFireOnPlayersOnly", true);

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
                    effectBuilder = effectBuilder.particleType(
                            CheckUtil.particleVal(swordID, swordEffectMap, "flame")
                    );

                    effectBuilder = effectBuilder.particleMultiplier(
                            CheckUtil.doubleVal(swordID, "Sword effect particle multiplier value", swordEffectMap.get("multiplier"), 1.0)
                    );
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
                }

                case EFFECT_SELF -> abilityBuilder = abilityBuilder.selfEffect(
                        CheckUtil.validPotionEffect(swordID, swordAbilityMap)
                );

                case LIGHTNING_BOLT -> abilityBuilder = abilityBuilder.lightningDamage(
                        CheckUtil.doubleVal(swordID, "Sword ability - lightning bolt damage value", swordAbilityMap.get("lightning_damage"), 5.0)
                );
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