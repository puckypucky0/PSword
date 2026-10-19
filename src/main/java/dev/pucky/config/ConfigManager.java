package dev.pucky.config;

import dev.pucky.sword.PSword;
import dev.pucky.util.MMUtil;
import dev.pucky.util.logs.Logger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
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
            if (swordMaterial == null) {
                warn("<yellow>Provided sword material (" + swordMaterialString + ") for sword ID " + swordID + " is <red>invalid. Will use IRON_SWORD for default.");
                swordMaterial = Material.IRON_SWORD;
            }

            swords.put(swordID, new Sword(
                    PSword.builder()
                            .name(swordName)
                            .lore(swordLore)
                            .itemType(swordMaterial)
                            .build()
            ));
        }
    }

//    private List<SwordAbility> loadAbilities(List<Map<?, ?>> list) {
//        List<SwordAbility> abilities = new ArrayList<>();
//
//        for (Map<?, ?> lootDataMap : list) {
//            boolean itemCheck = lootDataMap.get("item") instanceof String;
//            boolean chanceCheck = lootDataMap.get("chance") instanceof Number;
//            boolean minCheck = lootDataMap.get("min") instanceof Integer;
//            boolean maxCheck = lootDataMap.get("max") instanceof Integer;
//            boolean check = itemCheck && chanceCheck && minCheck && maxCheck;
//
//            if (check) {
//
//                String itemBase64 = (String) lootDataMap.get("item");
//                double chance = ((Number) lootDataMap.get("chance")).doubleValue();
//                int min = (int) lootDataMap.get("min");
//                int max = (int) lootDataMap.get("max");
//
//                ItemStack itemStack = null;
//                try {
//                    itemStack = CommandUtils.decode(itemBase64);
//                } catch (Exception e) {
//                    Logger.fail("<gray>Loot base64'ü <red>çözülemedi");
//                }
//
//                if (itemStack != null) {
//                    lootEntries.add(
//                            new LootEntry(
//                                    itemStack,
//                                    chance,
//                                    min,
//                                    max
//                            )
//                    );
//                }
//            }
//        }
//        return lootEntries;
//    }


    public record Sword(PSword swordInstance) {
    }

    public Sword getSword(String key) {
        return this.swords.get(key);
    }

    private void warn(String message) {
        Logger.fail(message);
    }
}