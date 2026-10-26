package dev.pucky.util;

import dev.pucky.PSwordMain;
import dev.pucky.sword.Trigger;
import dev.pucky.sword.ability.SwordAbilityType;
import dev.pucky.sword.effect.SwordEffectType;
import dev.pucky.util.logs.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class CheckUtil {
    public static <T> T getOrDefault(String path, Class<T> type, T def) {
        FileConfiguration config = PSwordMain.instance.getConfig();
        Object value = config.get(path);

        if (!config.contains(path, true) || !type.isInstance(value)) {
            Logger.warn("<dark_red>" + path + " <gray>is missing or invalid. <dark_red>Using default: <white>" + def);
            return def;
        }
        return type.cast(value);
    }

    public static double doubleVal(String swordID, String keyName, Object value, double def) {
        if (value instanceof Number n && n.doubleValue() >= 0)
            return n.doubleValue();

        Logger.fail("<red> " + keyName + " is malformed or missing. <white>Using " + def + " for default - " + swordID);
        return def;
    }

    public static int intVal(String swordID, String keyName, Object value, int def) {
        if (value instanceof Number n && n.intValue() >= 0)
            return n.intValue();

        Logger.fail("<red> " + keyName + " is malformed or missing. <white>Using " + def + " for default - " + swordID);
        return def;
    }

    public static String stringVal(String swordID, String keyName, Object value, String def) {
        if (value instanceof String s) {
            String stripped = MMUtil.stripLegacy(s);
            if (!stripped.isBlank())
                return stripped;
        }

        Logger.fail("<red> " + keyName + " is malformed or missing. <white>Using " + def + " for default - " + swordID);
        return def;
    }

    public static String stringVal(String keyName, Object value) {
        if (value instanceof String s) {
            String stripped = MMUtil.stripLegacy(s);
            if (!stripped.isBlank())
                return stripped;
        }

        Logger.fail("<red> " + keyName + " is malformed or missing. This part won't be loaded.");
        return null;
    }

    public static Sound soundVal(String swordID, Map<?, ?> sectionMap, String defaultSoundKey) {
        Registry<Sound> registry = Registry.SOUNDS;
        boolean soundKeyCheck = sectionMap.get("sound") instanceof String soundKey
                && !soundKey.isEmpty()
                && NamespacedKey.fromString(soundKey) != null
                && registry.get(NamespacedKey.fromString(soundKey)) != null;

        if (!soundKeyCheck)
            Logger.fail("<red>Sound key is malformed or missing. [" + swordID + "] Will use <white>" + defaultSoundKey + " for default.");

        return soundKeyCheck ?
                registry.get(NamespacedKey.fromString((String) sectionMap.get("sound"))) :
                registry.get(NamespacedKey.fromString(defaultSoundKey));
    }

    public static Particle particleVal(String swordID, Map<?, ?> sectionMap, String defaultParticleKey) {
        Registry<Particle> registry = Registry.PARTICLE_TYPE;
        boolean particleKeyCheck = sectionMap.get("particle_type") instanceof String particleKey
                && !particleKey.isEmpty()
                && NamespacedKey.fromString(particleKey) != null
                && registry.get(NamespacedKey.fromString(particleKey)) != null
                && registry.get(NamespacedKey.fromString(particleKey)).getDataType() == Void.class;

        if (!particleKeyCheck)
            Logger.fail("<red>Particle key is malformed, missing or parameterized, which is not supported by now. [" + swordID + "] Will use <white>" + defaultParticleKey + " for default.");

        return particleKeyCheck ?
                registry.get(NamespacedKey.fromString((String) sectionMap.get("particle_type"))) :
                registry.get(NamespacedKey.fromString(defaultParticleKey));
    }

    public static Trigger validTriggerType(Object triggerKey) {
        if (triggerKey instanceof String effectTriggerKey) {
            try {
                return Trigger.valueOf(effectTriggerKey);
            } catch (IllegalArgumentException ignored) {
                Logger.fatal("Trigger key is invalid. This ability / effect will be skipped.");
                return null;
            }

        } return null;
    }

    public static SwordEffectType validSwordEffectType(Object effectType) {
        if (effectType instanceof String effectTypeKey) {
            try {
                return SwordEffectType.valueOf(effectTypeKey);
            } catch (IllegalArgumentException ignored) {
                Logger.fatal("Sword effect type key is invalid. This effect will be skipped.");
                return null;
            }

        } return null;
    }

    public static SwordAbilityType validSwordAbilityType(Object abilityType) {
        if (abilityType instanceof String abilityTypeKey) {
            try {
                return SwordAbilityType.valueOf(abilityTypeKey);
            } catch (IllegalArgumentException ignored) {
                Logger.fatal("Sword ability type key is invalid. This effect will be skipped.");
                return null;
            }

        } return null;
    }

    public static PotionEffect validPotionEffect(String swordID, Map<?, ?> abilityMap) {
        PotionEffectType effect = null;
        NamespacedKey key = (abilityMap.get("effect_type") instanceof String s && !s.isEmpty()) ? NamespacedKey.fromString(s) : null;

        if (key != null) effect = Registry.EFFECT.get(key);

        if (effect == null) {
            Logger.fail("<red>Effect key is malformed or missing. <white>Using SLOWNESS for default. - " + swordID);
            effect = PotionEffectType.SLOWNESS;
        }

        return new PotionEffect(
                effect,
                intVal(swordID, "Ability potion effect duration value", abilityMap.get("duration_seconds"), 10) * 20,
                intVal(swordID, "Ability potion effect level value", abilityMap.get("level"), 1) - 1
        );
    }
}
