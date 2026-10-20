package dev.pucky.sword;

import dev.pucky.PSwordMain;
import dev.pucky.sword.effect.SwordEffect;
import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;

@Builder
public class PSword {
    public String swordID;
    public Component name;
    public List<Component> lore;
    public Material itemType;
//    public Set<SwordAbility> swordAbilities;
    public Set<SwordEffect> swordEffects;


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
