package com.ebicep.warlords.commands.debugcommands;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.maps.Game;
import com.ebicep.warlords.maps.GameManager.GameHolder;
import com.ebicep.warlords.maps.state.EndState;
import com.ebicep.warlords.maps.state.PlayingState;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class GameTerminateCommand extends GameTargetCommand implements TabExecutor {

    @Override
    protected void doAction(CommandSender sender, Collection<GameHolder> gameInstances) {
        sender.sendMessage(ChatColor.RED + "DEV:" + ChatColor.GRAY + " Requesting engine to terminate games...");
        if (gameInstances.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No valid targets found!");
            return;
        }
        for (GameHolder holder : gameInstances) {
            Game game = holder.getGame();
            if (game == null) {
                sender.sendMessage(ChatColor.GRAY + "- " + holder.getName() + ": " + ChatColor.RED + "The game is not active now");
                continue;
            }
            Optional<PlayingState> state = game.getState(PlayingState.class);
            if (!state.isPresent()) {
                sender.sendMessage(ChatColor.GRAY + "- " + holder.getName() + ": " + ChatColor.RED + "The game is not in playing state, instead it is in " + game.getState().getClass().getSimpleName());
            } else {
                sender.sendMessage(ChatColor.GRAY + "- " + holder.getName() + ": " + ChatColor.RED + "Terminating game...");
                game.setNextState(new EndState(game, null));
            }
        }

        sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "Game has been terminated. Warping back to lobby...");
    }

    public void register(Warlords instance) {
        instance.getCommand("terminategame").setExecutor(this);
        instance.getCommand("terminategame").setTabCompleter(this);
    }
}
