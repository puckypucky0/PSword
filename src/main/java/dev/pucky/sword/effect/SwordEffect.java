package dev.pucky.sword.effect;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.bukkit.Particle;
import org.bukkit.Sound;

@EqualsAndHashCode
@Builder
public class SwordEffect {
    public SwordEffectType effectType;

    // Particle
    public Particle particleType;
    public double particleMultiplier;

    // Sound
    public Sound sound;
    public double volume;
    public double pitch;

    // Lightning has no options.
}
