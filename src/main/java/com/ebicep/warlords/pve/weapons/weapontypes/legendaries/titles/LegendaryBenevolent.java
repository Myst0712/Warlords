package com.ebicep.warlords.pve.weapons.weapontypes.legendaries.titles;

import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.AbstractLegendaryWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.LegendaryTitles;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class LegendaryBenevolent extends AbstractLegendaryWeapon {

    private static final int HEALING_INCREASE = 20;
    private static final float HEALING_INCREASE_PER_UPGRADE = 7.5f;

    public LegendaryBenevolent() {
    }

    public LegendaryBenevolent(UUID uuid) {
        super(uuid);
    }

    public LegendaryBenevolent(AbstractLegendaryWeapon legendaryWeapon) {
        super(legendaryWeapon);
    }

    @Override
    public String getPassiveEffect() {
        return "Increase healing provided by " + formatTitleUpgrade(HEALING_INCREASE + HEALING_INCREASE_PER_UPGRADE * getTitleLevel()) + "%" + ChatColor.GRAY + ".";
    }

    @Override
    protected float getMeleeDamageMaxValue() {
        return 140;
    }

    @Override
    public void applyToWarlordsPlayer(WarlordsPlayer player) {
        super.applyToWarlordsPlayer(player);

        player.getGame().registerEvents(new Listener() {
            @EventHandler
            public void onEvent(WarlordsDamageHealingEvent event) {
                if (event.isHealingInstance() && event.getAttacker().equals(player)) {
                    float healingIncrease = 1 + (HEALING_INCREASE + HEALING_INCREASE_PER_UPGRADE * getTitleLevel()) / 100f;
                    event.setMin(event.getMin() * healingIncrease);
                    event.setMax(event.getMax() * healingIncrease);
                }
            }
        });
    }

    @Override
    public LegendaryTitles getTitle() {
        return LegendaryTitles.BENEVOLENT;
    }

    @Override
    protected float getMeleeDamageMinValue() {
        return 120;
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
        return 800;
    }

    @Override
    protected float getSpeedBonusValue() {
        return 10;
    }

    @Override
    protected float getSkillCritChanceBonusValue() {
        return 5;
    }

    @Override
    protected float getSkillCritMultiplierBonusValue() {
        return 5;
    }
}
