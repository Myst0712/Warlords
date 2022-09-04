package com.ebicep.warlords.guilds.menu;

import com.ebicep.warlords.guilds.Guild;
import com.ebicep.warlords.guilds.upgrades.GuildUpgrade;
import com.ebicep.warlords.guilds.upgrades.GuildUpgrades;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.java.DateUtil;
import com.ebicep.warlords.util.java.NumberFormat;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Collections;
import java.util.List;

import static com.ebicep.warlords.menu.Menu.MENU_BACK;

public class GuildBankMenu {

    public static void openGuildBankMenu(Player player, Guild guild) {
        Menu menu = new Menu("Guild Bank", 9 * 5);

        menu.setItem(1, 1,
                new ItemBuilder(Material.ENCHANTMENT_TABLE)
                        .name(ChatColor.GOLD + "Guild Upgrades")
                        .get(),
                (m, e) -> {
                    openGuildUpgradesMenu(player, guild);
                }
        );

        menu.setItem(4, 4, MENU_BACK, (m, e) -> GuildMenu.openGuildMenu(guild, player, 1));
        menu.openForPlayer(player);
    }

    public static void openGuildUpgradesMenu(Player player, Guild guild) {
        Menu menu = new Menu("Guild Upgrades", 9 * 3);

        menu.setItem(2, 1,
                new ItemBuilder(Material.GOLD_BARDING)
                        .name(ChatColor.GREEN + "Temporary Upgrades")
                        .get(),
                (m, e) -> {
                    openGuildTempUpgradeTypeMenu(player, guild, false);
                }
        );
        menu.setItem(6, 1,
                new ItemBuilder(Material.DIAMOND_BARDING)
                        .name(ChatColor.GREEN + "Permanent Upgrades")
                        .get(),
                (m, e) -> {
                    openGuildTempUpgradeTypeMenu(player, guild, true);
                }
        );

        menu.setItem(4, 2, MENU_BACK, (m, e) -> openGuildBankMenu(player, guild));
        menu.openForPlayer(player);
    }

    public static void openGuildTempUpgradeTypeMenu(Player player, Guild guild, boolean isPermanent) {
        Menu menu = new Menu("Temporary Upgrades", 9 * 6);

        List<GuildUpgrade> upgrades = guild.getUpgrades();
        int index = 0;
        for (GuildUpgrades value : GuildUpgrades.VALUES) {
            if (value.isPermanent != isPermanent) {
                continue;
            }

            ItemBuilder itemBuilder = new ItemBuilder(value.material)
                    .name(ChatColor.GREEN + value.name)
                    .flags(ItemFlag.HIDE_ENCHANTS);
            for (GuildUpgrade upgrade : upgrades) {
                if (upgrade.getUpgrade() == value) {
                    itemBuilder.enchant(Enchantment.OXYGEN, 1);
                    itemBuilder.lore(ChatColor.GRAY + "Current Tier: " + ChatColor.GREEN + upgrade.getTier());
                    if (!isPermanent) {
                        itemBuilder.addLore(ChatColor.GRAY + "Time Left: " + ChatColor.GREEN + DateUtil.getTimeTill(upgrade.getExpirationDate(),
                                false,
                                false,
                                true,
                                true
                        ));
                    }
                    itemBuilder.addLore(ChatColor.YELLOW + "\n>>> ACTIVE <<<");
                    break;
                }
            }
            menu.setItem(index % 7 + 1, index / 7 + 1,
                    itemBuilder.get(),
                    (m, e) -> {
                        openGuildUpgradePurchaseMenu(player, guild, value);
                    }
            );
            index++;
        }

        menu.setItem(4, 5, MENU_BACK, (m, e) -> openGuildUpgradesMenu(player, guild));
        menu.openForPlayer(player);
    }

    public static void openGuildUpgradePurchaseMenu(Player player, Guild guild, GuildUpgrades upgrade) {
        Menu menu = new Menu(upgrade.name, 9 * 5);

        for (int i = 0; i < 9; i++) {
            int tier = i + 1;
            menu.setItem(i % 7 + 1, i / 7 + 1,
                    new ItemBuilder(Utils.getWoolFromIndex(i + 5))
                            .name(ChatColor.GREEN + "Tier " + tier)
                            .lore(
                                    ChatColor.GRAY + "Cost: " + ChatColor.GREEN + NumberFormat.addCommas(upgrade.getCost(tier)) +
                                            " Guild Coins",
                                    "",
                                    ChatColor.RED + "WARNING: " + ChatColor.GRAY + "This will override the current upgrade."
                            )
                            .get(),
                    (m, e) -> {
                        Menu.openConfirmationMenu(player,
                                "Purchase " + upgrade.name + " (T" + tier + ")?",
                                3,
                                Collections.singletonList(ChatColor.GRAY + "Purchase Upgrade"),
                                Collections.singletonList(ChatColor.GRAY + "Go back"),
                                (m2, e2) -> {

                                },
                                (m2, e2) -> openGuildUpgradePurchaseMenu(player, guild, upgrade),
                                (m2) -> {
                                }
                        );
                    }
            );
        }

        menu.setItem(4, 4, MENU_BACK, (m, e) -> openGuildTempUpgradeTypeMenu(player, guild, upgrade.isPermanent));
        menu.openForPlayer(player);
    }

}
