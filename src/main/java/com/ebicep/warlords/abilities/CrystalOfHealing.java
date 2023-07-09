package com.ebicep.warlords.abilities;

import com.ebicep.warlords.abilities.internal.AbstractAbility;
import com.ebicep.warlords.abilities.internal.icon.PurpleAbilityIcon;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.FireWorkEffectPlayer;
import com.ebicep.warlords.effects.circle.CircleEffect;
import com.ebicep.warlords.effects.circle.CircumferenceEffect;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.pve.upgrades.AbilityTree;
import com.ebicep.warlords.pve.upgrades.AbstractUpgradeBranch;
import com.ebicep.warlords.pve.upgrades.arcanist.luminary.CrystalOfHealingBranch;
import com.ebicep.warlords.util.bukkit.PacketUtils;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class CrystalOfHealing extends AbstractAbility implements PurpleAbilityIcon {

    private static final float RADIUS = 1.5f;
    private int duration = 20; // seconds
    private float maxHeal = 1200f;
    private int lifeSpan = 40; // seconds

    public CrystalOfHealing() {
        super("Crystal of Healing", 0, 0, 20, 20, 0, 0);
    }

    @Override
    public void updateDescription(Player player) {
        description = Component.text("Create a crystal of healing that absorbs surrounding light over ")
                               .append(Component.text(format(duration), NamedTextColor.GOLD))
                               .append(Component.text(" seconds, gradually increasing the amount of health it will restore to one ally when they absorb it, to a maximum of "))
                               .append(Component.text(format(maxHeal), NamedTextColor.GREEN))
                               .append(Component.text(" health. The crystal of healing has a lifespan of "))
                               .append(Component.text(format(lifeSpan), NamedTextColor.GOLD))
                               .append(Component.text(" seconds after its completion."));
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        return null;
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsEntity wp, Player player) {
        Block targetBlock = Utils.getTargetBlock(player, 12);
        if (targetBlock.getType() == Material.AIR) {
            return false;
        }
        wp.subtractEnergy(energyCost, false);
        Location groundLocation = targetBlock.getLocation().clone();
        groundLocation.add(0, 1, 0);
        double baseY = groundLocation.getY();

        Utils.playGlobalSound(wp.getLocation(), "arcanist.crystalofhealing.activation", 2, 0.85f);

        CircleEffect teamCircleEffect = new CircleEffect(
                wp.getGame(),
                wp.getTeam(),
                groundLocation,
                RADIUS,
                new CircumferenceEffect(Particle.WAX_OFF, Particle.REDSTONE)
        );

        FireWorkEffectPlayer.playFirework(groundLocation, FireworkEffect.builder()
                                                                        .withColor(Color.LIME)
                                                                        .with(FireworkEffect.Type.BALL)
                                                                        .trail(true)
                                                                        .build());

        ArmorStand crystal = Utils.spawnArmorStand(groundLocation, armorStand -> {
            armorStand.setGravity(true);
            armorStand.customName(Component.text(60, NamedTextColor.GREEN));
            armorStand.setCustomNameVisible(true);
            armorStand.getEquipment().setHelmet(new ItemStack(Material.BROWN_STAINED_GLASS_PANE));
        });
        for (WarlordsEntity warlordsEntity : PlayerFilter.playingGame(wp.getGame()).enemiesOf(wp)) {
            if (warlordsEntity.getEntity() instanceof Player p) {
                PacketUtils.removeEntityForPlayer(p, crystal.getEntityId());
            }
        }
        wp.getCooldownManager().addCooldown(new RegularCooldown<>(
                name,
                "CRYSTAL",
                CrystalOfHealing.class,
                new CrystalOfHealing(),
                wp,
                CooldownTypes.ABILITY,
                cooldownManager -> {
                },
                cooldownManager -> {
                    crystal.remove();
                },
                false,
                (duration + lifeSpan) * 20,
                Collections.singletonList((cooldown, ticksLeft, ticksElapsed) -> {
                    teamCircleEffect.playEffects();
                    if (ticksElapsed % 2 == 0) {
                        Location crystalLocation = crystal.getLocation();
                        crystalLocation.setY(Math.sin(ticksElapsed * Math.PI / 40) / 4 + baseY);
                        crystalLocation.setYaw(crystalLocation.getYaw() + 10);
                        crystal.teleport(crystalLocation);
                    }
                    if (ticksElapsed % 20 == 0) {
                        int secondsElapsed = ticksElapsed / 20;
                        if (secondsElapsed < duration) {
                            crystal.customName(Component.text(duration - secondsElapsed, NamedTextColor.YELLOW));
                        } else {
                            crystal.customName(Component.text(lifeSpan - (secondsElapsed - duration), NamedTextColor.GREEN));
                        }
                        if (pveMasterUpgrade) {
                            for (WarlordsEntity allyTarget : PlayerFilter
                                    .entitiesAround(crystal.getLocation(), 6, 6, 6)
                                    .aliveTeammatesOf(wp)
                            ) {
                                allyTarget.addHealingInstance(
                                        wp,
                                        name,
                                        50,
                                        50,
                                        0,
                                        100
                                );
                            }
                        }

                        EffectUtils.playCircularEffectAround(
                                wp.getGame(),
                                crystal.getLocation(),
                                Particle.VILLAGER_HAPPY,
                                1,
                                1,
                                0.1,
                                8,
                                1,
                                3
                        );
                    }
                    if (ticksElapsed < 40) {
                        return; // prevent instant pickup
                    }
                    PlayerFilter.entitiesAround(groundLocation, RADIUS, RADIUS, RADIUS)
                                .teammatesOf(wp)
                                .closestFirst(groundLocation)
                                .first(teammate -> {
                                    teammate.playSound(teammate.getLocation(), "shaman.earthlivingweapon.impact", 1, 0.45f);
                                    FireWorkEffectPlayer.playFirework(groundLocation, FireworkEffect.builder()
                                                                                                    .withColor(Color.WHITE)
                                                                                                    .with(FireworkEffect.Type.STAR)
                                                                                                    .build());
                                    cooldown.setTicksLeft(0);
                                    int secondsElapsed = ticksElapsed / 20;
                                    float healAmount = secondsElapsed >= duration ? maxHeal : (maxHeal * ticksElapsed) / (duration * 20);
                                    teammate.addHealingInstance(wp, name, healAmount, healAmount, critChance, critMultiplier);
                                });
                })
        ));

        return true;
    }

    @Override
    public AbstractUpgradeBranch<?> getUpgradeBranch(AbilityTree abilityTree) {
        return new CrystalOfHealingBranch(abilityTree, this);
    }

    public float getMaxHeal() {
        return maxHeal;
    }

    public void setMaxHeal(float maxHeal) {
        this.maxHeal = maxHeal;
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }
}
