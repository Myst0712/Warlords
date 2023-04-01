package com.ebicep.warlords.pve.upgrades;

import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.player.ingame.WarlordsPlayer;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.bukkit.WordWrap;
import com.ebicep.warlords.util.chat.DefaultFontInfo;
import com.ebicep.warlords.util.java.NumberFormat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ebicep.warlords.menu.Menu.*;

public class AbilityTree {

    private final WarlordsPlayer player;
    private final List<AbstractUpgradeBranch<?>> upgradeBranches = new ArrayList<>();
    private final List<UpgradeLog> upgradeLog = new ArrayList<>();
    private AutoUpgradeProfile autoUpgradeProfile = new AutoUpgradeProfile();

    private int maxMasterUpgrades = 3;

    public AbilityTree(WarlordsPlayer player) {
        this.player = player;
    }

    public void openAbilityTree() {
        Menu menu = new Menu("Upgrades", 9 * 5);

        for (int i = 0; i < upgradeBranches.size(); i++) {
            AbstractUpgradeBranch<?> upgradeBranch = upgradeBranches.get(i);
            menu.setItem(
                    i + 2,
                    2,
                    new ItemBuilder(upgradeBranch.getItemStack())
                            .name(ChatColor.GOLD + upgradeBranch.getItemName())
                            .lore(
                                    ChatColor.GRAY + "Upgrades Remaining: " + ChatColor.GREEN + upgradeBranch.getMaxUpgrades(),
                                    ChatColor.GRAY + "Free Upgrades Available: " + ChatColor.GREEN + upgradeBranch.getFreeUpgrades(),
                                    "",
                                    getUpgradeTreeInfo(upgradeBranch, upgradeBranch.getTreeA()),
                                    "",
                                    getUpgradeTreeInfo(upgradeBranch, upgradeBranch.getTreeB()),
                                    "",
                                    getMasterUpgradeTreeInfo(upgradeBranch, upgradeBranch.getMasterUpgrade()),
                                    "",
                                    ChatColor.GRAY + ">> Click to open ability upgrade tree. <<"
                            )
                            .get(),
                    (m, e) -> upgradeBranch.openUpgradeBranchMenu()
            );
        }
        menu.setItem(4, 0,
                new ItemBuilder(Material.GOLD_INGOT)
                        .name(ChatColor.GRAY + "Insignia: " + ChatColor.GOLD + "❂ " + NumberFormat.addCommas(player.getCurrency()))
                        .lore(
                                ChatColor.GRAY + "Master Upgrades Remaining: " + ChatColor.GOLD + maxMasterUpgrades,
                                "",
                                ChatColor.RED + "■ " + "Upgrade is locked",
                                ChatColor.GREEN + "■ " + "Upgrade is unlocked",
                                ChatColor.YELLOW + "■ " + "You have enough insignia to upgrade",
                                ChatColor.GRAY + "■ " + "You don't meet the requirements to upgrade"
                        )
                        .get(),
                ACTION_DO_NOTHING
        );

        menu.setItem(
                0,
                4,
                new ItemBuilder(Material.BOOKSHELF)
                        .name(ChatColor.GREEN + "Auto Upgrade Queue")
                        .lore(autoUpgradeProfile.getAutoUpgradeEntries().isEmpty() ?
                              Collections.singletonList(WordWrap.wrapWithNewline(
                                      ChatColor.GRAY + "You have no upgrades queued. " +
                                              ChatColor.YELLOW + ChatColor.BOLD + "RIGHT-CLICK " +
                                              ChatColor.GRAY + "upgrades to add/remove them.", 130)
                              ) : autoUpgradeProfile.getLore(this)
                        )
                        .get(),
                (m, e) -> {

                }
        );
        DatabaseManager.getPlayer(player.getUuid(), databasePlayer -> {
            List<AutoUpgradeProfile> autoUpgradeProfiles = databasePlayer
                    .getPveStats()
                    .getAutoUpgradeProfiles()
                    .computeIfAbsent(player.getSpecClass(), k -> new ArrayList<>());
            if (autoUpgradeProfile.getAutoUpgradeEntries().isEmpty() ||
                    !autoUpgradeProfiles.isEmpty() &&
                            autoUpgradeProfiles.get(0).equals(autoUpgradeProfile)
            ) {
                return;
            }
            boolean autoSave = databasePlayer.getPveStats().isAutoSaveUpgradeProfile();
            menu.setItem(
                    2,
                    4,
                    new ItemBuilder(autoSave ? Material.BOOK_AND_QUILL : Material.BOOK)
                            .name(ChatColor.GREEN + "Save auto upgrade queue to spec")
                            .lore(
                                    "",
                                    ChatColor.YELLOW.toString() + ChatColor.BOLD + "LEFT-CLICK" + ChatColor.GRAY + " to save.",
                                    ChatColor.YELLOW.toString() + ChatColor.BOLD + "RIGHT-CLICK" +
                                            ChatColor.GRAY + " to " + (autoSave ? "disable" : "enable") + " auto save."
                            )
                            .get(),
                    (m, e) -> {
                        autoUpgradeProfiles.clear();
                        autoUpgradeProfiles.add(new AutoUpgradeProfile(autoUpgradeProfile));
                        player.sendMessage(AutoUpgradeProfile.AUTO_UPGRADE_PREFIX + ChatColor.GREEN + "Current auto upgrade queue saved to " + player.getSpecClass().name);
                        DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
                        openAbilityTree();
                    }
            );
        });
        if (!autoUpgradeProfile.getAutoUpgradeEntries().isEmpty()) {
            menu.setItem(
                    1,
                    4,
                    new ItemBuilder(Material.PAPER)
                            .name(ChatColor.GREEN + "Click to clear the auto upgrade queue")
                            .get(),
                    (m, e) -> {
                        autoUpgradeProfile.getAutoUpgradeEntries().clear();
                        openAbilityTree();
                    }
            );
        }
        menu.setItem(4, 4, MENU_CLOSE, ACTION_CLOSE_MENU);
        if (player.getEntity() instanceof Player) {
            menu.openForPlayer((Player) player.getEntity());
        }
    }

