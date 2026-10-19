package dev.pucky.commands.util;

import dev.pucky.commands.util.tab.TabCompleteEntry;
import dev.pucky.commands.util.tab.TabcompleteContext;
import dev.pucky.util.MMUtil;
import dev.pucky.util.logs.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Tek bir kök komutun altındaki alt komutları yönetir: /pdungeon join, /pdungeon leave ...
 */
public class MinecraftCommandManager implements CommandExecutor, TabCompleter {

    private final Map<String, MinecraftCommand> commands = new LinkedHashMap<>();
    private final String rootName;

    public MinecraftCommandManager(JavaPlugin plugin, String rootName) {
        this.rootName = rootName;

        PluginCommand root = plugin.getCommand(rootName);
        if (root == null) {
            Logger.fatal("<yellow>/" + rootName + " <gray>plugin.yml'de tanımlı değil, <red>komutlar kaydedilemedi.");
            return;
        }

        root.setExecutor(this);
        root.setTabCompleter(this);
        Logger.success("<green>/" + rootName + " <gray>komutu kaydedildi.");
    }

    public void register(MinecraftCommand... minecraftCommands) {
        for (MinecraftCommand command : minecraftCommands)
            commands.put(command.metadata().name().toLowerCase(Locale.ROOT), command);
    }

    public MinecraftCommand getCommand(String name) {
        return commands.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MMUtil.deserialize("<red>Kullanım: /" + rootName + " <komut>"));
            return true;
        }

        MinecraftCommand subCommand = getCommand(args[0]);
        if (subCommand == null) {
            sender.sendMessage(MMUtil.deserialize("<red>Böyle bir komut yok."));
            return true;
        }

        if (!subCommand.metadata().canUse(sender)) {
            sender.sendMessage(subCommand.metadata().requirementMessage(sender));
            return true;
        }

        subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return List.of();

        if (args.length == 1) return suggestCommandNames(sender, args[0]);

        MinecraftCommand subCommand = getCommand(args[0]);
        if (subCommand == null || !subCommand.metadata().canUse(sender)) return List.of();

        return suggestArguments(subCommand, sender, Arrays.copyOfRange(args, 1, args.length));
    }

    private List<String> suggestCommandNames(CommandSender sender, String input) {
        String lowered = input.toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();

        for (MinecraftCommand command : commands.values()) {
            String name = command.metadata().name().toLowerCase(Locale.ROOT);
            if (name.startsWith(lowered) && command.metadata().canUse(sender))
                suggestions.add(name);
        }

        return suggestions;
    }

    private List<String> suggestArguments(MinecraftCommand command, CommandSender sender, String[] subArgs) {
        TabcompleteContext ctx = new TabcompleteContext(sender, subArgs);
        String input = ctx.input().toLowerCase(Locale.ROOT);
        Set<String> suggestions = new LinkedHashSet<>();

        for (TabCompleteEntry entry : command.metadata().tabCompletes()) {
            if (!entry.matchesIndex(ctx.index()) || !entry.canSee(sender)) continue;

            Collection<String> candidates = entry.suggest(ctx);
            for (String candidate : candidates)
                if (candidate != null && candidate.toLowerCase(Locale.ROOT).startsWith(input))
                    suggestions.add(candidate);
        }

        return new ArrayList<>(suggestions);
    }
}
