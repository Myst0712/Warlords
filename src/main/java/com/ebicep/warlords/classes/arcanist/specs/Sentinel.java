package com.ebicep.warlords.classes.arcanist.specs;

import com.ebicep.warlords.abilties.*;
import com.ebicep.warlords.classes.arcanist.AbstractArcanist;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.pve.upgrades.AbilityTree;
import com.ebicep.warlords.pve.upgrades.AbstractUpgradeBranch;

import java.util.List;

public class Sentinel extends AbstractArcanist {

    public Sentinel() {
        super(
                "Guardian",
                6000,
                305,
                20,
                14,
                15,
                new FortifyingHex(),
                new GuardianBeam(),
                new EnergySeerSentinel(),
                new MysticalBarrier(),
                new Sanctuary()
        );
    }

    @Override
    public void setUpgradeBranches(WarlordsPlayer wp) {
        AbilityTree abilityTree = wp.getAbilityTree();
        List<AbstractUpgradeBranch<?>> branch = abilityTree.getUpgradeBranches();

    }
}
