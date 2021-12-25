package com.ebicep.warlords.database.repositories.player.pojos;

import com.ebicep.warlords.player.ArmorManager;
import com.ebicep.warlords.player.ClassesSkillBoosts;

public class DatabaseMage extends DatabaseWarlordsClass {

    protected DatabaseSpecialization pyromancer = new DatabaseSpecialization(ClassesSkillBoosts.FIREBALL);
    protected DatabaseSpecialization cryomancer = new DatabaseSpecialization(ClassesSkillBoosts.FROST_BOLT);
    protected DatabaseSpecialization aquamancer = new DatabaseSpecialization(ClassesSkillBoosts.WATER_BOLT);
    protected ArmorManager.Helmets helmet = ArmorManager.Helmets.SIMPLE_MAGE_HELMET;
    protected ArmorManager.ArmorSets armor = ArmorManager.ArmorSets.SIMPLE_CHESTPLATE_MAGE;

    public DatabaseMage() {
        super();
    }

    @Override
    public DatabaseSpecialization[] getSpecs() {
        return new DatabaseSpecialization[]{pyromancer, cryomancer, aquamancer};
    }

    public DatabaseSpecialization getPyromancer() {
        return pyromancer;
    }

    public DatabaseSpecialization getCryomancer() {
        return cryomancer;
    }

    public DatabaseSpecialization getAquamancer() {
        return aquamancer;
    }

    public ArmorManager.Helmets getHelmet() {
        return helmet;
    }

    public ArmorManager.ArmorSets getArmor() {
        return armor;
    }

    public void setHelmet(ArmorManager.Helmets helmet) {
        this.helmet = helmet;
    }

    public void setArmor(ArmorManager.ArmorSets armor) {
        this.armor = armor;
    }
}
