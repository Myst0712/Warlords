package com.ebicep.warlords.pve.mobs.events.spidersburrow;

import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.general.Weapons;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.pve.mobs.Spider;
import com.ebicep.warlords.pve.mobs.abilities.AbstractPveAbility;
import com.ebicep.warlords.pve.mobs.tiers.BossMinionMob;
import com.ebicep.warlords.pve.mobs.zombie.AbstractZombie;
import com.ebicep.warlords.util.pve.SkullID;
import com.ebicep.warlords.util.pve.SkullUtils;
import com.ebicep.warlords.util.warlords.PlayerFilterGeneric;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

public class EventForsakenShrieker extends AbstractZombie implements BossMinionMob, Spider {


    public EventForsakenShrieker(Location spawnLocation) {
        super(
                spawnLocation,
                "Forsaken Respite",
                new Utils.SimpleEntityEquipment(
                        SkullUtils.getSkullFrom(SkullID.DEEP_DARK_CRAWLER),
                        Utils.applyColorTo(Material.LEATHER_CHESTPLATE, 87, 9, 86),
                        Utils.applyColorTo(Material.LEATHER_LEGGINGS, 87, 9, 86),
                        Utils.applyColorTo(Material.LEATHER_BOOTS, 87, 9, 86),
                        Weapons.SILVER_PHANTASM_SWORD_3.getItem()
                ),
                2700,
                0.45f,
                0,
                300,
                450,
                new BlindNear()
        );
    }

    @Override
    public void onSpawn(PveOption option) {
        super.onSpawn(option);
        int currentWave = option.getWaveCounter();
        if (currentWave % 5 == 0 && currentWave > 5) {
            float additionalHealthMultiplier = 1 + .15f * (currentWave / 5f - 1);
            warlordsNPC.setMaxBaseHealth(warlordsNPC.getMaxBaseHealth() * additionalHealthMultiplier);
            warlordsNPC.heal();
        }
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

    private static class BlindNear extends AbstractPveAbility {

        public BlindNear() {
            super("Blind Near", 5, 50);
        }

        @Override
        public boolean onPveActivate(@Nonnull WarlordsEntity wp, PveOption pveOption) {
            PlayerFilterGeneric.entitiesAround(wp, 10, 10, 10)
                               .enemiesOf(wp)
                               .warlordsPlayers()
                               .forEach(warlordsPlayer -> warlordsPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, true, false)));
            return true;
        }

    }

}
