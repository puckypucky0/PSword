package dev.pucky.sword;

import dev.pucky.PSwordMain;
import dev.pucky.sword.ability.SwordAbility;
import dev.pucky.sword.effect.SwordEffect;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;

@Builder @Getter @Accessors(fluent = true)
public class PSword {
    private String swordID;
    private Component name;
    private List<Component> lore;
    private Material itemType;
    private Set<SwordAbility> swordAbilities;
    private Set<SwordEffect> swordEffects;


    public ItemStack swordItem() {
        ItemStack itemStack = new ItemStack(this.itemType, 1);
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(name);
        meta.lore(lore);
        meta.addItemFlags(
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_ATTRIBUTES
        );

        NamespacedKey swordKey = new NamespacedKey(PSwordMain.instance, "psword_id");
        meta.getPersistentDataContainer().set(swordKey, PersistentDataType.STRING, swordID);

        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
