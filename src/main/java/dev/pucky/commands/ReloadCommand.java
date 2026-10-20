package dev.pucky.commands;

import dev.pucky.commands.util.MinecraftCommand;
import dev.pucky.commands.util.MinecraftCommandMetadata;
import dev.pucky.config.ConfigManager;
import dev.pucky.util.MMUtil;
import dev.pucky.util.logs.Logger;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements MinecraftCommand {

    public ConfigManager configManager;

    public ReloadCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            configManager.load();
        } catch (Exception e) {
            sender.sendMessage(MMUtil.formatColorsPrefixed(
                    "<f>Konfigürasyon yüklenemedi: <v>" + e.getMessage() + " <n>Ayrıntılar için konsola bakın."
            ));
            Logger.fail("<gray>Config reload <red>failed: <white>" + e);
            e.printStackTrace();
            return;
        }

        sender.sendMessage(MMUtil.formatColorsPrefixed(
                "<s>Konfigürasyon yeniden yüklendi. <v>" + configManager.getSwords().size() + " kılıç yüklendi."
        ));
    }

    @Override
    public MinecraftCommandMetadata metadata() {
        return MinecraftCommandMetadata.of(
                "reload",
                "Reloads the PSword configuration."
        )
                .permission("psword.reload");
    }
}
