package com.ebicep.warlords.pve.bountysystem;

import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.repositories.player.PlayersCollections;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.database.repositories.player.pojos.pve.DatabasePlayerPvE;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.pve.bountysystem.events.BountyClaimEvent;
import com.ebicep.warlords.pve.bountysystem.events.BountyStartEvent;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

import static com.ebicep.warlords.menu.Menu.ACTION_CLOSE_MENU;
import static com.ebicep.warlords.menu.Menu.MENU_CLOSE;

public class BountyMenu {

    public static void openBountyMenu(Player player) {
        Menu menu = new Menu("Bounties", 9 * 6);

        addBountiesToMenu(player, PlayersCollections.DAILY, menu, 1, true);
        addBountiesToMenu(player, PlayersCollections.WEEKLY, menu, 2, true);
        addBountiesToMenu(player, PlayersCollections.LIFETIME, menu, 3, false);

        menu.openForPlayer(player);
    }

    private static void addBountiesToMenu(Player player, PlayersCollections collection, Menu menu, int y, boolean claimAll) {
        DatabaseManager.getPlayer(player.getUniqueId(), collection, databasePlayer -> {
            DatabasePlayerPvE pveStats = databasePlayer.getPveStats();
            List<AbstractBounty> bounties = pveStats.getActiveBounties()
                                                    .stream()
                                                    .filter(AbstractBounty::notClaimed)
                                                    .toList();
            menu.setItem(1, y,
                    new ItemBuilder(Material.BOOK)
                            .name(Component.text(collection.name + " Bounties", NamedTextColor.RED))
                            .get(),
                    (m, e) -> {}
            );
            boolean canBeClaimed = false;
            for (int i = 0; i < bounties.size(); i++) {
                // TODO fill in claimed bounties + barrier if none
                AbstractBounty bounty = bounties.get(i);
                if (bounty.isStarted() && bounty.getProgress() == null) {
                    canBeClaimed = true;
                }
                menu.setItem(i + 2, y,
                        bounty.getItem().get(),
                        (m, e) -> {
                            if (bounty.isStarted()) {
                                if (bounty.getProgress() == null) {
                                    claimBounty(player, databasePlayer, bounty);
                                    player.closeInventory();
                                }
                            } else {
                                //AbstractBounty.COST
                                bounty.setStarted(true);
                                BountyUtils.sendBountyMessage(
                                        player,
                                        Component.text("You started the bounty ", NamedTextColor.GRAY)
                                                 .append(Component.text(bounty.getName(), NamedTextColor.GREEN))
                                                 .append(Component.text("!"))
                                );
                                Bukkit.getPluginManager().callEvent(new BountyStartEvent(databasePlayer, bounty));
                                player.closeInventory();
                            }
                        }
                );
            }
            if (claimAll && canBeClaimed) {
                menu.setItem(7, y,
                        new ItemBuilder(Material.GOLD_BLOCK)
                                .name(Component.text("Click to claim all bounties!", NamedTextColor.GREEN))
                                .get(),
                        (m, e) -> {
                            for (AbstractBounty bounty : bounties) {
                                if (bounty.isStarted()) {
                                    claimBounty(player, databasePlayer, bounty);
                                }
                            }
                            player.closeInventory();
                        }
                );
            }
            menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
        });
    }

    private static void claimBounty(Player player, DatabasePlayer databasePlayer, AbstractBounty bounty) {
        bounty.claim(databasePlayer);
        BountyUtils.sendBountyMessage(
                player,
                Component.text("You claimed the bounty ", NamedTextColor.GRAY)
                         .append(Component.text(bounty.getName(), NamedTextColor.GREEN))
                         .append(Component.text("!"))
        );
        Bukkit.getPluginManager().callEvent(new BountyClaimEvent(databasePlayer, bounty));
    }

}