package com.ebicep.warlords.guilds;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.leaderboards.guilds.GuildLeaderboardManager;
import com.ebicep.warlords.guilds.logs.types.twoplayer.GuildLogInvite;
import com.ebicep.warlords.guilds.upgrades.temporary.GuildUpgradeTemporary;
import com.ebicep.warlords.util.chat.ChatUtils;
import com.ebicep.warlords.util.java.Pair;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class GuildManager {

    public static final List<Guild> GUILDS = new ArrayList<>();
    private static final Set<GuildInvite> INVITES = new HashSet<>();

    private static final Set<Guild> GUILDS_TO_UPDATE = new HashSet<>();

    static {
        new BukkitRunnable() {

            int secondsElapsed = 0;

            @Override
            public void run() {
                //check for guilds to update
                if (secondsElapsed % 20 == 0) {
                    Warlords.newChain()
                            .async(GuildManager::updateGuilds)
                            .sync(GUILDS_TO_UPDATE::clear)
                            .execute();
                }
                //check for guild temp upgrades expiring
                for (Guild guild : GUILDS) {
                    guild.getUpgrades().removeIf(upgrade -> {
                        if (!(upgrade instanceof GuildUpgradeTemporary)) {
                            return false;
                        }
                        boolean shouldRemove = ((GuildUpgradeTemporary) upgrade).getExpirationDate().isBefore(Instant.now());
                        if (shouldRemove) {
                            for (Player player : guild.getOnlinePlayers()) {
                                Guild.sendGuildMessage(player,
                                        ChatColor.RED + "Your guild upgrade " + ChatColor.YELLOW + upgrade.getUpgrade()
                                                .getName() + ChatColor.RED + " has expired!"
                                );
                            }
                        }
                        return shouldRemove;
                    });
                }
                secondsElapsed++;
            }
        }.runTaskTimer(Warlords.getInstance(), 60, 20);
    }

    public static void updateGuilds() {
        GUILDS_TO_UPDATE.forEach(guild -> DatabaseManager.guildService.update(guild));
    }

    public static void queueUpdateGuild(Guild guild) {
        if (DatabaseManager.guildService == null || !DatabaseManager.enabled) {
            return;
        }
        GUILDS_TO_UPDATE.add(guild);
    }

    public static void reloadPlayerCaches() {
        GUILDS.forEach(Guild::reloadPlayerCache);
    }

    public static boolean existingGuildWithName(String name) {
        return GUILDS.stream().anyMatch(guild -> guild.getName().equalsIgnoreCase(name));
    }

    public static void addGuild(Guild guild) {
        GUILDS.add(guild);
        GuildLeaderboardManager.COINS_LEADERBOARD.forEach((timing, guilds) -> guilds.add(guild));
        GuildLeaderboardManager.EXPERIENCE_LEADERBOARD.forEach((timing, guilds) -> guilds.add(guild));
        queueUpdateGuild(guild);
    }

    public static void removeGuild(Guild guild) {
        GUILDS.remove(guild);
        GuildLeaderboardManager.COINS_LEADERBOARD.forEach((timing, guilds) -> guilds.remove(guild));
        GuildLeaderboardManager.EXPERIENCE_LEADERBOARD.forEach((timing, guilds) -> guilds.remove(guild));
        queueUpdateGuild(guild);
    }

    public static Pair<Guild, GuildPlayer> getGuildAndGuildPlayerFromPlayer(UUID uuid) {
        for (Guild guild : GUILDS) {
            if (guild.getDisbandDate() != null) {
                continue;
            }
            Optional<GuildPlayer> optionalGuildPlayer = guild.getPlayerMatchingUUID(uuid);
            if (optionalGuildPlayer.isPresent()) {
                return new Pair<>(guild, optionalGuildPlayer.get());
            }
        }
        return null;
    }

    public static Pair<Guild, GuildPlayer> getGuildAndGuildPlayerFromPlayer(Player player) {
        return getGuildAndGuildPlayerFromPlayer(player.getUniqueId());
    }


    public static void addInvite(Player from, Player to, Guild guild) {
        INVITES.add(new GuildInvite(to.getUniqueId(), guild));
        guild.log(new GuildLogInvite(from.getUniqueId(), to.getUniqueId()));
        guild.queueUpdate();

        ChatUtils.sendCenteredMessage(to, ChatColor.GREEN.toString() + ChatColor.BOLD + "------------------------------------------");
        ChatUtils.sendCenteredMessage(to, ChatColor.AQUA + from.getName() + ChatColor.YELLOW + " has invited you to join their guild!");
        TextComponent message = new TextComponent(ChatColor.YELLOW + "You have" + ChatColor.RED + " 5 " + ChatColor.YELLOW + "minutes to accept. " + ChatColor.GOLD + "Click here to join " + guild.getName());
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.GREEN + "Click to join " + guild.getName()).create()
        ));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild join " + guild.getName()));
        ChatUtils.sendCenteredMessageWithEvents(to, Collections.singletonList(message));
        ChatUtils.sendCenteredMessage(to, ChatColor.GREEN.toString() + ChatColor.BOLD + "------------------------------------------");
    }

    public static boolean hasInviteFromGuild(Player invited, Guild guild) {
        return INVITES.contains(new GuildInvite(invited.getUniqueId(), guild));
    }

    public static Optional<Guild> getGuildFromInvite(Player player, String guildName) {
        return INVITES.stream()
                .filter(invite ->
                        invite.getUuid().equals(player.getUniqueId()) &&
                                invite.getGuild().getName().equalsIgnoreCase(guildName) &&
                                Instant.now().isBefore(invite.getExpiration()))
                .findFirst()
                .map(GuildInvite::getGuild);
    }

    public static void removeGuildInvite(Player player, Guild guild) {
        INVITES.remove(new GuildInvite(player.getUniqueId(), guild));
    }

    public static Optional<Guild> getGuildFromName(String guildName) {
        return GUILDS.stream()
                .filter(guild -> guild.getDisbandDate() == null && guild.getName().equalsIgnoreCase(guildName))
                .findFirst();
    }

    static class GuildInvite {
        private final UUID uuid;
        private final Guild guild;
        private final Instant expiration = Instant.now().plus(5, ChronoUnit.MINUTES);

        public GuildInvite(UUID uuid, Guild guild) {
            this.uuid = uuid;
            this.guild = guild;
        }

        public UUID getUuid() {
            return uuid;
        }

        public Guild getGuild() {
            return guild;
        }

        public Instant getExpiration() {
            return expiration;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            GuildInvite that = (GuildInvite) o;
            return uuid.equals(that.uuid) && guild.equals(that.guild);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, guild);
        }
    }

}
