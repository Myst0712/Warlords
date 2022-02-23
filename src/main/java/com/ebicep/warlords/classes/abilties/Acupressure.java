package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.effects.FallingBlockWaveEffect;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.util.*;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class Acupressure extends AbstractAbility {

    private final int acuRange = 8;
    private final int duration = 3;
    private final float minWaveHealing = 268;
    private final float maxWaveHealing = 324;

    public Acupressure() {
        super("Acupressure", 359, 485, 12, 30, 25, 175);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Discharge a shockwave of special potions\n" +
                "§7around you, healing everyone in the range for\n" +
                "§a" + format(minDamageHeal) + " §7- §a" + format(maxDamageHeal) + " §7health." +
                "\n\n" +
                "§7Each enemy afflicted with your §aLEECH §7effect\n" +
                "§7within the range will cause the enemy\n" +
                "§7to discharge an additional healing wave\n" +
                "§7that heals nearby allies for §a" + format(minWaveHealing) + " §7- §a" + format(maxWaveHealing) + " §7health\n" +
                "§7and increase their energy regeneration by\n" +
                "§e30 §7for §6" + duration + " §7seconds.";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        Acupressure tempAcupressure = new Acupressure();
        wp.addHealingInstance(
                wp,
                name,
                minDamageHeal,
                maxDamageHeal,
                critChance,
                critMultiplier,
                false,
                false
        );

        Utils.playGlobalSound(player.getLocation(), "shaman.chainlightning.impact", 2, 0.1f);
        Utils.playGlobalSound(player.getLocation(), Sound.BLAZE_DEATH, 2, 0.6f);

        new FallingBlockWaveEffect(player.getLocation(), acuRange, 1, Material.DEAD_BUSH, (byte) 0).play();

        for (WarlordsPlayer acuTarget : PlayerFilter
                .entitiesAround(player, acuRange, acuRange, acuRange)
                .aliveTeammatesOfExcludingSelf(wp)
        ) {
            acuTarget.addHealingInstance(
                    wp,
                    name,
                    minDamageHeal,
                    maxDamageHeal,
                    critChance,
                    critMultiplier,
                    false,
                    false
            );
        }

        for (WarlordsPlayer enemyTarget : PlayerFilter
                .entitiesAround(player, acuRange, acuRange, acuRange)
                .aliveEnemiesOf(wp)
        ) {
            if (enemyTarget.getCooldownManager().hasCooldown(ImpalingStrike.class)) {

                Utils.playGlobalSound(enemyTarget.getLocation(), Sound.GLASS, 2, 0.6f);

                new GameRunnable(wp.getGame()) {
                    @Override
                    public void run() {
                        for (WarlordsPlayer allyTarget : PlayerFilter
                                .entitiesAround(player, 6, 6, 6)
                                .aliveTeammatesOf(wp)
                        ) {
                            allyTarget.addHealingInstance(
                                    wp,
                                    name,
                                    minWaveHealing,
                                    maxWaveHealing,
                                    critChance,
                                    critMultiplier,
                                    false,
                                    false
                            );
                            allyTarget.getCooldownManager().removeCooldown(Acupressure.class);
                            allyTarget.getCooldownManager().addRegularCooldown(
                                    "Acupressure",
                                    "ACU",
                                    Acupressure.class,
                                    tempAcupressure,
                                    wp,
                                    CooldownTypes.BUFF,
                                    cooldownManager -> {},
                                    duration * 20
                            );
                        }
                    }
                }.runTaskLater(5);

                FireWorkEffectPlayer.playFirework(enemyTarget.getLocation(), FireworkEffect.builder()
                        .withColor(Color.ORANGE)
                        .with(FireworkEffect.Type.STAR)
                        .build());

                new GameRunnable(wp.getGame()) {
                    @Override
                    public void run() {
                        if (wp.getCooldownManager().hasCooldown(tempAcupressure)) {
                            EffectUtils.playParticleLinkAnimation(wp.getLocation(), enemyTarget.getLocation(), 255, 170, 0, 1);
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(0, 5);
            }
        }

        return true;
    }

}
