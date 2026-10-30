package dev.pucky;

import dev.pucky.commands.GiveSwordCommand;
import dev.pucky.commands.ReloadCommand;
import dev.pucky.commands.util.MinecraftCommandManager;
import dev.pucky.config.ConfigManager;
import dev.pucky.listeners.SwordAbilityListener;
import dev.pucky.listeners.SwordEffectListener;
import dev.pucky.sword.PSword;
import dev.pucky.sword.ability.SwordAbility;
import dev.pucky.util.MMUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Getter
public final class PSwordMain extends JavaPlugin implements Listener {
    public static PSwordMain instance;

    private ConfigManager configManager;
    private MinecraftCommandManager commandManager;

    private SwordAbilityListener abilityListener;
    private SwordEffectListener effectListener;

    private BukkitTask abilityCooldownTask;

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
        this.abilityListener = new SwordAbilityListener(configManager);
        this.effectListener = new SwordEffectListener(configManager);

        getServer().getPluginManager().registerEvents(abilityListener, this);
        getServer().getPluginManager().registerEvents(effectListener, this);
        getServer().getPluginManager().registerEvents(this, this);

        this.abilityCooldownTask = Bukkit.getScheduler().runTaskTimer(this, () -> {

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                ItemStack playerSword = onlinePlayer.getInventory().getItemInMainHand();

                String swordKey = playerSword.getPersistentDataContainer()
                        .get(new NamespacedKey(this, "psword_id"), PersistentDataType.STRING);

                if (swordKey == null || configManager.getSword(swordKey) == null) continue;
                PSword sword = configManager.getSword(swordKey).swordInstance();

                Component component = MMUtil.deserialize("| ");

                for (SwordAbility ability : sword.swordAbilities()) {
                    SwordAbilityListener.CooldownKey cooldownKey = new SwordAbilityListener.CooldownKey(onlinePlayer.getUniqueId(), ability);
                    Map<SwordAbilityListener.CooldownKey, Long> cooldowns = abilityListener.getCooldowns();

                    if (cooldowns.containsKey(cooldownKey)) {
                        long nextUse = (cooldowns.getOrDefault(cooldownKey, 0L) + ability.cooldownMillis());
                        double millis = ((nextUse - System.currentTimeMillis()) / 1000.0);
                        String secondsLeft = String.format(Locale.ROOT, "%.1f", millis) + "s | ";
                        if (millis > 0)
                            component =
                                    component.append(ability.abilityName())
                                            .append(MMUtil.deserialize(" "))
                                            .append(MMUtil.deserialize(secondsLeft));
                        else {
                            cooldowns.remove(cooldownKey);
                            onlinePlayer.showTitle(Title.title(
                                    MMUtil.EMPTY_COMPONENT,
                                    MMUtil.formatColors(MMUtil.serialize(ability.abilityName()) + " <s>tekrar kullanılabilir! ✔"),
                                    Title.Times.times(
                                            Duration.ofMillis(500),
                                            Duration.ofMillis(1000),
                                            Duration.ofMillis(250)
                                    )
                            ));
                            onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                        }
                    }
                }

                if (!sword.swordAbilities().isEmpty() && !MMUtil.serialize(component).equals("| "))
                    onlinePlayer.sendActionBar(component);
            }
        }, 5L, 5L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.abilityListener.getCooldowns().keySet().removeIf(key -> key.player().equals(event.getPlayer().getUniqueId()));
    }

    @Override
    public void onDisable() {
        if (this.abilityCooldownTask != null)
            this.abilityCooldownTask.cancel();
    }
}
