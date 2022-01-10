package com.ebicep.warlords.database.repositories.player.pojos.ctf;

import com.ebicep.warlords.database.repositories.games.GameMode;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGame;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayers;
import com.ebicep.warlords.database.repositories.player.pojos.AbstractDatabaseStatInformation;
import org.springframework.data.mongodb.core.mapping.Field;

public class DatabaseBaseCTF extends AbstractDatabaseStatInformation {

    @Field("flags_captured")
    private int flagsCaptured = 0;
    @Field("flags_returned")
    private int flagsReturned = 0;
    @Field("total_blocks_travelled")
    private long totalBlocksTravelled = 0;
    @Field("most_blocks_travelled")
    private long mostBlocksTravelled = 0;
    @Field("total_time_in_respawn")
    private long totalTimeInRespawn = 0;
    @Field("total_time_played")
    private long totalTimePlayed = 0;

    public DatabaseBaseCTF() {
    }

    @Override
    public void updateCustomStats(GameMode gameMode, boolean isCompGame, DatabaseGame databaseGame, DatabaseGamePlayers.GamePlayer gamePlayer, boolean won, boolean add) {
        //UPDATE SPEC EXPERIENCE
        this.experience += add ? gamePlayer.getExperienceEarnedSpec() : -gamePlayer.getExperienceEarnedSpec();

        this.flagsCaptured += gamePlayer.getFlagCaptures();
        this.flagsReturned += gamePlayer.getFlagReturns();
        this.totalBlocksTravelled += gamePlayer.getBlocksTravelled();
        if (this.mostBlocksTravelled < gamePlayer.getBlocksTravelled()) {
            this.mostBlocksTravelled = gamePlayer.getBlocksTravelled();
        }
        this.totalTimeInRespawn += gamePlayer.getSecondsInRespawn();
        this.totalTimePlayed += 900 - databaseGame.getTimeLeft();
    }

    public int getFlagsCaptured() {
        return flagsCaptured;
    }

    public void setFlagsCaptured(int flagsCaptured) {
        this.flagsCaptured = flagsCaptured;
    }

    public int getFlagsReturned() {
        return flagsReturned;
    }

    public void setFlagsReturned(int flagsReturned) {
        this.flagsReturned = flagsReturned;
    }

    public long getTotalBlocksTravelled() {
        return totalBlocksTravelled;
    }

    public void addTotalBlocksTravelled(long totalBlocksTravelled) {
        this.totalBlocksTravelled += totalBlocksTravelled;
    }

    public long getMostBlocksTravelled() {
        return mostBlocksTravelled;
    }

    public void setMostBlocksTravelled(long mostBlocksTravelled) {
        this.mostBlocksTravelled = mostBlocksTravelled;
    }

    public long getTotalTimeInRespawn() {
        return totalTimeInRespawn;
    }

    public void addTotalTimeInRespawn(long totalTimeInRespawn) {
        this.totalTimeInRespawn += totalTimeInRespawn;
    }

    public long getTotalTimePlayed() {
        return totalTimePlayed;
    }

    public void addTotalTimePlayed(long totalTimePlayed) {
        this.totalTimePlayed += totalTimePlayed;
    }
}
