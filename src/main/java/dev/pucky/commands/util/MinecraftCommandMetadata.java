package dev.pucky.commands.util;

import dev.pucky.commands.util.tab.TabCompleteEntry;
import dev.pucky.util.MMUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public record MinecraftCommandMetadata(
        String name,
        String description,
        String permission,
        boolean consoleAllowed,
        List<TabCompleteEntry> tabCompletes) {

    public static MinecraftCommandMetadata of(String name, String description) {
        return new MinecraftCommandMetadata(name, description, null, true, List.of());
    }

    public MinecraftCommandMetadata permission(String permission) {
        return new MinecraftCommandMetadata(name, description, permission, consoleAllowed, tabCompletes);
    }

    public MinecraftCommandMetadata playerOnly() {
        return new MinecraftCommandMetadata(name, description, permission, false, tabCompletes);
    }

    public MinecraftCommandMetadata tabCompletes(TabCompleteEntry... entries) {
        return new MinecraftCommandMetadata(name, description, permission, consoleAllowed, List.of(entries));
    }

    public boolean canUse(CommandSender sender) {
        if (!(sender instanceof Player) && !consoleAllowed) return false;
        return permission == null || sender.hasPermission(permission);
    }

    public Component requirementMessage(CommandSender sender) {
        if (!(sender instanceof Player) && !consoleAllowed)
            return MMUtil.deserialize("<red>Bu komutu konsoldan <dark_red>kullanamazsınız.");

        return MMUtil.deserialize("<red>Bu komutu kullanmak için yetkiniz yok.");
    }
}
