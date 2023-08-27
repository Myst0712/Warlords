package com.ebicep.warlords.pve.bountysystem.bounties;

import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.option.pve.wavedefense.WaveDefenseOption;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.pve.DifficultyIndex;
import com.ebicep.warlords.pve.bountysystem.AbstractBounty;
import com.ebicep.warlords.pve.bountysystem.Bounty;
import com.ebicep.warlords.pve.bountysystem.BountyUtils;
import com.ebicep.warlords.pve.bountysystem.rewards.DailyRewardSpendable4;
import com.ebicep.warlords.pve.bountysystem.trackers.TracksPostGame;

public class Advance2 extends AbstractBounty implements TracksPostGame, DailyRewardSpendable4 {

    @Override
    public int getTarget() {
        return 50;
    }

    @Override
    public String getName() {
        return "Advance";
    }

    @Override
    public String getDescription() {
        return "Complete 50 waves in Endless.";
    }

    @Override
    public Bounty getBounty() {
        return Bounty.ADVANCE2;
    }

    @Override
    public void onGameEnd(Game game, WarlordsPlayer warlordsPlayer) {
        BountyUtils.getPvEOptionFromGame(game, WaveDefenseOption.class).ifPresent(waveDefenseOption -> {
            if (waveDefenseOption.getDifficulty() == DifficultyIndex.ENDLESS) {
                value += waveDefenseOption.getWavesCleared();
            }
        });
    }

}