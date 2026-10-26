package dev.pucky.listeners;

import dev.pucky.PSwordMain;
import dev.pucky.config.ConfigManager;
import dev.pucky.sword.PSword;
import dev.pucky.sword.Trigger;
import dev.pucky.sword.effect.SwordEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class SwordEffectListener implements Listener {
    public ConfigManager configManager;

    public SwordEffectListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordDamageDealTrigger(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damagerPlayer))
            return;

        Entity victimEntity = event.getEntity();

        if (!configManager.isEffectShowOnPlayersOnly() || victimEntity instanceof Player) {
            this.handleSwordEffect(damagerPlayer, Trigger.ON_DAMAGE_DEAL, victimEntity.getLocation().add(0, 0.5, 0));

            if (event.isCritical())
                this.handleSwordEffect(damagerPlayer, Trigger.ON_HIT_CRIT, victimEntity.getLocation().add(0, 0.5, 0));
        }

        if (victimEntity instanceof Player victimPlayer)
            this.handleSwordEffect(victimPlayer, Trigger.ON_DAMAGE_RECEIVE, damagerPlayer.getLocation().add(0, 0.5, 0));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordHolderDeath(EntityDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player killerPlayer))
            return;

        Entity diedEntity = event.getEntity();
        if (configManager.isEffectShowOnPlayersOnly()) {
            if (diedEntity instanceof Player diedPlayer)
                this.handleSwordEffect(killerPlayer, Trigger.ON_KILL, diedPlayer.getLocation());
        } else this.handleSwordEffect(killerPlayer, Trigger.ON_KILL, event.getEntity().getLocation());

    }

    public void handleSwordEffect(Player holder, Trigger trigger, Location location) {
        for (ConfigManager.Sword swordValue : configManager.getSwords().values()) {
            PSword sword = swordValue.swordInstance();
            ItemStack mainItem = holder.getInventory().getItemInMainHand();
            String swordKey = mainItem.getPersistentDataContainer().get(new NamespacedKey(PSwordMain.instance, "psword_id"), PersistentDataType.STRING);

            if (Objects.equals(swordKey, swordValue.swordInstance().swordID())) { // Found sword
                for (SwordEffect effect : sword.swordEffects()) {
                    if (effect.effectTrigger == trigger)
                        this.handleSwordEffectSpawn(effect, location, location.getWorld());
                }
            }
        }
    }

    public void handleSwordEffectSpawn(SwordEffect effect, Location effectLocation, World world) {
        switch (effect.effectType) {
            case SOUND -> world.playSound(effectLocation, effect.sound, (float) effect.volume, (float) effect.pitch);

            case LIGHTNING -> world.strikeLightningEffect(effectLocation);

            case PARTICLE -> world.spawnParticle(effect.particleType,
                    effectLocation.clone().add(0, 1, 0),
                    Math.max(1, Math.toIntExact(Math.round(25 * effect.particleMultiplier))),
                    configManager.getParticleOffsetX(),
                    configManager.getParticleOffsetY(),
                    configManager.getParticleOffsetZ(),
                    configManager.getParticleOffsetExtra()
            );
        }
    }
}
