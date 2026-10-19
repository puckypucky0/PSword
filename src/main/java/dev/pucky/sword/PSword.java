package dev.pucky.sword;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@Builder
public class PSword {
    public Component name;
    public List<Component> lore;
    public Material itemType;
//    public Set<SwordAbility> swordAbilities;
//    public Set<SwordEffect> swordEffects;


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
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
