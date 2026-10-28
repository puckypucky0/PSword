package dev.pucky.sword.ability;

import dev.pucky.sword.Trigger;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.potion.PotionEffect;

@Builder @Getter @Accessors(fluent = true)
public class SwordAbility {
    private Component abilityName;
    private SwordAbilityType abilityType;
    private Trigger abilityTrigger;
    private int cooldownMillis;

    // Effect
    private PotionEffect selfEffect;
    private PotionEffect enemyEffect;
    private Integer enemyEffectRadius;

    // Lightning
    private double lightningDamage;

    // Radius lightning
    private double radiusLightningDamage;
    private Integer lightningRadius;

    // Life steal
    private double ratio;

    // Blink
    private double blinkRange;

    // Dash
    private double dashRange;

}
