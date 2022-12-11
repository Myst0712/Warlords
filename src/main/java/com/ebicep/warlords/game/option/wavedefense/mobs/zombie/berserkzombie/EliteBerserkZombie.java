package com.ebicep.warlords.game.option.wavedefense.mobs.zombie.berserkzombie;

import com.ebicep.warlords.abilties.Berserk;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.wavedefense.WaveDefenseOption;
import com.ebicep.warlords.game.option.wavedefense.mobs.MobTier;
import com.ebicep.warlords.player.general.ArmorManager;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.PermanentCooldown;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EliteBerserkZombie extends AbstractBerserkZombie {

    public EliteBerserkZombie(Location spawnLocation) {
        super(
                spawnLocation,
                "Elite Berserker",
                MobTier.ELITE,
                new Utils.SimpleEntityEquipment(
                        ArmorManager.Helmets.GREATER_WARRIOR_HELMET.itemRed,
                        new ItemStack(Material.CHAINMAIL_CHESTPLATE),
                        new ItemStack(Material.CHAINMAIL_LEGGINGS),
                        new ItemStack(Material.CHAINMAIL_BOOTS),
                        new ItemStack(Material.PRISMARINE_SHARD)
                ),
                4000,
                0.494f, //30% more than basic zombie
                10,
                300,
                500
        );
        woundingStrike.setMinDamageHeal(woundingStrike.getMinDamageHeal() * 1.25f);
        woundingStrike.setMaxDamageHeal(woundingStrike.getMaxDamageHeal() * 1.25f);
    }

    @Override
    public void onSpawn(WaveDefenseOption option) {
        super.onSpawn(option);
        warlordsNPC.getCooldownManager().addCooldown(new PermanentCooldown<Berserk>(
                "Berserk",
                "BERS",
                Berserk.class,
                new Berserk(),
                warlordsNPC,
                CooldownTypes.ABILITY,
                cooldownManager -> {
                },
                false,
                (cooldown, ticksElapsed) -> {
                    if (ticksElapsed % 3 == 0) {
                        ParticleEffect.VILLAGER_ANGRY.display(
                                0,
                                0,
                                0,
                                0.1f,
                                1,
                                warlordsNPC.getLocation().add(0, 1.75, 0),
                                500
                        );
                    }
                }
        ) {
            @Override
            public float modifyDamageBeforeInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                return currentDamageValue * 1.2f;
            }
        });
    }
}
