package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractStrikeBase;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class RighteousStrike extends AbstractStrikeBase {

    public RighteousStrike() {
        super("Righteous Strike", 358, 455, 0, 90, 20, 175);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Strike the targeted enemy for §c" + format(minDamageHeal) + " §7- §c" + format(maxDamageHeal) + " §7damage.\n" +
                "§7Each strike reduces the duration of your struck\n" +
                "§7target's active ability timers by §60.5 §7seconds." +
                "\n\n" +
                "§7Additionally, if your struck target is silenced,\n" +
                "§7reduce the cooldown of your Prism Guard by §60.8\n" +
                "§7seconds and reduce their active ability timers\n" +
                "§7by §60.8 §7seconds instead.";
    }

    @Override
    protected void onHit(@Nonnull WarlordsPlayer wp, @Nonnull Player player, @Nonnull WarlordsPlayer nearPlayer) {
        if (nearPlayer.getCooldownManager().hasCooldown(SoulShackle.class)) {
            nearPlayer.getCooldownManager().subtractTicksOnRegularCooldowns(CooldownTypes.ABILITY, 16);
            wp.getSpec().getBlue().subtractCooldown(0.8f);
        } else {
            nearPlayer.getCooldownManager().subtractTicksOnRegularCooldowns(CooldownTypes.ABILITY, 10);
        }
        nearPlayer.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
    }
}