package com.ebicep.warlords.pve.upgrades.arcanist.luminary;

import com.ebicep.warlords.abilities.CrystalOfHealing;
import com.ebicep.warlords.pve.upgrades.AbilityTree;
import com.ebicep.warlords.pve.upgrades.AbstractUpgradeBranch;
import com.ebicep.warlords.pve.upgrades.Upgrade;

public class CrystalOfHealingBranch extends AbstractUpgradeBranch<CrystalOfHealing> {

    float maxHeal = ability.getMaxHeal();
    int lifeSpan = ability.getLifeSpan();

    public CrystalOfHealingBranch(AbilityTree abilityTree, CrystalOfHealing ability) {
        super(abilityTree, ability);
        if (abilityTree.getWarlordsPlayer().isInPve()) {
            ability.setDuration(5);
        }

        treeA.add(new Upgrade(
                "Alleviate - Tier I",
                "+125 Max health",
                5000,
                () -> {
                    ability.setMaxHeal(maxHeal + 125);
                }
        ));
        treeA.add(new Upgrade(
                "Alleviate - Tier II",
                "+250 Max health",
                10000,
                () -> {
                    ability.setMaxHeal(maxHeal + 250);
                }
        ));
        treeA.add(new Upgrade(
                "Alleviate - Tier III",
                "+375 Max health",
                15000,
                () -> {
                    ability.setMaxHeal(maxHeal + 375);
                }
        ));
        treeA.add(new Upgrade(
                "Alleviate - Tier IV",
                "+500 Max health",
                20000,
                () -> {
                    ability.setMaxHeal(maxHeal + 500);
                }
        ));


        treeB.add(new Upgrade(
                "Chronos - Tier I",
                "+5s Lifespan",
                5000,
                () -> {
                    ability.setLifeSpan(lifeSpan + 5);
                }
        ));
        treeB.add(new Upgrade(
                "Chronos - Tier II",
                "+10s Lifespan",
                10000,
                () -> {
                    ability.setLifeSpan(lifeSpan + 10);
                }
        ));
        treeB.add(new Upgrade(
                "Chronos - Tier III",
                "+15s Lifespan",
                15000,
                () -> {
                    ability.setLifeSpan(lifeSpan + 15);
                }
        ));
        treeB.add(new Upgrade(
                "Chronos - Tier IV",
                "+20s Lifespan",
                20000,
                () -> {
                    ability.setLifeSpan(lifeSpan + 20);
                }
        ));

        masterUpgrade = new Upgrade(
                "Lucid Healing",
                "Crystal of Healing - Master Upgrade",
                """
                        Crystal of Healing provide 50 healing per second for allies within 6 blocks.
                        """,
                50000,
                () -> {

                }
        );
    }

}