package dev.pucky.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CommandUtils {
    public static List<Integer> checkIntegerArgs(String[] args, int... indexes) {
        List<Integer> invalid = new ArrayList<>();

        for (int index : indexes) {
            if (index >= args.length || parseIntOrNull(args[index]) == null)
                invalid.add(index);
        }

        return invalid;
    }

    public static Location parseLocationFromArgs(String[] args, int worldIndex, int xIndex, int yIndex, int zIndex) {
        if (!checkIntegerArgs(args, xIndex, yIndex, zIndex).isEmpty())
            return null;

        if (worldIndex >= args.length) return null;

        World world = Bukkit.getWorld(args[worldIndex]);
        if (world == null) return null;

        return new Location(
                world,
                parseIntOrNull(args[xIndex]),
                parseIntOrNull(args[yIndex]),
                parseIntOrNull(args[zIndex])
        );
    }

    public static Integer parseIntOrNull(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    public static Double parseDoubleOrNull(String raw) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    public static String stringifyLocation(Location location) {
        return location.getBlockX() + ", "
                + location.getBlockY() + ", "
                + location.getBlockZ();
    }


    public static String encode(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack decode(String base64) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(base64));
    }

    public static String formatDuration(long millis) {
        Duration d = Duration.ofMillis(millis);

        long hours = d.toHours();
        int minutes = d.toMinutesPart();
        int seconds = d.toSecondsPart();

        StringBuilder sb = new StringBuilder();
        if (hours > 0)   sb.append(hours).append(" saat ");
        if (minutes > 0) sb.append(minutes).append(" dakika ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append(" saniye");

        return sb.toString().trim();
    }
 }
