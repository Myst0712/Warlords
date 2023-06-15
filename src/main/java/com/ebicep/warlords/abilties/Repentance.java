package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.abilties.internal.Duration;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Repentance extends AbstractAbility implements Duration {

    private int tickDuration = 240;
    private int healthRestore = 150;
    private int energyRestore = 4;
    private int maxProcs = 15;
    private int damageDealtTaken = 2000;

    public Repentance() {
        super("Repentance", 0, 0, 31.32f, 20);
    }

    @Override
    public void updateDescription(Player player) {
        description = Component.text("During the duration of Repentance every ")
                               .append(Component.text(damageDealtTaken, NamedTextColor.RED))
                               .append(Component.text(" damage you deal and take will heal you for "))
                               .append(Component.text(healthRestore, NamedTextColor.GREEN))
                               .append(Component.text(" health and restore "))
                               .append(Component.text(energyRestore, NamedTextColor.YELLOW))
                               .append(Component.text(" energy. Can proc up to "))
                               .append(Component.text(maxProcs, NamedTextColor.GOLD))
                               .append(Component.text(" times. Lasts "))
                               .append(Component.text(format(tickDuration / 20f), NamedTextColor.GOLD))
                               .append(Component.text(" seconds."));
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        List<Pair<String, String>> info = new ArrayList<>();
        info.add(new Pair<>("Times Used", "" + timesUsed));

        return info;
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsEntity wp, @Nonnull Player player) {
        wp.subtractEnergy(energyCost, false);
        Utils.playGlobalSound(player.getLocation(), "paladin.barrieroflight.impact", 2, 1.35f);
        EffectUtils.playCylinderAnimation(player, 1, 255, 255, 255);


        wp.getCooldownManager().addCooldown(new RegularCooldown<>(
                name, "REPE",
                Repentance.class,
                new Repentance(),
                wp,
                CooldownTypes.ABILITY,
                cooldownManager -> {
                },
                tickDuration
        ) {
            private float damageCounter = 0;
            private int procs = 0;

            @Override
            public boolean distinct() {
                return true;
            }

            @Override
            public void onDamageFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue, boolean isCrit) {
                addToCounter(currentDamageValue);
            }

            @Override
            public void onDamageFromAttacker(WarlordsDamageHealingEvent event, float currentDamageValue, boolean isCrit) {
                addToCounter(currentDamageValue);
            }

            private void addToCounter(float amount) {
                if (procs > 10) {
                    return;
                }
                damageCounter += amount;
                if (damageCounter >= damageDealtTaken) {
                    // for if you deal/take like 6000 dmg, heal/energy should be 3x
                    int times = (int) (damageCounter / damageDealtTaken);
                    int validTimes = 0;
                    for (int i = 0; i < times; i++) {
                        procs++;
                        if (procs > 10) {
                            break;
                        }
                        damageCounter -= damageDealtTaken;
                        validTimes++;
                    }
                    int healthGain = healthRestore;
                    int energyGain = Repentance.this.energyRestore;
                    wp.addHealingInstance(wp, "Repentance", healthGain * validTimes, healthGain * validTimes, 0, 100, false, false);
                    wp.addEnergy(wp, "Repentance", energyGain * validTimes);
                }
            }
        });

        return true;
    }

    @Override
    public int getTickDuration() {
        return tickDuration;
    }

    @Override
    public void setTickDuration(int tickDuration) {
        this.tickDuration = tickDuration;
    }

    public int getHealthRestore() {
        return healthRestore;
    }

    public void setHealthRestore(int healthRestore) {
        this.healthRestore = healthRestore;
    }

    public int getEnergyRestore() {
        return energyRestore;
    }

    public void setEnergyRestore(int energyRestore) {
        this.energyRestore = energyRestore;
    }

    public int getMaxProcs() {
        return maxProcs;
    }

    public void setMaxProcs(int maxProcs) {
        this.maxProcs = maxProcs;
    }

    public int getDamageDealtTaken() {
        return damageDealtTaken;
    }

    public void setDamageDealtTaken(int damageDealtTaken) {
        this.damageDealtTaken = damageDealtTaken;
    }
}
