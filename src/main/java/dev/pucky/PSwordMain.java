package dev.pucky;

import dev.pucky.commands.GiveSwordCommand;
import dev.pucky.commands.ReloadCommand;
import dev.pucky.commands.util.MinecraftCommandManager;
import dev.pucky.config.ConfigManager;
import dev.pucky.util.CommandUtils;
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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