    public String getUpgradeTreeInfo(AbstractUpgradeBranch<?> upgradeBranch, List<Upgrade> upgrades) {
        if (upgrades.isEmpty()) {
            return "No upgrades available.";
        }
        String upgradeName = upgrades.get(0).getName();
        Upgrade lastUpgraded = null;
        for (int i = upgrades.size() - 1; i >= 0; i--) {
            Upgrade upgrade = upgrades.get(i);
            if (upgrade.isUnlocked()) {
                lastUpgraded = upgrade;
                break;
            }
        }
        StringBuilder output = new StringBuilder((lastUpgraded == null ? ChatColor.RED : ChatColor.GOLD) + upgradeName.substring(0,
                upgradeName.indexOf(" - ")
        ) + " ");
        int currency = getPlayer().getCurrency();
        int maxUpgrades = upgradeBranch.getMaxUpgrades();
        for (Upgrade upgrade : upgrades) {
            if (upgrade.isUnlocked()) {
                output.append(ChatColor.GREEN);
            } else {
                if (maxUpgrades <= 0) {
                    output.append(ChatColor.RED);
                } else if (currency >= upgrade.getCurrencyCost()) {
                    currency -= upgrade.getCurrencyCost();
                    output.append(ChatColor.YELLOW);
                } else {
                    output.append(ChatColor.GRAY);
                }
                maxUpgrades--;
            }
            output.append("■");
        }
        if (lastUpgraded != null) {
            output.append(("\n" + lastUpgraded.getDescription()).replaceAll("\n", "\n " + ChatColor.GREEN));
        }
        /*
//        output.append("\n");
        for (int i = upgrades.size() - 1; i >= 0; i--) {
            Upgrade upgrade = upgrades.get(i);
            if (upgrade.isUnlocked()) {
//                Upgrade nextUpgrade = i + 1 < upgrades.size() ? upgrades.get(i + 1) : null;
//                if (upgradeAfter != null) {
//                    String[] upgradeLore = upgrade.getDescription().split("\n");
//                    String[] nextUpgradeLore = upgradeAfter.getDescription().split("\n");
//                    for (int j = 0; j < Math.max(upgradeLore.length, nextUpgradeLore.length); j++) {
//                        if (j < upgradeLore.length && j < nextUpgradeLore.length) {
//                            getBlockedLore(output, upgradeLore[j], nextUpgradeLore[j]);
//                        } else if (j < upgradeLore.length) {
//                            getBlockedLore(output, upgradeLore[j], "");
//                        } else {
//                            getBlockedLore(output, "", nextUpgradeLore[j]);
//                        }
//                        output.append("\n");
//                    }
//                } else {
//                    output.append(("\n" + upgrade.getDescription()).replaceAll("\n", "\n " + ChatColor.GREEN));
//                }
                output.append(("\n" + upgrade.getDescription()).replaceAll("\n", "\n " + ChatColor.GREEN));
//                if (nextUpgrade != null) {
//                    //output.append("\n");
//                    output.append(("\n" + nextUpgrade.getDescription()).replaceAll("\n", "\n " + ChatColor.GRAY));
//                }
                break;
            }
        }
         */
        return output.toString();
    }

