package dev.pucky.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class PlayerUtil {
    public static int itemCountInInventory(Player player, Material type) {
        int itemCount = 0;
        for (ItemStack item : player.getInventory().getContents())
            if (item != null && item.getType() == type)
                itemCount += item.getAmount();

        return itemCount;
    }

    public static boolean itemCanFit(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() != -1)
            return true;


        for (ItemStack content : player.getInventory().getContents()) {
            if (content != null && content.isSimilar(item)) {
                if (content.getAmount() < content.getMaxStackSize()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAnyPermission(Player player, Set<String> perms) {
        return perms.stream().anyMatch(player::hasPermission);
    }

    public static boolean hasPermissions(Player player, Set<String> permissions) {
        if (player == null || permissions == null || permissions.isEmpty()) return false;

        for (String perm : permissions) {
            if (!player.hasPermission(perm)) {
                return false;
            }
        }

        return true;
    }
}
