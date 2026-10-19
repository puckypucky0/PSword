package dev.pucky.commands.util.tab;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public record TabCompleteEntry(
        int index,
        String permission,
        Function<TabcompleteContext, Collection<String>> provider) {

    public static final int ANY_INDEX = -1;

    public static TabCompleteEntry of(int index, String... values) {
        List<String> list = List.of(values);
        return new TabCompleteEntry(index, null, ctx -> list);
    }

    public static TabCompleteEntry of(int index, Function<TabcompleteContext, Collection<String>> provider) {
        return new TabCompleteEntry(index, null, provider);
    }

    public static TabCompleteEntry ofPlayer(int index, Function<Player, Collection<String>> provider) {
        return new TabCompleteEntry(index, null, ctx ->
                ctx.sender() instanceof Player player ? provider.apply(player) : List.of());
    }

    public TabCompleteEntry permission(String permission) {
        return new TabCompleteEntry(index, permission, provider);
    }

    public boolean canSee(CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }

    public boolean matchesIndex(int currentIndex) {
        return index == ANY_INDEX || index == currentIndex;
    }

    public Collection<String> suggest(TabcompleteContext ctx) {
        Collection<String> values = provider.apply(ctx);
        return values == null ? List.of() : values;
    }
}
