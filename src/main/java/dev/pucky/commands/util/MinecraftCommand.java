package dev.pucky.commands.util;

import org.bukkit.command.CommandSender;

public interface MinecraftCommand {

    void execute(CommandSender sender, String[] args);

    MinecraftCommandMetadata metadata();
}
