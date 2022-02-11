package com.ebicep.jda;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.GameManager.GameHolder;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.WinAfterTimeoutOption;
import com.ebicep.warlords.game.state.PlayingState;
import com.ebicep.warlords.game.state.PreLobbyState;
import com.ebicep.warlords.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import javax.security.auth.login.LoginException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class BotManager {

    public static JDA jda;
    public static String botToken;

    public static String compGamesServerID = "776590423501045760";
    public static String compGamesServerStatusChannel = "instant-updates";
    public static HashMap<String, TextChannel> compGamesServerChannelCache = new HashMap<>();
    public static Message compStatusMessage;

    public static String wl2ServerID = "931564871462572062";
    public static String wl2ServerStatusChannel = "server-status";
    public static HashMap<String, TextChannel> wl2ServerChannelCache = new HashMap<>();
    public static Message wl2StatusMessage;

    public static int numberOfMessagesSentLast30Sec = 0;

    public static void connect() throws LoginException {
        if (botToken != null) {
            jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(new BotListener())
                    .build();
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                if (numberOfMessagesSentLast30Sec > 0) {
                    numberOfMessagesSentLast30Sec--;
                }
            }
        }.runTaskTimer(Warlords.getInstance(), 20, 70);
    }

    public static void sendDebugMessage(String message) {
        getWL2Server().getTextChannels().stream()
                .filter(textChannel -> textChannel.getName().equalsIgnoreCase("admin-log"))
                .findFirst()
                .ifPresent(textChannel -> textChannel.sendMessage(message).queue());
    }

    public static void sendDebugMessage(MessageEmbed embed) {
        getWL2Server().getTextChannels().stream()
                .filter(textChannel -> textChannel.getName().equalsIgnoreCase("admin-log"))
                .findFirst()
                .ifPresent(textChannel -> textChannel.sendMessageEmbeds(embed).queue());
    }

    public static Guild getCompGamesServer() {
        return jda.getGuildById(compGamesServerID);
    }

    public static Guild getWL2Server() {
        return jda.getGuildById(wl2ServerID);
    }

    public static Optional<TextChannel> getTextChannelCompsByName(String name) {
        if (compGamesServerChannelCache.containsKey(name))
            return Optional.ofNullable(compGamesServerChannelCache.get(name));
        Optional<TextChannel> optionalTextChannel = getCompGamesServer().getTextChannels().stream().filter(textChannel -> textChannel.getName().equalsIgnoreCase(name)).findFirst();
        optionalTextChannel.ifPresent(textChannel -> compGamesServerChannelCache.put(name, textChannel));
        return optionalTextChannel;
    }

    public static Optional<TextChannel> getTextChannelWL2ByName(String name) {
        if (wl2ServerChannelCache.containsKey(name)) return Optional.ofNullable(wl2ServerChannelCache.get(name));
        Optional<TextChannel> optionalTextChannel = getWL2Server().getTextChannels().stream().filter(textChannel -> textChannel.getName().equalsIgnoreCase(name)).findFirst();
        optionalTextChannel.ifPresent(textChannel -> wl2ServerChannelCache.put(name, textChannel));
        return optionalTextChannel;
    }

    public static void sendMessageToNotificationChannel(String message, boolean sendToCompServer, boolean sendToWL2Server) {
        if (numberOfMessagesSentLast30Sec > 15) {
            return;
        }
        if (!Warlords.serverIP.equals("51.81.49.127")) {
            return;
        }
        if (sendToCompServer) {
            getTextChannelCompsByName(compGamesServerStatusChannel).ifPresent(textChannel -> textChannel.sendMessage(message).queue());
        }
        if (sendToWL2Server) {
            getTextChannelWL2ByName(wl2ServerStatusChannel).ifPresent(textChannel -> textChannel.sendMessage(message).queue());
        }
    }

    public static void sendStatusMessage(boolean onQuit) {
        if (!Warlords.serverIP.equals("51.81.49.127")) {
            return;
        }
        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Server Status", null)
                .setColor(3066993)
                .setFooter(dateFormat.format(new Date()) + " EST");
        eb.setDescription("**Players Online**: " + (onQuit ? Bukkit.getOnlinePlayers().size() - 1 : Bukkit.getOnlinePlayers().size()) + "\n");
        eb.appendDescription("**Players In Game**: " + Warlords.getGameManager().getPlayerCount() + "\n");
        eb.appendDescription("**Players Waiting in lobby**: " + Warlords.getGameManager().getPlayerCountInLobby()+ "\n");
        for(GameHolder holder : Warlords.getGameManager().getGames()) {
            Game game = holder.getGame();
            if(game == null) {
                eb.appendDescription("**Game**: " + holder.getMap().getMapName() + " Inactive\n");
            } else {
                if (game.getState() instanceof PreLobbyState) {
                    PreLobbyState state = (PreLobbyState) game.getState();
                    if (!state.hasEnoughPlayers()) {
                        eb.appendDescription("**Game**: " + game.getMap().getMapName() + " Lobby - Waiting for players" + "\n");
                    } else {
                        eb.appendDescription("**Game**: " + game.getMap().getMapName() + " Lobby - " + state.getTimeLeftString() + " Left" + "\n");
                    }
                } else if (game.getState() instanceof PlayingState) {
                    OptionalInt timeLeft = WinAfterTimeoutOption.getTimeLeft(game);
                    String time = Utils.formatTimeLeft(timeLeft.isPresent() ? timeLeft.getAsInt() : (System.currentTimeMillis() - game.createdAt()) / 1000);
                    String word = timeLeft.isPresent() ? " Left" : " Elapsed";
                    eb.appendDescription("**Game**: " + game.getMap().getMapName() + " - " + time + word + " - " + game.getPoints(Team.BLUE)+ ":" + game.getPoints(Team.RED) + "\n");
                } else {
                    eb.appendDescription("**Game**: Ending" + "\n");
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder("**Parties**: ");
        Warlords.partyManager.getParties().forEach(party -> stringBuilder.append(party.getLeaderName()).append(" (").append(party.getPartyPlayers().size()).append("), "));
        stringBuilder.setLength(stringBuilder.length() - 1);
        eb.appendDescription(stringBuilder);

        MessageEmbed messageEmbed = eb.build();
        getTextChannelCompsByName(compGamesServerStatusChannel).ifPresent(textChannel -> {
            if (compStatusMessage == null) {
                textChannel.sendMessageEmbeds(messageEmbed).queue(m -> compStatusMessage = m);
            } else if (textChannel.getLatestMessageId().equals(compStatusMessage.getId())) {
                compStatusMessage.editMessageEmbeds(messageEmbed).queue(m -> compStatusMessage = m);
            } else {
                compStatusMessage.delete().queue();
                textChannel.sendMessageEmbeds(messageEmbed).queue(m -> compStatusMessage = m);
            }
        });
        getTextChannelWL2ByName(wl2ServerStatusChannel).ifPresent(textChannel -> {
            if (wl2StatusMessage == null) {
                textChannel.sendMessageEmbeds(messageEmbed).queue(m -> wl2StatusMessage = m);
            } else if (textChannel.getLatestMessageId().equals(wl2StatusMessage.getId())) {
                wl2StatusMessage.editMessageEmbeds(messageEmbed).queue(m -> wl2StatusMessage = m);
            } else {
                wl2StatusMessage.delete().queue();
                textChannel.sendMessageEmbeds(messageEmbed).queue(m -> wl2StatusMessage = m);
            }
        });

    }

    public static void deleteStatusMessage() {
        if (compStatusMessage != null) {
            compStatusMessage.delete().complete();
        }
        if (wl2StatusMessage != null) {
            wl2StatusMessage.delete().complete();
        }
    }

}
