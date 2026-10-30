package dev.pucky.sword.ability;

import dev.pucky.sword.Trigger;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.potion.PotionEffect;

import java.util.List;

@Builder @Getter @Accessors(fluent = true)
public class SwordAbility {
    private Component abilityName;
    private SwordAbilityType abilityType;
    private Trigger abilityTrigger;
    private int cooldownMillis;

    private boolean successRadiusHighlightEnabled;
    private boolean failRadiusHighlightEnabled;

    private List<String> successRadiusHighlightColors;
    private List<String> failRadiusHighlightColors;

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
