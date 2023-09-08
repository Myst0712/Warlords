package com.ebicep.warlords.pve.mobs.slime;

import com.ebicep.warlords.abilities.internal.AbstractAbility;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.pve.mobs.tiers.AdvancedMob;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;

public class SlimyChess extends AbstractSlime implements AdvancedMob {
    public SlimyChess(Location spawnLocation) {
        super(
                spawnLocation,
                "Slimy Chess",
                null,
                10000,
                0.1f,
                30,
                0,
                0,
                new Blob()
        );
    }

    public SlimyChess(
            Location spawnLocation,
            String name,
            int maxHealth,
            float walkSpeed,
            int damageResistance,
            float minMeleeDamage,
            float maxMeleeDamage
    ) {
        super(
                spawnLocation,
                name,
                null,
                maxHealth,
                walkSpeed,
                damageResistance,
                minMeleeDamage,
                maxMeleeDamage,
                new Blob()
        );
    }

    @Override
    public void onSpawn(PveOption option) {
        super.onSpawn(option);
        this.entity.get().setSize(10, true);
    }

    @Override
    public void whileAlive(int ticksElapsed, PveOption option) {

    }

    @Override
    public void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, WarlordsDamageHealingEvent event) {

    }

    @Override
    public void onDamageTaken(WarlordsEntity self, WarlordsEntity attacker, WarlordsDamageHealingEvent event) {
        //attacker.getSpec().increaseAllCooldownTimersBy(1);
    }

    private static class Blob extends AbstractAbility {

        public Blob() {
            super("Blob", 1, 50);
        }

        @Override
        public void updateDescription(Player player) {

        }

        @Override
        public List<Pair<String, String>> getAbilityInfo() {
            return null;
        }

        @Override
        public boolean onActivate(@Nonnull WarlordsEntity wp, Player player) {
            wp.subtractEnergy(energyCost, false);

            for (WarlordsEntity we : PlayerFilter
                    .entitiesAround(wp, 10, 10, 10)
                    .aliveEnemiesOf(wp)
            ) {
                we.subtractEnergy(10, true);
            }
            for (WarlordsEntity we : PlayerFilter
                    .entitiesAround(wp, 100, 100, 100)
                    .aliveEnemiesOf(wp)
                    .closestFirst(wp)
                    .limit(1)
            ) {
                EffectUtils.playParticleLinkAnimation(wp.getLocation(), we.getLocation(), Particle.DRIP_LAVA);
                we.subtractEnergy(5, true);
                we.addSpeedModifier(wp, "Blob Slowness", -20, 20);
            }
            return true;
        }
    }
}