package dev.pucky.config;

import dev.pucky.sword.PSword;
import dev.pucky.sword.effect.SwordEffect;
import dev.pucky.sword.effect.SwordEffectType;
import dev.pucky.util.MMUtil;
import dev.pucky.util.logs.Logger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class ConfigManager {

    private final JavaPlugin plugin;

    @Getter
    private final Map<String, Sword> swords = new LinkedHashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        swords.clear();
        loadSwords();

        plugin.getLogger().info(swords.size() + " kılıç yüklendi.");
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
                warn("<yellow>Sword material isn't set for sword " + swordID + ". Defaulting to IRON.");
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
                            .swordEffects(this.loadEffects(swordID, perSwordSection.getMapList("effects")))
                            .itemType(swordMaterial)
                            .build()
            ));
        }
    }

    private Set<SwordEffect> loadEffects(String swordID, List<Map<?, ?>> list) {
        Set<SwordEffect> effects = new HashSet<>();

        for (Map<?, ?> swordEffectMap : list) {
            if (swordEffectMap.get("type") instanceof String effectTypeKey) {
                SwordEffectType effectType = null;

                try {
                    effectType = SwordEffectType.valueOf(effectTypeKey);
                } catch (IllegalArgumentException ignored) {}

                SwordEffect.SwordEffectBuilder effectBuilder = SwordEffect.builder().effectType(effectType);
                SwordEffect effect;

                switch (effectType) {
                    case LIGHTNING -> effect = effectBuilder.build();
                    case SOUND -> {
                        boolean soundKeyCheck = swordEffectMap.get("sound") instanceof String soundKey
                                && !soundKey.isEmpty()
                                && NamespacedKey.fromString(soundKey) != null
                                && Registry.SOUNDS.get(NamespacedKey.fromString(soundKey)) != null;

                        boolean volumeCheck = swordEffectMap.get("volume") instanceof Number volume
                               && volume.doubleValue() > 0;


                        boolean pitchCheck = swordEffectMap.get("pitch") instanceof Number pitch
                                && pitch.doubleValue() > 0;


                        if (!soundKeyCheck) {
                            Logger.fail("<red>Sound key is malformed or missing. [" + swordID + "] Will use <white>entity.player.levelup for default.");
                            effectBuilder = effectBuilder.sound(Sound.ENTITY_PLAYER_LEVELUP);
                        } else {
                            Sound soundKey = Registry.SOUNDS.get(NamespacedKey.fromString((String) swordEffectMap.get("sound")));

                            effectBuilder = effectBuilder.sound(soundKey);
                        }

                        if (!volumeCheck) {
                            Logger.fail("<red>Sound volume key is malformed or missing. [" + swordID + "] Will use <white>1.0 for default.");
                            effectBuilder = effectBuilder.volume(1.0);
                        } else {
                            Number volumeKey = ((Number) swordEffectMap.get("volume")).doubleValue();

                            effectBuilder = effectBuilder.volume(volumeKey.doubleValue());
                        }

                        if (!pitchCheck) {
                            Logger.fail("<red>Sound pitch key is malformed or missing. [" + swordID + "]  Will use <white>1.0 for default.");
                            effectBuilder = effectBuilder.pitch(1.0);
                        } else {
                            Number pitchKey = ((Number) swordEffectMap.get("pitch")).doubleValue();

                            effectBuilder = effectBuilder.pitch(pitchKey.doubleValue());
                        }

                        effect = effectBuilder.build();
                    }
                    case PARTICLE -> {
                        boolean particleKeyCheck = swordEffectMap.get("particle_type") instanceof String particleKey
                                && !particleKey.isEmpty()
                                && NamespacedKey.fromString(particleKey) != null
                                && Registry.PARTICLE_TYPE.get(NamespacedKey.fromString(particleKey)) != null
                                && Registry.PARTICLE_TYPE.get(NamespacedKey.fromString(particleKey)).getDataType() == Void.class;

                        boolean multiplierKeyCheck = swordEffectMap.get("multiplier") instanceof Number multiplier
                                && multiplier.doubleValue() > 0;


                        if (!particleKeyCheck) {
                            Logger.fail("<red>Particle key is malformed, missing or <yellow>parameterized (dust, block or item). This version of PSword doesn't support parameterized particle types.  [" + swordID + "] Will use <white>flame for default.");
                            effectBuilder = effectBuilder.particleType(Particle.FLAME);
                        } else {
                            Particle particle = Registry.PARTICLE_TYPE.get(NamespacedKey.fromString((String) swordEffectMap.get("particle_type")));

                            effectBuilder = effectBuilder.particleType(particle);
                        }

                        if (!multiplierKeyCheck) {
                            Logger.fail("<red>Particle multiplier key is malformed or missing. [" + swordID + "] Will use <white>1.0 for default.");
                            effectBuilder = effectBuilder.particleMultiplier(1.0);
                        } else {
                            Number multiplierKey = ((Number) swordEffectMap.get("multiplier")).doubleValue();

                            effectBuilder = effectBuilder.particleMultiplier(multiplierKey.doubleValue());
                        }

                        effect = effectBuilder.build();
                    }
                    case null -> {
                        Logger.fatal("<dark_red>Sword effect key " + effectTypeKey + " is invalid for sword key " + swordID);
                        continue;
                    }
                }

                effects.add(effect);
            } else Logger.fatal("<dark_red>No sword effect type specified for sword " + swordID);
        }
        return effects;
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