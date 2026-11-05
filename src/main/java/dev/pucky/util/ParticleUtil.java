package dev.pucky.util;

import dev.pucky.particle.Gradient;
import dev.pucky.particle.ParticleEffect;
import dev.pucky.particle.ParticleEngine;
import dev.pucky.particle.Shapes;
import dev.pucky.sword.ability.SwordAbility;
import org.bukkit.entity.Player;

public class ParticleUtil {
    public static void drawRadiusCircle(int radius, Player holderPlayer, boolean fired, SwordAbility ability) {
        ParticleEffect.ParticleEffectBuilder effectBuilder = ParticleEffect.builder()
                .shape(Shapes.circle(radius, 48 * radius, Shapes.Axis.Y))
                .origin(holderPlayer::getLocation)
                .durationTicks(3);

        if (fired) {
            if (ability.successRadiusHighlightEnabled()) {
                effectBuilder.gradient(
                        Gradient.of(ability.successRadiusHighlightColors().toArray(String[]::new))
                );
                ParticleEngine.submit(effectBuilder.build());
            }
        } else {
            if (ability.failRadiusHighlightEnabled()) {
                effectBuilder.gradient(
                        Gradient.of(ability.failRadiusHighlightColors().toArray(String[]::new))
                );
                ParticleEngine.submit(effectBuilder.build());
            }
        }
    }
}
