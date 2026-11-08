package dev.pucky.util;

import dev.pucky.particle.Gradient;
import dev.pucky.particle.ParticleEffect;
import dev.pucky.particle.ParticleEngine;
import dev.pucky.particle.Shapes;
import dev.pucky.sword.ability.SwordAbility;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

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

    public static void highlightPlayer(Player player) {
        BoundingBox box = player.getBoundingBox();

        ParticleEngine.submit(ParticleEffect.builder()
                .shape(Shapes.box(new Vector(box.getWidthX(), box.getHeight(), box.getWidthZ()), 0.15))
                .origin(() -> player.getBoundingBox().getCenter().toLocation(player.getWorld()))
                .gradient(Gradient.of("#E0705A", "#FFD27A"))
                .durationTicks(5)
                .owner(player)
                .build());
    }
}
