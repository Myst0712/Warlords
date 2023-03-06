package com.ebicep.warlords.pve.weapons.weapontypes.legendaries.titles;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.AbstractLegendaryWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.LegendaryTitles;
import com.ebicep.warlords.util.java.Pair;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.springframework.data.annotation.Transient;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LegendaryVigorous extends AbstractLegendaryWeapon {

    public static final int EPS = 20;
    public static final int EPS_PER_UPGRADE = 5;
    public static final int DURATION = 10;

    @Transient
    private LegendaryVigorousAbility ability;

    public LegendaryVigorous() {
    }

    public LegendaryVigorous(UUID uuid) {
        super(uuid);
    }

    public LegendaryVigorous(AbstractLegendaryWeapon legendaryWeapon) {
        super(legendaryWeapon);
    }

    @Override
    public String getPassiveEffect() {
        return formatTitleUpgrade("+", EPS + EPS_PER_UPGRADE * getTitleLevel()) +
                " energy per second for " + DURATION + " seconds. Can be triggered every 30 seconds.";
    }

    @Override
    public List<Pair<String, String>> getPassiveEffectUpgrade() {
        return Collections.singletonList(new Pair<>(
                formatTitleUpgrade("+", EPS + EPS_PER_UPGRADE * getTitleLevel()),
                formatTitleUpgrade("+", EPS + EPS_PER_UPGRADE * getTitleLevelUpgraded())
        ));
    }

    @Override
    protected float getMeleeDamageMaxValue() {
        return 170;
    }

    @Override
    public LegendaryTitles getTitle() {
        return LegendaryTitles.VIGOROUS;
    }

    @Override
    public LegendaryVigorousAbility getAbility() {
        return ability;
    }

    @Override
    public void resetAbility() {
        ability = new LegendaryVigorousAbility(EPS + EPS_PER_UPGRADE * getTitleLevel());
    }

    @Override
    protected float getMeleeDamageMinValue() {
        return 140;
    }

    @Override
    protected float getCritChanceValue() {
        return 20;
    }

    @Override
    protected float getCritMultiplierValue() {
        return 180;
    }

    @Override
    protected float getHealthBonusValue() {
        return 600;
    }

    @Override
    protected float getSpeedBonusValue() {
        return 10;
    }

    @Override
    protected float getEnergyPerSecondBonusValue() {
        return 2;
    }

    static class LegendaryVigorousAbility extends AbstractAbility {

        private final float energyPerSecond;

        public LegendaryVigorousAbility(float energyPerSecond) {
            super("Vigorous", 0, 0, 30, 0);
            this.energyPerSecond = energyPerSecond;
        }

        @Override
        public void updateDescription(Player player) {
            description = ChatColor.YELLOW + "+" + DECIMAL_FORMAT_TITLE.format(energyPerSecond) + ChatColor.GRAY + " energy per second for " + ChatColor.GOLD + "10 " + ChatColor.GRAY + "seconds.";
        }

        @Override
        public List<Pair<String, String>> getAbilityInfo() {
            return null;
        }

        @Override
        public boolean onActivate(@Nonnull WarlordsEntity wp, @Nonnull Player player) {
            wp.getCooldownManager().addCooldown(new RegularCooldown<>(
                    "LegendaryVigorous",
                    "VIGOR",
                    LegendaryVigorous.class,
                    null,
                    wp,
                    CooldownTypes.ABILITY,
                    cooldownManager -> {
                    },
                    DURATION * 20
            ) {
                @Override
                public float addEnergyGainPerTick(float energyGainPerTick) {
                    return energyGainPerTick + (energyPerSecond / 20);
                }
            });
            return true;
        }

    }
}

