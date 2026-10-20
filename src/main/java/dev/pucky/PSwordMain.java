package dev.pucky;

import dev.pucky.commands.GiveSwordCommand;
import dev.pucky.commands.ReloadCommand;
import dev.pucky.commands.util.MinecraftCommandManager;
import dev.pucky.config.ConfigManager;
import dev.pucky.listeners.PSwordListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class PSwordMain extends JavaPlugin {
    public static PSwordMain instance;

    private ConfigManager configManager;
    private MinecraftCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.load();

        commandManager = new MinecraftCommandManager(this, "psword");
        commandManager.register(
                new ReloadCommand(configManager),
                new GiveSwordCommand(configManager)
        );

        getServer().getPluginManager().registerEvents(new PSwordListener(configManager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
