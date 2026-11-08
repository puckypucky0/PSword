package dev.pucky.listeners;

import dev.pucky.PSwordMain;
import dev.pucky.config.ConfigManager;
import dev.pucky.sword.PSword;
import dev.pucky.sword.Trigger;
import dev.pucky.sword.ability.SwordAbility;
import dev.pucky.sword.ability.SwordAbilityType;
import dev.pucky.util.MMUtil;
import dev.pucky.util.ParticleUtil;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;

public class SwordAbilityListener implements Listener {
    public ConfigManager configManager;

    @Getter
    private Map<CooldownKey, Long> cooldowns = new HashMap<>();
    public record CooldownKey(UUID player, SwordAbility ability) {}

    @Getter
    private final Map<ActiveEffectKey, Long> activeEffects = new HashMap<>();
    public record ActiveEffectKey(UUID player, SwordAbilityType type) {}

    public SwordAbilityListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

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

    @EventHandler
    public void onSwordRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) // Is hand
            return;

        if (event.getAction().isRightClick()) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
                if (!configManager.isRightClickIgnoreInteractables())
                    this.handleSwordAbilityFire(event.getPlayer(), Trigger.RIGHT_CLICK, null);
            }

            else this.handleSwordAbilityFire(event.getPlayer(), Trigger.RIGHT_CLICK, null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordRightClickTargeted(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) // Is hand
            return;

        this.handleSwordAbilityFire(event.getPlayer(), Trigger.RIGHT_CLICK, event.getRightClicked());
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

    @EventHandler(ignoreCancelled = true)
    public void onGlideToggle(EntityToggleGlideEvent event) {
        if (event.isGliding() && event.getEntity() instanceof Player player) {
            if (this.isActive(player.getUniqueId(), SwordAbilityType.ELYTRA_LOCK)) {
                event.setCancelled(true);
                player.sendActionBar(MMUtil.formatColors("<f>Elitran devre dışı. Uçamazsın"));
            }
        }
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
            boolean fired = false;

            if (this.isActive(holderPlayer.getUniqueId(), SwordAbilityType.SILENCE)) {
                holderPlayer.playSound(holderPlayer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            switch (ability.abilityType()) {
                case EFFECT_SINGLE, EFFECT_RADIUS -> {
                    Integer radius = ability.enemyEffectRadius();
                    List<Entity> affectedEntities;

                    if (victimEntity == null && ability.abilityType() == SwordAbilityType.EFFECT_SINGLE)
                        return;

                    fired = true;
                    if (configManager.isAbilitiesFireOnPlayersOnly()) {
                        affectedEntities = ability.abilityType() == SwordAbilityType.EFFECT_RADIUS ?
                                (new ArrayList<>(holderPlayer.getLocation().getNearbyPlayers(radius))) :
                                List.of(victimEntity);
                    }

                    else affectedEntities = ability.abilityType() == SwordAbilityType.EFFECT_RADIUS ?
                            (new ArrayList<>(holderPlayer.getLocation().getNearbyEntities(radius, 4.0, radius))) :
                            List.of(victimEntity);
                    int affected = 0;

                    boolean blocked = !PSwordMain.instance.canUse(holderPlayer, holderPlayer.getLocation());
                    for (Entity affectedEntity : affectedEntities) {
                        if (!this.canAffect(holderPlayer, affectedEntity))
                            continue;

                        if (PSwordMain.instance.canUse(holderPlayer, affectedEntity.getLocation())) {
                            LivingEntity living = (LivingEntity) affectedEntity;
                            living.addPotionEffect(ability.enemyEffect());
                            affected++;
                        } else blocked = true;
                    }

                    if (affected == 0) {
                        holderPlayer.sendActionBar(
                                MMUtil.formatColors(blocked ? "<f>Burada kullanamazsın!" : "<f>Yakında hiç kimse yok!")
                        );
                        fired = false;
                    }

                    if (ability.abilityType() == SwordAbilityType.EFFECT_RADIUS) {
                        ParticleUtil.drawRadiusCircle(radius, holderPlayer, fired, ability);
                    }
                }

                case EFFECT_SELF -> {
                    Location checkLocation = victimEntity != null ? victimEntity.getLocation() : holderPlayer.getLocation();
                    if (!PSwordMain.instance.canUse(holderPlayer, checkLocation))
                        return;

                    fired = true;
                    holderPlayer.addPotionEffect(ability.selfEffect());
                }

                case LIGHTNING_BOLT, LIGHTNING_BOLT_RADIUS -> {
                    Integer radius = ability.lightningRadius();
                    List<Entity> affectedEntities;

                    if (victimEntity == null && ability.abilityType() == SwordAbilityType.LIGHTNING_BOLT)
                        return;

                    fired = true;
                    if (configManager.isAbilitiesFireOnPlayersOnly()) {
                        affectedEntities = ability.abilityType() == SwordAbilityType.LIGHTNING_BOLT_RADIUS ?
                                (new ArrayList<>(holderPlayer.getLocation().getNearbyPlayers(radius))) :
                                List.of(victimEntity);
                    }

                    else affectedEntities = ability.abilityType() == SwordAbilityType.LIGHTNING_BOLT_RADIUS ?
                            (new ArrayList<>(holderPlayer.getLocation().getNearbyEntities(radius, 4.0, radius))) :
                            List.of(victimEntity);

                    int affected = 0;
                    boolean blocked = !PSwordMain.instance.canUse(holderPlayer, holderPlayer.getLocation());
                    for (Entity affectedEntity : affectedEntities) {
                        if (!this.canAffect(holderPlayer, affectedEntity))
                            continue;

                        if (PSwordMain.instance.canUse(holderPlayer, affectedEntity.getLocation())) {
                            LivingEntity living = (LivingEntity) affectedEntity;

                            holderPlayer.getWorld().strikeLightningEffect(affectedEntity.getLocation()).setFlashCount(6);
                            living.damage(ability.abilityType() == SwordAbilityType.LIGHTNING_BOLT_RADIUS ?
                                    ability.radiusLightningDamage() :
                                    ability.lightningDamage());
                            affected++;
                        } else blocked = true;
                    }

                    if (affected == 0) {
                        holderPlayer.sendActionBar(
                                MMUtil.formatColors(blocked ? "<f>Burada kullanamazsın!" : "<f>Yakında hiç kimse yok!")
                        );
                        fired = false;
                    }

                    if (ability.abilityType() == SwordAbilityType.LIGHTNING_BOLT_RADIUS) {
                        ParticleUtil.drawRadiusCircle(radius, holderPlayer, fired, ability);
                    }
                }
                case DASH -> {
                    Vector directionWithStrength = holderPlayer.getLocation().getDirection().multiply(ability.strength());
                    double currentY = directionWithStrength.getY();
                    double lastY = Math.clamp(currentY, 0.15, 0.50);
                    directionWithStrength.setY(lastY);

                    if (PSwordMain.instance.canUse(holderPlayer, holderPlayer.getLocation())) {
                        holderPlayer.setVelocity(directionWithStrength);
                        holderPlayer.playSound(holderPlayer, Sound.ENTITY_BAT_TAKEOFF, 5, 0.9f);
                        fired = true;
                    }
                }

                case WIND_BURST -> {
                    int radius = ability.windBurstRadius();
                    double strength = ability.strength();
                    List<Entity> affectedEntities;

                    fired = true;
                    if (configManager.isAbilitiesFireOnPlayersOnly())
                        affectedEntities = new ArrayList<>(holderPlayer.getLocation().getNearbyPlayers(radius));
                    else
                        affectedEntities = new ArrayList<>(holderPlayer.getLocation().getNearbyEntities(radius, 4.0, radius));

                    int affected = 0;
                    boolean blocked = !PSwordMain.instance.canUse(holderPlayer, holderPlayer.getLocation());

                    for (Entity affectedEntity : affectedEntities) {
                        if (!this.canAffect(holderPlayer, affectedEntity))
                            continue;

                        if (PSwordMain.instance.canUse(holderPlayer, affectedEntity.getLocation())) {
                            LivingEntity living = (LivingEntity) affectedEntity;

                            Vector targetVector = living.getLocation().toVector().subtract(holderPlayer.getLocation().toVector());
                            targetVector.setY(0);
                            if (targetVector.lengthSquared() == 0) continue;

                            targetVector = targetVector.normalize();
                            targetVector = targetVector.multiply(strength);
                            targetVector.setY(0.4);

                            living.setVelocity(targetVector);

                            affected++;
                        } else blocked = true;
                    }

                    if (affected == 0) {
                        holderPlayer.sendActionBar(
                                MMUtil.formatColors(blocked ? "<f>Burada kullanamazsın!" : "<f>Yakında hiç kimse yok!")
                        );
                        fired = false;
                    }
                    ParticleUtil.drawRadiusCircle(radius, holderPlayer, fired, ability);
                }
                case PULL -> {
                    int radius = ability.pullRadius();
                    double strength = ability.strength();
                    List<Entity> affectedEntities;

                    fired = true;
                    if (configManager.isAbilitiesFireOnPlayersOnly())
                        affectedEntities = new ArrayList<>(holderPlayer.getLocation().getNearbyPlayers(radius));
                    else
                        affectedEntities = new ArrayList<>(holderPlayer.getLocation().getNearbyEntities(radius, 4.0, radius));

                    int affected = 0;
                    boolean blocked = !PSwordMain.instance.canUse(holderPlayer, holderPlayer.getLocation());
                    boolean holderBlocked = blocked;

                    for (Entity affectedEntity : affectedEntities) {
                        if (!this.canAffect(holderPlayer, affectedEntity))
                            continue;

                        if (!holderBlocked && PSwordMain.instance.canUse(holderPlayer, affectedEntity.getLocation())) {
                            LivingEntity living = (LivingEntity) affectedEntity;

                            Vector targetVector = holderPlayer.getLocation().toVector().subtract(affectedEntity.getLocation().toVector());
                            targetVector.setY(0);
                            if (targetVector.lengthSquared() == 0) continue;

                            double distance = targetVector.length();
                            double speed = Math.min(distance * 0.1 * strength, 2.0);

                            targetVector = targetVector.normalize();
                            targetVector = targetVector.multiply(speed);
                            targetVector.setY(0.2);

                            living.setVelocity(targetVector);

                            affected++;
                        } else blocked = true;
                    }

                    if (affected == 0) {
                        holderPlayer.sendActionBar(
                                MMUtil.formatColors(blocked ? "<f>Burada kullanamazsın!" : "<f>Yakında hiç kimse yok!")
                        );
                        fired = false;
                    }
                    ParticleUtil.drawRadiusCircle(radius, holderPlayer, fired, ability);
                }
                case ELYTRA_LOCK -> {
                    int radius = ability.elytraLockRadius();
                    long durationMillis = ability.durationMillis();

                    List<Entity> affectedEntities;

                    fired = true;
                    if (configManager.isAbilitiesFireOnPlayersOnly())
                        affectedEntities = new ArrayList<>(holderPlayer.getLocation().getNearbyPlayers(radius));
                    else
                        affectedEntities = new ArrayList<>(holderPlayer.getLocation().getNearbyEntities(radius, 4.0, radius));

                    int affected = 0;
                    boolean blocked = !PSwordMain.instance.canUse(holderPlayer, holderPlayer.getLocation());

                    for (Entity affectedEntity : affectedEntities) {
                        if (!this.canAffect(holderPlayer, affectedEntity))
                            continue;

                        if (PSwordMain.instance.canUse(holderPlayer, affectedEntity.getLocation())) {
                            LivingEntity living = (LivingEntity) affectedEntity;

                            if (living instanceof Player) {
                                this.activeEffects.put(new ActiveEffectKey(affectedEntity.getUniqueId(), ability.abilityType()), System.currentTimeMillis() + durationMillis);
                                living.setGliding(false);
                                affected++;
                            }
                        } else blocked = true;
                    }

                    if (affected == 0) {
                        holderPlayer.sendActionBar(
                                MMUtil.formatColors(blocked ? "<f>Burada kullanamazsın!" : "<f>Yakında hiç kimse yok!")
                        );
                        fired = false;
                    }
                    ParticleUtil.drawRadiusCircle(radius, holderPlayer, fired, ability);
                }
                case SILENCE -> {
                    if (victimEntity instanceof Player victimPlayer) {
                        if (!this.canAffect(holderPlayer, victimPlayer))
                            return;

                        if (!PSwordMain.instance.canUse(holderPlayer, victimPlayer.getLocation())) {
                            holderPlayer.sendActionBar(MMUtil.formatColors("<f>Burada kullanamazsın!"));
                            return;
                        }

                        this.activeEffects.put(new ActiveEffectKey(victimPlayer.getUniqueId(), SwordAbilityType.SILENCE), System.currentTimeMillis() + ability.durationMillis());
                        ParticleUtil.highlightPlayer(victimPlayer);
                        fired = true;
                    }
                }

                case SILENCE_RADIUS -> {
                    fired = this.applyToTargets(ability, ability.silenceRadius(), holderPlayer, (living -> {
                        if (living instanceof Player victimPlayer) {
                            this.activeEffects.put(new ActiveEffectKey(living.getUniqueId(), SwordAbilityType.SILENCE), System.currentTimeMillis() + ability.durationMillis());
                            ParticleUtil.highlightPlayer(victimPlayer);
                            return true;
                        } else return false;
                    }));
                }
            }

            if (fired)
                cooldowns.put(cooldownKey, System.currentTimeMillis());
        }
    }

    public List<Entity> nearbyTargets(Player holderPlayer, int radius) {
        List<Entity> affectedEntities;
        if (configManager.isAbilitiesFireOnPlayersOnly())
            affectedEntities = new ArrayList<>(holderPlayer.getLocation().getNearbyPlayers(radius));
        else
            affectedEntities = new ArrayList<>(holderPlayer.getLocation().getNearbyEntities(radius, 4.0, radius));

        return affectedEntities;
    }

    public boolean applyToTargets(SwordAbility ability, int radius, Player holderPlayer, Predicate<LivingEntity> action) {
        List<Entity> affectedEntities = this.nearbyTargets(holderPlayer, radius);
        boolean fired = true;

        int affected = 0;
        boolean blocked = !PSwordMain.instance.canUse(holderPlayer, holderPlayer.getLocation());

        for (Entity affectedEntity : affectedEntities) {
            if (!this.canAffect(holderPlayer, affectedEntity))
                continue;

            if (PSwordMain.instance.canUse(holderPlayer, affectedEntity.getLocation())) {
                LivingEntity living = (LivingEntity) affectedEntity;

                if (action.test(living)) {
                    affected++;
                }

            } else blocked = true;
        }

        if (affected == 0) {
            holderPlayer.sendActionBar(
                    MMUtil.formatColors(blocked ? "<f>Burada kullanamazsın!" : "<f>Yakında hiç kimse yok!")
            );
            fired = false;
        }
        ParticleUtil.drawRadiusCircle(radius, holderPlayer, fired, ability);

        return fired;
    }

    public boolean canAffect(Player holder, Entity victim) {
        if (!(victim instanceof LivingEntity))
            return false;

        if (victim == holder)
            return false;

        if (!victim.isValid())
            return false;

        if (victim instanceof ArmorStand)
            return false;

        if (victim.hasMetadata("shopkeeper"))
            return false;

        if (victim instanceof Player victimPlayer && victimPlayer.getGameMode() == GameMode.SPECTATOR)
            return false;

        return true;
    }

    public boolean isActive(UUID uuid, SwordAbilityType abilityType) {
        ActiveEffectKey key = new ActiveEffectKey(uuid, abilityType);
        Long millis = activeEffects.get(key);

        if (millis == null)
            return false;

        if (System.currentTimeMillis() > millis) {
            activeEffects.remove(key);
            return false;
        }

        return true;
    }
}
