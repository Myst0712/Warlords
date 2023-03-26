package com.ebicep.warlords.pve.items.types;

import com.ebicep.warlords.pve.items.ItemTier;
import com.ebicep.warlords.pve.items.modifiers.ItemTomeModifier;
import com.ebicep.warlords.pve.items.statpool.ItemTomeStatPool;

import java.util.HashMap;

public class ItemTome extends AbstractItem<ItemTomeStatPool, ItemTomeModifier.Blessings, ItemTomeModifier.Curses> {

    public ItemTome() {
    }

    public ItemTome(ItemTier tier) {
        super(tier, tier.generateStatPool(ItemTomeStatPool.VALUES));
    }

    @Override
    public ItemTome clone() {
        ItemTome itemTome = new ItemTome();
        itemTome.copyFrom(this);
        return itemTome;
    }

    @Override
    public HashMap<ItemTomeStatPool, ItemTier.StatRange> getTierStatRanges() {
        return tier.tomeStatRange;
    }

    @Override
    public ItemType getType() {
        return ItemType.TOME;
    }

    @Override
    public ItemTomeModifier.Blessings[] getBlessings() {
        return ItemTomeModifier.Blessings.VALUES;
    }

    @Override
    public ItemTomeModifier.Curses[] getCurses() {
        return ItemTomeModifier.Curses.VALUES;
    }

}
