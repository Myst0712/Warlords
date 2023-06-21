package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractBeam;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownFilter;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.java.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class RayOfLight extends AbstractBeam {

    private int healingIncrease = 100;

    public RayOfLight() {
        super("Ray of Light", 536, 724, 10, 10, 20, 175, 30, 30, true);
    }

    @Override
    public void updateDescription(Player player) {
        description = Component.text("Unleash a concentrated beam of holy light, healing ")
                               .append(formatRangeDamage(minDamageHeal, maxDamageHeal))
                               .append(Component.text(
                                       " health to all allies hit. If the target is affected by the max stacks of Merciful Hex, remove all stacks and increase the healing of Ray of Light by "))
                               .append(Component.text(healingIncrease + "%", NamedTextColor.GREEN))
                               .append(Component.text(" and removes their debuffs.\n\nHas a maximum range of "))
                               .append(Component.text(format(maxDistance), NamedTextColor.YELLOW))
                               .append(Component.text(" blocks."));
    }

    @Override
    public List<Pair<String, String>> getAbilityInfo() {
        return null;
    }

    @Override
    protected void playEffect(@Nonnull Location currentLocation, int ticksLived) {

    }

    @Override
    protected void onNonCancellingHit(@Nonnull InternalProjectile projectile, @Nonnull WarlordsEntity hit, @Nonnull Location impactLocation) {
        WarlordsEntity wp = projectile.getShooter();
        if (hit.isTeammate(wp)) {
            float minHeal = minDamageHeal;
            float maxHeal = maxDamageHeal;
            int hexStacks = (int) new CooldownFilter<>(hit, RegularCooldown.class)
                    .filterCooldownFrom(wp)
                    .filterCooldownClass(MercifulHex.class)
                    .stream()
                    .count();
            boolean hasDivineBlessing = wp.getCooldownManager().hasCooldown(DivineBlessing.class);
            if (hexStacks >= 3) {
                if (!hasDivineBlessing) {
                    hit.getCooldownManager().removeCooldown(MercifulHex.class, false);
                }
                minHeal *= 1 + (healingIncrease / 100f);
                maxHeal *= 1 + (healingIncrease / 100f);
            }
            wp.getCooldownManager().removeDebuffCooldowns();
            wp.getSpeed().removeSlownessModifiers();
            hit.addHealingInstance(wp, name, minHeal, maxHeal, critChance, critMultiplier, false, false);
        }
    }

    @Override
    public ItemStack getBeamItem() {
        return new ItemStack(Material.BLUE_ORCHID);
    }

    @Nullable
    @Override
    protected String getActivationSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0;
    }

    @Override
    protected float getSoundPitch() {
        return 0;
    }

}
