package com.ebicep.warlords.pve.upgrades;

import com.ebicep.warlords.abilties.Inferno;
import org.bukkit.inventory.ItemStack;

public class InfernoUpgradeBranch extends UpgradeBranch<Inferno> {

    public InfernoUpgradeBranch(AbilityTree abilityTree, Inferno ability, ItemStack itemStack) {
        super(abilityTree, ability, itemStack);
        upgrades.add(new Upgrade("Tier 1", "+10% Critical Chance"));
        upgrades.add(new Upgrade("Tier 2", "+20% Critical Damage"));
        upgrades.add(new Upgrade("Tier 3", "+10% Critical Chance"));
        upgrades.add(new Upgrade("Tier 4", "+20% Critical Damage"));
        upgrades.add(new Upgrade("Tier 5", "infERNo"));
    }

    @Override
    public void tierOneUpgrade() {
        ability.setCritChanceIncrease(ability.getCritChanceIncrease() + 10);
    }

    @Override
    public void tierTwoUpgrade() {
        ability.setCritMultiplierIncrease(ability.getCritMultiplierIncrease() + 20);
    }

    @Override
    public void tierThreeUpgrade() {
        ability.setCritChanceIncrease(ability.getCritChanceIncrease() + 10);
    }

    @Override
    public void tierFourUpgrade() {
        ability.setCritMultiplierIncrease(ability.getCritMultiplierIncrease() + 20);
    }

    @Override
    public void tierFiveUpgrade() {
        //0 energy cost somethinbg
    }

}