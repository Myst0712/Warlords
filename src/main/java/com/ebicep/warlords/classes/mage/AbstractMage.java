package com.ebicep.warlords.classes.mage;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.classes.AbstractPlayerClass;

public abstract class AbstractMage extends AbstractPlayerClass {

    public AbstractMage(int maxHealth, int maxEnergy, int energyPerSec, int energyOnHit, int damageResistance, AbstractAbility weapon, AbstractAbility red, AbstractAbility purple, AbstractAbility blue, AbstractAbility orange) {
        super(maxHealth, maxEnergy, energyPerSec, energyOnHit, damageResistance, weapon, red, purple, blue, orange);
    }
}
