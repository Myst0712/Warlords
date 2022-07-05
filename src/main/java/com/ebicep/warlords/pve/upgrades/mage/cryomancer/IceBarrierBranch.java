package com.ebicep.warlords.pve.upgrades.mage.cryomancer;

import com.ebicep.warlords.abilties.IceBarrier;
import com.ebicep.warlords.pve.upgrades.AbilityTree;
import com.ebicep.warlords.pve.upgrades.AbstractUpgradeBranch;
import com.ebicep.warlords.pve.upgrades.Upgrade;

public class IceBarrierBranch extends AbstractUpgradeBranch<IceBarrier> {

    public IceBarrierBranch(AbilityTree abilityTree, IceBarrier ability) {
        super(abilityTree, ability);
        treeA.add(new Upgrade("Duration - Tier I", "+2s Duration", 5000));
        treeA.add(new Upgrade("Duration - Tier II", "+4s Duration", 10000));
        treeA.add(new Upgrade("Duration - Tier III", "+6s Duration", 20000));

        treeC.add(new Upgrade("Defense - Tier I", "-10% Damage taken", 5000));
        treeC.add(new Upgrade("Defense - Tier II", "-20% Damage taken", 10000));
        treeC.add(new Upgrade("Defense - Tier III", "-40% Damage taken", 20000));

        masterUpgrade = new Upgrade(
                "Master Upgrade",
                "-15% Cooldown reduction\n\nEnvelop the player with a frosty aura, slowing enemies\nthat come within a 6 block radius of the player. Taunts\nenemies on activation.",
                500000
        );
    }

    int duration = ability.getDuration();
    @Override
    public void a1() {
        ability.setDuration(duration + 2);
    }

    @Override
    public void a2() {
        ability.setDuration(duration + 4);
    }

    @Override
    public void a3() {
        ability.setDuration(duration + 6);
    }

    @Override
    public void b1() {

    }

    @Override
    public void b2() {

    }

    @Override
    public void b3() {

    }

    int damageReductionPercent = ability.getDamageReductionPercent();

    @Override
    public void c1() {
        ability.setDamageReductionPercent(damageReductionPercent + 10);
    }

    @Override
    public void c2() {
        ability.setDamageReductionPercent(damageReductionPercent + 20);
    }

    @Override
    public void c3() {
        ability.setDamageReductionPercent(damageReductionPercent + 40);
    }
    @Override
    public void master() {
        ability.setCooldown(ability.getCooldown() * 0.85f);
    }
}