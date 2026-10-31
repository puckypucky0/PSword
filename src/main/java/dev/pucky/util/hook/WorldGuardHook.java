package dev.pucky.util.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.pucky.util.logs.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {
    private static StateFlag pswordFlag;

    public static void register() {
        StateFlag flag = new StateFlag("psword", true);

        try {
            WorldGuard.getInstance().getFlagRegistry().register(flag);
            pswordFlag = flag;
        } catch (FlagConflictException e) {
            Logger.fatal("<red>Exception occured in WorldGuardHook: " + e.getMessage());
        }
    }

    public static boolean canUse(Player player, Location location) {
        if (pswordFlag == null)
            return true;

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location wrappedLocation = BukkitAdapter.adapt(location);

        return query.testState(wrappedLocation, localPlayer, pswordFlag);
    }
}
