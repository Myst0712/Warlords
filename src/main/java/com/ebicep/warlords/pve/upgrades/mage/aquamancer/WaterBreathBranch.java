package com.ebicep.warlords.pve.upgrades.mage.aquamancer;

import com.ebicep.warlords.abilties.WaterBreath;
import com.ebicep.warlords.pve.upgrades.AbilityTree;
import com.ebicep.warlords.pve.upgrades.AbstractUpgradeBranch;
import com.ebicep.warlords.pve.upgrades.Upgrade;

public class WaterBreathBranch extends AbstractUpgradeBranch<WaterBreath> {

    public WaterBreathBranch(AbilityTree abilityTree, WaterBreath ability) {
        super(abilityTree, ability);
        treeA.add(new Upgrade("Cooldown - Tier I", "-10% Cooldown Reduction", 5000));
        treeA.add(new Upgrade("Cooldown - Tier II", "-20% Cooldown Reduction", 10000));
        treeA.add(new Upgrade("Cooldown - Tier III", "-40% Cooldown Reduction", 20000));

        treeB.add(new Upgrade("Utility - Tier I", "+10% Knockback", 5000));
        treeB.add(new Upgrade("Utility - Tier II", "+25% Knockback", 10000));
        treeB.add(new Upgrade("Utility - Tier III", "+50% Knockback", 20000));

        treeC.add(new Upgrade("Healing - Tier I", "+10% Healing", 5000));
        treeC.add(new Upgrade("Healing - Tier II", "+20% Healing", 10000));
        treeC.add(new Upgrade("Healing - Tier III", "+40% Healing", 20000));

        masterUpgrade = new Upgrade(
                "Master Upgrade",
                "+30% Cone Range\n\nAll allies hit by Water Breath gain\n1% max health per second for 5 seconds.",
                50000
        );
    }

    float cooldown = ability.getCooldown();

    @Override
    public void a1() {
        ability.setCooldown(cooldown * 0.9f);
    }

    @Override
    public void a2() {
        ability.setCooldown(cooldown * 0.8f);
    }

    @Override
    public void a3() {
        ability.setCooldown(cooldown * 0.6f);
    }

    @Override
    public void b1() {
        ability.setVelocity(1.2);
    }

    @Override
    public void b2() {
        ability.setVelocity(1.35);
    }

    @Override
    public void b3() {
        ability.setVelocity(1.6);
    }

    float minHealing = ability.getMinDamageHeal();
    float maxHealing = ability.getMaxDamageHeal();

    @Override
    public void c1() {
        ability.setMinDamageHeal(minHealing * 1.1f);
        ability.setMaxDamageHeal(maxHealing * 1.1f);
    }

    @Override
    public void c2() {
        ability.setMinDamageHeal(minHealing * 1.2f);
        ability.setMaxDamageHeal(maxHealing * 1.2f);
    }

    @Override
    public void c3() {
        ability.setMinDamageHeal(minHealing * 1.4f);
        ability.setMaxDamageHeal(maxHealing * 1.4f);
    }

    @Override
    public void master() {
        ability.setHitbox(15);
        ability.setMaxAnimationTime(24);
        ability.setPveUpgrade(true);
    }
}