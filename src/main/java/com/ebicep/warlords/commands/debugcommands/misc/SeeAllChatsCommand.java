package com.ebicep.warlords.commands.debugcommands.misc;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.ebicep.warlords.commands.miscellaneouscommands.ChatCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@CommandAlias("seeallchats")
//@CommandPermission("group.administrator")
public class SeeAllChatsCommand extends BaseCommand {

    public static final Set<UUID> playerSeeAllChats = new HashSet<>();

    public static void addPlayerSeeAllChats(Set<Player> players) {
        for (UUID playerSeeAllChat : playerSeeAllChats) {
            Player player = Bukkit.getPlayer(playerSeeAllChat);
            if (player != null) {
                players.add(player);
            }
        }
    }

    @Default
    @Description("Toggles seeing all chats")
    public void seeAllChats(Player player) {
        if (playerSeeAllChats.contains(player.getUniqueId())) {
            playerSeeAllChats.remove(player.getUniqueId());
            ChatCommand.sendDebugMessage(player, ChatColor.GREEN + "You will no longer see all chats", true);
        } else {
            playerSeeAllChats.add(player.getUniqueId());
            ChatCommand.sendDebugMessage(player, ChatColor.GREEN + "You will now see all chats", true);
        }
    }

    @Subcommand("clear")
    @Description("Clears all players that can see all chats")
    public void clear(CommandIssuer issuer) {
        playerSeeAllChats.clear();
        ChatCommand.sendDebugMessage(issuer, ChatColor.GREEN + "All players that can see all chats have been cleared", true);
    }

}