package com.ebicep.warlords.pve.upgrades.paladin.avenger;

import com.ebicep.warlords.abilties.AvengersStrike;
import com.ebicep.warlords.pve.upgrades.AbilityTree;
import com.ebicep.warlords.pve.upgrades.AbstractUpgradeBranch;
import com.ebicep.warlords.pve.upgrades.Upgrade;

public class AvengerStrikeBranch extends AbstractUpgradeBranch<AvengersStrike> {

    float minDamage = ability.getMinDamageHeal();
    float maxDamage = ability.getMaxDamageHeal();
    float energyCost = ability.getEnergyCost();
    float energySteal = ability.getEnergySteal();
    double hitbox = ability.getHitbox();

    public AvengerStrikeBranch(AbilityTree abilityTree, AvengersStrike ability) {
        super(abilityTree, ability);

        treeA.add(new Upgrade(
                "Impair - Tier I",
                "+5% Damage\n+2.5 Energy steal",
                5000,
                () -> {
                    ability.setMinDamageHeal(minDamage * 1.05f);
                    ability.setMaxDamageHeal(maxDamage * 1.05f);
                    ability.setEnergySteal(energySteal + 2.5f);
                }
        ));
        treeA.add(new Upgrade(
                "Impair - Tier II",
                "+10% Damage\n+5 Energy steal",
                10000,
                () -> {
                    ability.setMinDamageHeal(minDamage * 1.1f);
                    ability.setMaxDamageHeal(maxDamage * 1.1f);
                    ability.setEnergySteal(energySteal + 5);
                }
        ));
        treeA.add(new Upgrade(
                "Impair - Tier III",
                "+15% Damage\n+7.5 Energy steal",
                15000,
                () -> {
                    ability.setMinDamageHeal(minDamage * 1.15f);
                    ability.setMaxDamageHeal(maxDamage * 1.15f);
                    ability.setEnergySteal(energySteal + 7.5f);
                }
        ));
        treeA.add(new Upgrade(
                "Impair - Tier IV",
                "+20% Damage\n+10 Energy steal",
                20000,
                () -> {
                    ability.setMinDamageHeal(minDamage * 1.2f);
                    ability.setMaxDamageHeal(maxDamage * 1.2f);
                    ability.setEnergySteal(energySteal + 10);
                }
        ));

        treeB.add(new Upgrade(
                "Spark - Tier I",
                "-2.5 Energy cost\n+0.25 Blocks hit radius",
                5000,
                () -> {
                    ability.setEnergyCost(energyCost - 2.5f);
                    ability.setHitbox(hitbox + 0.25);
                }
        ));
        treeB.add(new Upgrade(
                "Spark - Tier II",
                "-5 Energy cost\n+0.5 Blocks hit radius",
                10000,
                () -> {
                    ability.setEnergyCost(energyCost - 5);
                    ability.setHitbox(hitbox + 0.5);
                }
        ));
        treeB.add(new Upgrade(
                "Spark - Tier III",
                "-7.5 Energy cost\n+0.75 Blocks hit radius",
                15000,
                () -> {
                    ability.setEnergyCost(energyCost - 7.5f);
                    ability.setHitbox(hitbox + 0.75);
                }
        ));
        treeB.add(new Upgrade(
                "Spark - Tier IV",
                "-10 Energy cost\n+1 Blocks hit radius",
                20000,
                () -> {
                    ability.setEnergyCost(energyCost - 10);
                    ability.setHitbox(hitbox + 1);
                }
        ));

        masterUpgrade = new Upgrade(
                "Avenger's Slash",
                "Avenger's Strike - Master Upgrade",
                "-5 Additional energy cost.\n\nDeal 30% more damage against BASIC enemies\nand 15% more damage against ELITE enemies.",
                50000,
                () -> {
                    ability.setPveUpgrade(true);
                    ability.setEnergyCost(ability.getEnergyCost() - 5);
                }
        );
    }
}
