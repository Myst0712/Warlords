
package com.ebicep.warlords.events.player;

import com.ebicep.warlords.player.ingame.WarlordsEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 *
 */
public class WarlordsDamageHealingEvent extends AbstractWarlordsPlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private WarlordsEntity attacker;
    private String ability;
    private float min;
    private float max;
    private float critChance;
    private float critMultiplier;
    private boolean ignoreReduction;
    private boolean isLastStandFromShield;
    private boolean isDamageInstance;

    private List<String> flags;
    private boolean cancelled;

    public WarlordsDamageHealingEvent(
            WarlordsEntity player,
            WarlordsEntity attacker,
            String ability,
            float min,
            float max,
            float critChance,
            float critMultiplier,
            boolean ignoreReduction,
            boolean isLastStandFromShield,
            boolean isDamageInstance,
            List<String> flags
    ) {
        super(player);
        this.attacker = attacker;
        this.ability = ability;
        this.min = min;
        this.max = max;
        this.critChance = critChance;
        this.critMultiplier = critMultiplier;
        this.ignoreReduction = ignoreReduction;
        this.isLastStandFromShield = isLastStandFromShield;
        this.isDamageInstance = isDamageInstance;
        this.flags = flags;
    }

    public WarlordsEntity getAttacker() {
        return attacker;
    }

    public void setAttacker(WarlordsEntity attacker) {
        this.attacker = attacker;
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getCritChance() {
        return critChance;
    }

    public void setCritChance(float critChance) {
        this.critChance = critChance;
    }

    public float getCritMultiplier() {
        return critMultiplier;
    }

    public void setCritMultiplier(float critMultiplier) {
        this.critMultiplier = critMultiplier;
    }

    public boolean isIgnoreReduction() {
        return ignoreReduction;
    }

    public void setIgnoreReduction(boolean ignoreReduction) {
        this.ignoreReduction = ignoreReduction;
    }

    public boolean isIsLastStandFromShield() {
        return isLastStandFromShield;
    }

    public void setIsLastStandFromShield(boolean isLastStandFromShield) {
        this.isLastStandFromShield = isLastStandFromShield;
    }

    public boolean isDamageInstance() {
        return isDamageInstance;
    }

    public void setIsDamageInstance(boolean isDamageInstance) {
        this.isDamageInstance = isDamageInstance;
    }

    public boolean isHealingInstance() {
        return !isDamageInstance;
    }

    public void setIsHealingInstance(boolean isHealingInstance) {
        this.isDamageInstance = !isHealingInstance;
    }

    public List<String> getFlags() {
        return flags;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WarlordsDamageHealingEvent{");
        sb.append("player=").append(getPlayer());
        sb.append(", attacker=").append(attacker);
        sb.append(", ability=").append(ability);
        sb.append(", min=").append(min);
        sb.append(", max=").append(max);
        sb.append(", critChance=").append(critChance);
        sb.append(", critMultiplier=").append(critMultiplier);
        sb.append(", ignoreReduction=").append(ignoreReduction);
        sb.append(", isLastStandFromShield=").append(isLastStandFromShield);
        sb.append(", isDamageInstance=").append(isDamageInstance);
        sb.append(", cancelled=").append(cancelled);
        sb.append('}');
        return sb.toString();
    }
            
}