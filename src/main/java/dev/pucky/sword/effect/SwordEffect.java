package dev.pucky.sword.effect;

import dev.pucky.sword.Trigger;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.bukkit.Particle;
import org.bukkit.Sound;

@EqualsAndHashCode
@Builder
public class SwordEffect {
    public SwordEffectType effectType;
    public Trigger effectTrigger;

    // Particle
    public Particle particleType;
    public double particleMultiplier;
    // NEW: Dust particle data
    public Particle.DustOptions dustOptions;

    // Sound
    public Sound sound;
    public double volume;
    public double pitch;

    // Lightning has no options.
}
