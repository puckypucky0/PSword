package dev.pucky.commands.util.tab;

import org.bukkit.command.CommandSender;

/**
 * args: komut adı çıkarılmış alt argümanlar.
 * index: yazılmakta olan argümanın sırası.
 * input: anlık yazılmakta olan kelime.
 */
public record TabcompleteContext(CommandSender sender, String[] args, int index, String input) {

    public TabcompleteContext(CommandSender sender, String[] args) {
        this(sender, args, args.length - 1, args.length == 0 ? "" : args[args.length - 1]);
    }

    public String argAt(int i) {
        return (i < 0 || i >= args.length) ? null : args[i];
    }

    public String previousArg() {
        return argAt(index - 1);
    }
}
