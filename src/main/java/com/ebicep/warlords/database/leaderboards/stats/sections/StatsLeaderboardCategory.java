package com.ebicep.warlords.database.leaderboards.stats.sections;

import com.ebicep.warlords.database.leaderboards.stats.StatsLeaderboard;
import com.ebicep.warlords.database.repositories.player.PlayersCollections;
import com.ebicep.warlords.database.repositories.player.pojos.AbstractDatabaseStatInformation;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import me.filoghost.holographicdisplays.api.hologram.Hologram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>ALL
 * <p>Comps
 * <p>Pubs
 */
public class StatsLeaderboardCategory<T extends AbstractDatabaseStatInformation> {

    public final Function<DatabasePlayer, T> statFunction;
    public final String categoryName;
    public final List<StatsLeaderboard> statsLeaderboards = new ArrayList<>();

    public StatsLeaderboardCategory(Function<DatabasePlayer, T> statFunction, String categoryName) {
        this.statFunction = statFunction;
        this.categoryName = categoryName;
    }

    public void resetLeaderboards(PlayersCollections collection, Set<DatabasePlayer> databasePlayers, String subTitle) {
        for (StatsLeaderboard statsLeaderboard : statsLeaderboards) {
            statsLeaderboard.resetHolograms(collection, databasePlayers, categoryName, subTitle);
        }
    }

    public List<List<Hologram>> getCollectionHologramPaged(PlayersCollections collections) {
        return statsLeaderboards.stream()
                .flatMap(statsLeaderboard -> statsLeaderboard.getSortedHolograms(collections).stream())
                .collect(Collectors.toList());
    }

    public List<List<Hologram>> getAllHologramsPaged() {
        return statsLeaderboards.stream()
                .flatMap(statsLeaderboard -> statsLeaderboard.getSortedTimedHolograms().values().stream())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<Hologram> getAllHolograms() {
        return statsLeaderboards.stream()
                .flatMap(statsLeaderboard -> statsLeaderboard.getSortedTimedHolograms().values().stream())
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<StatsLeaderboard> getLeaderboards() {
        return statsLeaderboards;
    }

}
