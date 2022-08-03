package com.ebicep.warlords.pve.upgrades;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.option.RecordTimeElapsedOption;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.warlords.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.ebicep.warlords.menu.Menu.*;

public abstract class AbstractUpgradeBranch<T extends AbstractAbility> {

    protected AbilityTree abilityTree;
    protected T ability;
    protected ItemStack itemStack;
    protected String itemName;

    protected List<Upgrade> treeA = new ArrayList<>();
    protected List<Upgrade> treeB = new ArrayList<>();
    protected List<Upgrade> treeC = new ArrayList<>();
    protected Upgrade masterUpgrade;

    private int maxUpgrades = 6;

    public AbstractUpgradeBranch(AbilityTree abilityTree, T ability) {
        this.abilityTree = abilityTree;
        this.ability = ability;
        this.itemStack = abilityTree.getPlayer().getItemStackForAbility(ability);
        this.itemName = ability.getName();
    }

    public void openUpgradeBranchMenu() {
        WarlordsPlayer player = abilityTree.getPlayer();
        Menu menu = new Menu("Upgrades", 9 * 6);

        for (int i = 0; i < treeA.size(); i++) {
            Upgrade upgrade = treeA.get(i);
            int finalI = i;
            menu.setItem(
                    2,
                    4 - i,
                    branchItem(upgrade),
                    (m, e) -> {
                        updateInventory(player);
                        if (upgrade.isUnlocked()) {
                            player.sendMessage(ChatColor.RED + "You already unlocked this upgrade.");
                            return;
                        }
                        if (finalI != 0) {
                            if (!treeA.get(finalI - 1).isUnlocked()) {
                                player.sendMessage(ChatColor.RED + "You need to unlock the previous upgrade first!");
                                return;
                            }
                        }
                        if (player.getCurrency() < upgrade.getCurrencyCost()) {
                            player.sendMessage(ChatColor.RED + "You do not have enough Insignia (❂) to buy this upgrade!");
                            return;
                        }
                        if (maxUpgrades <= 0) {
                            player.sendMessage(ChatColor.RED + "You cannot unlock this upgrade, maximum upgrades reached.");
                            return;
                        }

                        upgrade.getOnUpgrade().run();
                        upgrade.setUnlocked(true);
                        maxUpgrades--;

                        player.subtractCurrency(upgrade.getCurrencyCost());
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 500, 1.3f);

                        globalAnnouncement(player.getGame(), upgrade, ability);
                        updateInventory(player);
                        openUpgradeBranchMenu();

                        abilityTree.getUpgradeLog().add(new AbilityTree.UpgradeLog(
                                RecordTimeElapsedOption.getTimeElapsed(player.getGame()),
                                upgrade.getName(),
                                upgrade.getDescription())
                        );
                    }
            );
        }

