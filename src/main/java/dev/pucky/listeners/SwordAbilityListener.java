package dev.pucky.listeners;

import dev.pucky.PSwordMain;
import dev.pucky.config.ConfigManager;
import dev.pucky.sword.PSword;
import dev.pucky.sword.Trigger;
import dev.pucky.sword.ability.SwordAbility;
import dev.pucky.sword.ability.SwordAbilityType;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class SwordAbilityListener implements Listener {
    public ConfigManager configManager;

    @Getter
    private Map<CooldownKey, Long> cooldowns = new HashMap<>();

    public SwordAbilityListener(ConfigManager configManager) {
        this.configManager = configManager;
    }
    public record CooldownKey(UUID player, SwordAbility ability) {}

    @EventHandler(ignoreCancelled = true)
    public void onSwordDamageDealTrigger(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damagerPlayer))
            return;

        Entity victimEntity = event.getEntity();

        if (!configManager.isAbilitiesFireOnPlayersOnly() || victimEntity instanceof Player) {
            this.handleSwordAbilityFire(damagerPlayer, Trigger.ON_DAMAGE_DEAL, victimEntity);

            if (event.isCritical())
                this.handleSwordAbilityFire(damagerPlayer, Trigger.ON_HIT_CRIT, victimEntity);
        }

        if (victimEntity instanceof Player victimPlayer)
            this.handleSwordAbilityFire(victimPlayer, Trigger.ON_DAMAGE_RECEIVE, damagerPlayer);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordHolderDeath(EntityDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player killerPlayer))
            return;

        Entity diedEntity = event.getEntity();
        if (configManager.isAbilitiesFireOnPlayersOnly()) {
            if (diedEntity instanceof Player diedPlayer)
                this.handleSwordAbilityFire(killerPlayer, Trigger.ON_KILL, diedPlayer);
        } else this.handleSwordAbilityFire(killerPlayer, Trigger.ON_KILL, event.getEntity());

    }

    public void handleSwordAbilityFire(Player holder, Trigger trigger, Entity victimEntity) {
        for (ConfigManager.Sword swordValue : configManager.getSwords().values()) {
            PSword sword = swordValue.swordInstance();
            ItemStack mainItem = holder.getInventory().getItemInMainHand();
            String swordKey = mainItem.getPersistentDataContainer().get(new NamespacedKey(PSwordMain.instance, "psword_id"), PersistentDataType.STRING);

            if (Objects.equals(swordKey, swordValue.swordInstance().swordID())) { // Found sword
                for (SwordAbility ability : sword.swordAbilities()) {
                    if (ability.abilityTrigger() == trigger) {
                        this.handleSwordAbility(ability, holder, victimEntity);
                    }
                }
            }
        }
    }

    public void handleSwordAbility(SwordAbility ability, Player holderPlayer, Entity victimEntity) {
        CooldownKey cooldownKey = new CooldownKey(holderPlayer.getUniqueId(), ability);

        long nextUse = (cooldowns.getOrDefault(cooldownKey, 0L) + ability.cooldownMillis());
        if (nextUse < System.currentTimeMillis()) {
            cooldowns.put(cooldownKey, System.currentTimeMillis());

            switch (ability.abilityType()) {
                case EFFECT_SINGLE, EFFECT_RADIUS -> {
                    Integer radius = ability.enemyEffectRadius();
                    List<Entity> affectedEntities;

                    if (configManager.isAbilitiesFireOnPlayersOnly())
                        affectedEntities = ability.abilityType() == SwordAbilityType.EFFECT_RADIUS ?
                                (new ArrayList<>(holderPlayer.getLocation().getNearbyPlayers(radius))) :
                                List.of(victimEntity);

                    else affectedEntities = ability.abilityType() == SwordAbilityType.EFFECT_RADIUS ?
                            (new ArrayList<>(holderPlayer.getLocation().getNearbyEntities(radius, 4.0, radius))) :
                            List.of(victimEntity);

                    for (Entity affectedEntity : affectedEntities) {
                        if (affectedEntity instanceof LivingEntity living && holderPlayer != living)
                            living.addPotionEffect(ability.enemyEffect());

                    }
                }

                case EFFECT_SELF -> holderPlayer.addPotionEffect(ability.selfEffect());
                case LIGHTNING_BOLT -> {
                    if (victimEntity instanceof LivingEntity living) {
                        victimEntity.getWorld().strikeLightningEffect(victimEntity.getLocation()).setFlashCount(6);
                        living.damage(ability.lightningDamage());
                    }
                }
            }
        }
    }
}
