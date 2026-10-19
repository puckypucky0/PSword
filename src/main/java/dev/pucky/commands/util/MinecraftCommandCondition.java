package dev.pucky.commands.util;

import dev.pucky.util.MMUtil;
import dev.pucky.util.PlayerUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

@Builder
@Getter
@Accessors(fluent = true)
public class MinecraftCommandCondition {

    @Builder.Default
    private Set<String> permissions = Set.of();
    @Builder.Default
    private boolean shouldHaveAllPermissions = false;
    @Builder.Default
    private Boolean accountMustBeLinked = null;
    @Builder.Default
    private Boolean consoleCanUse = null;
    @Builder.Default
    private World world = null;

    private boolean hasRequiredPermissions(Player player) {
        return this.permissions.isEmpty() ||
                (this.shouldHaveAllPermissions
                ? PlayerUtil.hasPermissions(player, this.permissions)
                : PlayerUtil.hasAnyPermission(player, this.permissions));
    }

    private boolean isInRequiredWorld(Player player) {
        return this.world == null || player.getWorld().equals(this.world);
    }

    public boolean canUse(CommandSender sender) {
        if (sender instanceof Player player) {
            boolean permissionCheck = this.hasRequiredPermissions(player);
            boolean worldCheck = this.isInRequiredWorld(player);

            return permissionCheck && worldCheck;
        } else return consoleCanUse != null ? consoleCanUse : false;
    }

    public boolean canSee(CommandSender sender) {
        if (sender instanceof Player player) {
            boolean permissionCheck = this.hasRequiredPermissions(player);
            boolean worldCheck = this.isInRequiredWorld(player);

            return permissionCheck && worldCheck;
        } else return consoleCanUse != null ? consoleCanUse : false;
    }

    public Component requirementMessage(CommandSender sender) {
        Component component = MMUtil.formatColorsPrefixed("<n>Bu komutu kullanabilmek için şartları karşılamıyorsunuz.\n");

        if (!(sender instanceof Player) && !(consoleCanUse != null ? consoleCanUse : false))
            return component.append(MMUtil.formatColorsPrefixed("<#980000>Konsoldan bu komutu kullanamazsınız."));

        else if (sender instanceof Player player) {
            boolean permissionCheck = this.hasRequiredPermissions(player);
            boolean worldCheck = this.isInRequiredWorld(player);

            if (!permissionCheck)
                component = component.append(MMUtil.formatColorsPrefixed("<#D50000>Yetkileriniz <#980000>yetersiz veya eksik."));
            if (!worldCheck)
                component = component.append(MMUtil.formatColorsPrefixed("<#980000>" + ((world == null) ? "Gerekli dünyada değilsiniz." : "<#D50000>" + world.getName() + " dünyasında olmalısınız.")));

            return component;
        }
        return component.append(MMUtil.deserialize("<dark_red>Bir hata oluştu."));
    }

}