        for (int i = 0; i < treeB.size(); i++) {
            Upgrade upgrade = treeB.get(i);
            int finalI = i;
            menu.setItem(
                    6,
                    4 - i,
                    branchItem(upgrade),
                    (m, e) -> {
                        updateInventory(player);
                        if (upgrade.isUnlocked()) {
                            player.sendMessage(ChatColor.RED + "You already unlocked this upgrade.");
                            return;
                        }
                        if (finalI != 0) {
                            if (!treeB.get(finalI - 1).isUnlocked()) {
                                player.sendMessage(ChatColor.RED + "You need to unlock the previous upgrade first!");
                                return;
                            }
                        }
                        if (player.getCurrency() < upgrade.getCurrencyCost()) {
                            player.sendMessage(ChatColor.RED + "You do not have enough Insignia (❂) to buy this upgrade!");
                            return;
                        }
                        if (maxUpgrades <= 0) {
                            player.sendMessage(ChatColor.RED + "You cannot unlock this upgrade, maximum upgrades reached.");
                            return;
                        }

                        upgrade.getOnUpgrade().run();
                        upgrade.setUnlocked(true);
                        maxUpgrades--;

                        player.subtractCurrency(upgrade.getCurrencyCost());
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 500, 1.3f);

                        globalAnnouncement(player.getGame(), upgrade, ability);
                        updateInventory(player);
                        openUpgradeBranchMenu();

                        abilityTree.getUpgradeLog().add(new AbilityTree.UpgradeLog(
                                RecordTimeElapsedOption.getTimeElapsed(player.getGame()),
                                upgrade.getName(),
                                upgrade.getDescription())
                        );
                    }
            );
        }

        /*for (int i = 0; i < treeC.size(); i++) {
            Upgrade upgrade = treeC.get(i);
            int finalI = i;
            menu.setItem(
                    6,
                    4 - i,
                    branchItem(upgrade),
                    (m, e) -> {
                        updateInventory(player);
                        if (upgrade.isUnlocked()) {
                            player.sendMessage(ChatColor.RED + "You already unlocked this upgrade.");
                            return;
                        }
                        if (finalI != 0) {
                            if (!treeC.get(finalI - 1).isUnlocked()) {
                                player.sendMessage(ChatColor.RED + "You need to unlock the previous upgrade first!");
                                return;
                            }
                        }
                        if (player.getCurrency() < upgrade.getCurrencyCost()) {
                            player.sendMessage(ChatColor.RED + "You do not have enough Insignia's (❂) to buy this upgrade!");
                            return;
                        }

                        upgrade.getOnUpgrade().run();
                        upgrade.setUnlocked(true);

                        player.subtractCurrency(upgrade.getCurrencyCost());
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 500, 1.3f);

                        globalAnnouncement(player.getGame(), upgrade, ability);
                        updateInventory(player);
                        openUpgradeBranchMenu();

                        abilityTree.getUpgradeLog().add(new AbilityTree.UpgradeLog(
                                RecordTimeElapsedOption.getTimeElapsed(player.getGame()),
                                upgrade.getName(),
                                upgrade.getDescription())
                        );
                    }
            );
        }*/

        menu.setItem(
                4,
                0,
                masterBranchItem(masterUpgrade),
                (m, e) -> {
                    if (player.getAbilityTree().getMaxMasterUpgrades() <= 0) {
                        player.sendMessage(ChatColor.RED + "You cannot unlock this master upgrade, maximum master upgrades reached.");
                        return;
                    }
                    if (player.getCurrency() < masterUpgrade.getCurrencyCost()) {
                        player.sendMessage(ChatColor.RED + "You do not have enough Insignia (❂) to buy this upgrade!");
                        return;
                    }
                    if (masterUpgrade.isUnlocked()) {
                        player.sendMessage(ChatColor.RED + "You already unlocked this upgrade.");
                        return;
                    }

                    if (maxUpgrades <= 0) {
                        masterUpgrade.getOnUpgrade().run();
                        masterUpgrade.setUnlocked(true);

                        player.getAbilityTree().setMaxMasterUpgrades(abilityTree.getMaxMasterUpgrades() - 1);
                        player.subtractCurrency(masterUpgrade.getCurrencyCost());
                        Utils.playGlobalSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 500f, 0.8f);

                        globalAnnouncement(player.getGame(), masterUpgrade, ability);
                    } else {
                        String s = maxUpgrades == 1 ? "" : "s";
                        player.sendMessage(ChatColor.RED + "You need to unlock " + maxUpgrades + " more upgrade" + s + " before unlocking the master upgrade!");
                        return;
                    }

                    ability.updateDescription((Player) player.getEntity());
                    openUpgradeBranchMenu();
                });

        menu.setItem(3, 5, MENU_BACK, (m, e) -> abilityTree.openAbilityTree());
        menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
        if (player.getEntity() instanceof Player) {
            menu.openForPlayer((Player) player.getEntity());
        }

        menu.setItem(4, 3, new ItemBuilder(Material.DIAMOND).name(ChatColor.GRAY + "Upgrades remaining: " + ChatColor.GREEN + maxUpgrades).get(), ACTION_DO_NOTHING);
    }

    private ItemStack masterBranchItem(Upgrade upgrade) {
        ArrayList<String> lore = new ArrayList<>();
        if (upgrade.getSubName() != null) {
            lore.add((upgrade.isUnlocked() ? ChatColor.RED : ChatColor.DARK_GRAY) + upgrade.getSubName());
            lore.add("");
        }
        lore.add((upgrade.isUnlocked() ? ChatColor.GREEN : ChatColor.GRAY) + upgrade.getDescription() +
                "\n\n" + ChatColor.GRAY + "Cost: " + ChatColor.AQUA + "❂ " + upgrade.getCurrencyCost());
        return new ItemBuilder(masterUpgrade.isUnlocked() ? new ItemStack(Material.WOOL, 1, (short) 1) : new ItemStack(Material.WOOL))
                .name(ChatColor.GOLD + ChatColor.BOLD.toString() + masterUpgrade.getName())
                .lore(lore)
                .get();
    }

    private ItemStack branchItem(Upgrade upgrade) {
        return new ItemBuilder(upgrade.isUnlocked() ? new ItemStack(Material.WOOL, 1, (short) 1) : new ItemStack(Material.WOOL, 1, (short) 8))
                .name((upgrade.isUnlocked() ? ChatColor.GOLD : ChatColor.RED) + upgrade.getName())
                .lore((upgrade.isUnlocked() ? ChatColor.GREEN : ChatColor.GRAY) + upgrade.getDescription() +
                        "\n\n" + ChatColor.GRAY + "Cost: " + ChatColor.AQUA + "❂ " + upgrade.getCurrencyCost())
                .get();
    }

    private void globalAnnouncement(Game game, Upgrade upgrade, T ability) {
        game.forEachOnlinePlayerWithoutSpectators((p, t) -> {
            if (upgrade.getName().equals("Master Upgrade") || (upgrade.getSubName() != null && upgrade.getSubName().contains("Master Upgrade"))) {
                p.sendMessage(
                        ChatColor.GOLD + abilityTree.getPlayer().getName() + " §ehas unlocked §6" +
                        ability.getName() + " - §c§l" +
                        upgrade.getName() + "§e!"
                );
            } else {
                p.sendMessage(
                        ChatColor.GOLD + abilityTree.getPlayer().getName() + " §ehas unlocked §6" +
                        ability.getName() + " - " +
                        upgrade.getName() + "§e!"
                );
            }
        });
    }

    public abstract void c1();

    public abstract void c2();

    public abstract void c3();

    public abstract void c4();

    public abstract void master();

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getItemName() {
        return itemName;
    }

    public void updateInventory(WarlordsPlayer wp) {
        wp.weaponRightClick();
        wp.updateRedItem();
        wp.updatePurpleItem();
        wp.updateBlueItem();
        wp.updateOrangeItem();
        ability.updateDescription((Player) wp.getEntity());
    }

    public int getMaxUpgrades() {
        return maxUpgrades;
    }

    public void setMaxUpgrades(int maxUpgrades) {
        this.maxUpgrades = maxUpgrades;
    }
}
