package com.ebicep.warlords.guilds.logs.types.oneplayer;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public class GuildLogJoin extends AbstractGuildLogOnePlayer {

    public GuildLogJoin(UUID sender) {
        super(sender);
    }

    @Override
    public Component getAction() {
        return Component.text("joined");
    }


}
