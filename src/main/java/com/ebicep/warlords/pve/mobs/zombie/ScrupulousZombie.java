package com.ebicep.warlords.pve.mobs.zombie;

import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.general.Weapons;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.pve.mobs.tiers.AdvancedMob;
import com.ebicep.warlords.util.pve.SkullID;
import com.ebicep.warlords.util.pve.SkullUtils;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.*;

public class ScrupulousZombie extends AbstractZombie implements AdvancedMob {

    public ScrupulousZombie(Location spawnLocation) {
        super(
                spawnLocation,
                "Scrupulous Zombie",
                new Utils.SimpleEntityEquipment(
                        SkullUtils.getSkullFrom(SkullID.SCULK_CORRUPTION),
                        Utils.applyColorTo(Material.LEATHER_CHESTPLATE, 10, 50, 130),
                        Utils.applyColorTo(Material.LEATHER_LEGGINGS, 10, 50, 130),
                        Utils.applyColorTo(Material.LEATHER_BOOTS, 10, 50, 130),
                        Weapons.AMARANTH.getItem()
                ),
                8000,
                0.28f,
                0,
                1200,
                1600
        );
    }

    public ScrupulousZombie(
            Location spawnLocation,
            String name,
            int maxHealth,
            float walkSpeed,
            int damageResistance,
            float minMeleeDamage,
            float maxMeleeDamage
    ) {
        super(
                spawnLocation,
                name,
                new Utils.SimpleEntityEquipment(
                        SkullUtils.getSkullFrom(SkullID.SCULK_CORRUPTION),
                        Utils.applyColorTo(Material.LEATHER_CHESTPLATE, 10, 50, 130),
                        Utils.applyColorTo(Material.LEATHER_LEGGINGS, 10, 50, 130),
                        Utils.applyColorTo(Material.LEATHER_BOOTS, 10, 50, 130),
                        Weapons.AMARANTH.getItem()
                ),
                maxHealth,
                walkSpeed,
                damageResistance,
                minMeleeDamage,
                maxMeleeDamage
        );
    }

    @Override
    public void onSpawn(PveOption option) {
        super.onSpawn(option);
        EffectUtils.strikeLightning(warlordsNPC.getLocation(), false, 2);
    }

    @Override
    public void whileAlive(int ticksElapsed, PveOption option) {

    }

    @Override
    public void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, WarlordsDamageHealingEvent event) {

    }

    @Override
    public void onDamageTaken(WarlordsEntity self, WarlordsEntity attacker, WarlordsDamageHealingEvent event) {

    }

    @Override
    public void onDeath(WarlordsEntity killer, Location deathLocation, PveOption option) {
        super.onDeath(killer, deathLocation, option);
        EffectUtils.playFirework(deathLocation, FireworkEffect.builder()
                                                                       .withColor(Color.WHITE)
                                                                       .with(FireworkEffect.Type.BURST)
                                                                       .withTrail()
                                                                       .build());
        Utils.playGlobalSound(deathLocation, Sound.ENTITY_ZOMBIE_DEATH, 2, 0.4f);
    }
}