    public String getMasterUpgradeTreeInfo(AbstractUpgradeBranch<?> upgradeBranch, Upgrade upgrade) {
        if (upgrade == null) {
            return "No upgrades";
        }
        StringBuilder output = new StringBuilder((upgrade.isUnlocked() ? ChatColor.GOLD : ChatColor.RED).toString() + ChatColor.BOLD + upgrade.getName() + " ");
        if (upgrade.isUnlocked()) {
            output.append(ChatColor.GREEN);
        } else {
            if (maxMasterUpgrades <= 0) {
                output.append(ChatColor.RED);
            } else if (upgradeBranch.getMaxUpgrades() == 0 && getPlayer().getCurrency() >= upgrade.getCurrencyCost()) {
                output.append(ChatColor.YELLOW);
            } else {
                output.append(ChatColor.GRAY);
            }
        }
        output.append("■");
        if (upgrade.isUnlocked()) {
            output.append(("\n" + upgrade.getDescription()).replaceAll("\n", "\n " + ChatColor.GREEN));
        }
        return output.toString();
    }

    public WarlordsPlayer getPlayer() {
        return player;
    }

    public void getBlockedLore(StringBuilder output, String upgradeLore, String nextUpgradeLore) {
        int loreLength = 250;
        int upgradeLoreLength = DefaultFontInfo.getStringLength(upgradeLore);
        int nextUpgradeLoreLength = DefaultFontInfo.getStringLength(nextUpgradeLore);
        int space = loreLength - upgradeLoreLength - nextUpgradeLoreLength;
        if (space < 0) {
            return;
        }
        output.append(upgradeLore);
        output.append(" ".repeat(Math.round(space / 3f) + 1));
        output.append(nextUpgradeLore);
    }

    public List<AbstractUpgradeBranch<?>> getUpgradeBranches() {
        return upgradeBranches;
    }

    public List<UpgradeLog> getUpgradeLog() {
        return upgradeLog;
    }

    public int getMaxMasterUpgrades() {
        return maxMasterUpgrades;
    }

    public void setMaxMasterUpgrades(int maxMasterUpgrades) {
        this.maxMasterUpgrades = maxMasterUpgrades;
    }

    public AutoUpgradeProfile getAutoUpgradeProfile() {
        return autoUpgradeProfile;
    }

    public void setAutoUpgradeProfile(AutoUpgradeProfile autoUpgradeProfile) {
        this.autoUpgradeProfile = autoUpgradeProfile;
    }

    public static class UpgradeLog {

        @Field("time_elapsed")
        private int gameTimeLeft;
        @Field("name")
        private String upgradeName;
        @Field("description")
        private String upgradeDescription;

        public UpgradeLog(int gameTimeLeft, String upgradeName, String upgradeDescription) {
            this.gameTimeLeft = gameTimeLeft;
            this.upgradeName = upgradeName;
            this.upgradeDescription = upgradeDescription;
        }
    }
}
