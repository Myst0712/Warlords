package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.WarlordsNPC;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.bukkit.HeadUtils;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SoulSwitch extends AbstractAbility {

    private int radius = 13;

    public SoulSwitch() {
        super("Soul Switch", 0, 0, 30, 40, -1, 50);
    }

    @Override
    public void updateDescription(Player player) {
        description = Component.text("Switch locations with an enemy, blinding them for ")
                               .append(Component.text("1.5 ", NamedTextColor.GOLD))
                               .append(Component.text("seconds. Has an optimal range of "))
                               .append(Component.text(radius, NamedTextColor.YELLOW))
                               .append(Component.text("blocks. Soul Switch has low vertical range."));

    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        List<Pair<String, String>> info = new ArrayList<>();
        info.add(new Pair<>("Times Used", "" + timesUsed));

        return info;
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsEntity wp, @Nonnull Player player) {
        for (WarlordsEntity swapTarget : PlayerFilter
                .entitiesAround(wp.getLocation(), radius, radius / 2f, radius)
                .aliveEnemiesOf(wp)
                .requireLineOfSight(wp)
                .lookingAtFirst(wp)
        ) {
            if (swapTarget.getCarriedFlag() != null) {
                wp.sendMessage(Component.text(" You cannot Soul Switch with a player holding the flag!", NamedTextColor.RED));
            } else if (wp.getCarriedFlag() != null) {
                wp.sendMessage(Component.text(" You cannot Soul Switch while holding the flag!", NamedTextColor.RED));
            } else {
                wp.subtractEnergy(energyCost, false);
                Utils.playGlobalSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2, 1.5f);

                Location swapLocation = swapTarget.getLocation();
                Location ownLocation = wp.getLocation();

                EffectUtils.playCylinderAnimation(swapLocation, 1.05, Particle.CLOUD, 1);
                EffectUtils.playCylinderAnimation(ownLocation, 1.05, Particle.CLOUD, 1);

                swapTarget.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0, true, false));
                swapTarget.sendMessage(WarlordsEntity.RECEIVE_ARROW_RED
                        .append(Component.text(" You've been Soul Swapped by ", NamedTextColor.GRAY))
                        .append(Component.text(wp.getName(), NamedTextColor.YELLOW))
                        .append(Component.text("!", NamedTextColor.GRAY))
                );
                swapTarget.teleport(new Location(
                        wp.getWorld(),
                        ownLocation.getX(),
                        ownLocation.getY(),
                        ownLocation.getZ(),
                        swapLocation.getYaw(),
                        swapLocation.getPitch()
                ));

                wp.sendMessage(WarlordsEntity.GIVE_ARROW_GREEN
                        .append(Component.text(" You swapped with ", NamedTextColor.GRAY))
                        .append(Component.text(swapTarget.getName(), NamedTextColor.YELLOW))
                        .append(Component.text("!", NamedTextColor.GRAY))
                );
                wp.teleport(new Location(
                        swapLocation.getWorld(),
                        swapLocation.getX(),
                        swapLocation.getY(),
                        swapLocation.getZ(),
                        ownLocation.getYaw(),
                        ownLocation.getPitch()
                ));

                if (swapTarget instanceof WarlordsNPC) {
                    ArmorStand decoy = Utils.spawnArmorStand(ownLocation, armorStand -> {
                        armorStand.setCustomNameVisible(true);
                        armorStand.customName(wp.getColoredName().append(Component.text("'s Decoy")));
                    });

                    EntityEquipment equipment = decoy.getEquipment();
                    equipment.setItemInMainHand(player.getInventory().getItem(0));
                    equipment.setHelmet(HeadUtils.getHead(player.getUniqueId()));
                    equipment.setChestplate(player.getInventory().getChestplate());
                    equipment.setLeggings(player.getInventory().getLeggings());
                    equipment.setBoots(player.getInventory().getBoots());

                    PlayerFilter.entitiesAround(ownLocation, 15, 15, 15)
                                .aliveEnemiesOf(wp)
                                .forEach(warlordsEntity -> {
                                    if (warlordsEntity instanceof WarlordsNPC) {
                                        ((WarlordsNPC) warlordsEntity).getMob().setTarget(decoy);
                                    }
                                });

                    if (pveUpgrade) {
                        float healing = (wp.getMaxHealth() - wp.getHealth()) * 0.1f;
                        wp.addHealingInstance(
                                wp,
                                name,
                                healing,
                                healing,
                                -1,
                                100,
                                false,
                                false
                        );
                    }
                    new GameRunnable(wp.getGame()) {
                        @Override
                        public void run() {
                            decoy.remove();
                            PlayerFilter.entitiesAround(ownLocation, 5, 5, 5)
                                        .aliveEnemiesOf(wp)
                                        .forEach(hit -> {
                                            hit.addDamageInstance(
                                                    wp,
                                                    name,
                                                    782 * (pveUpgrade ? 2 : 1),
                                                    1034 * (pveUpgrade ? 2 : 1),
                                                    0,
                                                    100,
                                                    false
                                            );
                                            if (pveUpgrade) {
                                                hit.getCooldownManager().addCooldown(new RegularCooldown<>(
                                                        "Switch Crippling",
                                                        "CRIP",
                                                        SoulSwitch.class,
                                                        new SoulSwitch(),
                                                        wp,
                                                        CooldownTypes.DEBUFF,
                                                        cooldownManager -> {
                                                        },
                                                        20 * 5
                                                ) {
                                                    @Override
                                                    public float modifyDamageBeforeInterveneFromAttacker(
                                                            WarlordsDamageHealingEvent event,
                                                            float currentDamageValue
                                                    ) {
                                                        return currentDamageValue * .5f;
                                                    }
                                                });
                                            }
                                        });

                            ownLocation.getWorld().spawnParticle(
                                    Particle.EXPLOSION_LARGE,
                                    ownLocation.add(0, 1, 0),
                                    5,
                                    0,
                                    0,
                                    0,
                                    0.5,
                                    null,
                                    true
                            );
                        }
                    }.runTaskLater(60);
                }

                return true;
            }
        }

        return false;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
