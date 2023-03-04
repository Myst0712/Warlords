package com.ebicep.warlords.menu.debugmenu;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.GameMode;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.flags.GroundFlagLocation;
import com.ebicep.warlords.game.flags.PlayerFlagLocation;
import com.ebicep.warlords.game.flags.SpawnFlagLocation;
import com.ebicep.warlords.game.option.marker.DebugLocationMarker;
import com.ebicep.warlords.game.option.marker.FlagHolder;
import com.ebicep.warlords.game.option.marker.LobbyLocationMarker;
import com.ebicep.warlords.game.option.marker.MapSymmetryMarker;
import com.ebicep.warlords.game.state.PlayingState;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.menu.MenuItemPairList;
import com.ebicep.warlords.permissions.PermissionHandler;
import com.ebicep.warlords.player.general.Classes;
import com.ebicep.warlords.player.general.PlayerSettings;
import com.ebicep.warlords.player.general.SkillBoosts;
import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.AbstractCooldown;
import com.ebicep.warlords.player.ingame.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.bukkit.HeadUtils;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.bukkit.WordWrap;
import com.ebicep.warlords.util.bukkit.signgui.SignGUI;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.ebicep.warlords.menu.Menu.*;
import static com.ebicep.warlords.util.chat.ChatChannels.sendDebugMessage;


public class DebugMenuPlayerOptions {

    public static void openPlayerMenu(Player player, WarlordsEntity target) {
        if (target == null) {
            return;
        }
        String targetName = target.getName();
        String coloredName = target.getColoredName();

        Menu menu = new Menu("Player Options: " + targetName, 9 * 5);

        MenuItemPairList firstRow = new MenuItemPairList();

        firstRow.add(new ItemBuilder(Material.EXP_BOTTLE)
                        .name(target.isNoEnergyConsumption() ? ChatColor.GREEN + "Enable Energy Consumption" : ChatColor.RED + "Disable Energy Consumption")
                        .get(),
                (m, e) -> {
                    player.performCommand("wl energy " + (target.isNoEnergyConsumption() ? "disable " : "enable ") + targetName);
                    openPlayerMenu(player, target);
                }
        );
        firstRow.add(new ItemBuilder(Material.INK_SACK, 1, (byte) 8)
                        .name(target.isDisableCooldowns() ? ChatColor.GREEN + "Enable Cooldowns Timers" : ChatColor.RED + "Disable Cooldown Timers")
                        .get(),
                (m, e) -> {
                    player.performCommand("wl cooldown " + (target.isDisableCooldowns() ? "enable " : "disable ") + targetName);
                    openPlayerMenu(player, target);
                }
        );
        firstRow.add(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                        .name(target.isTakeDamage() ? ChatColor.RED + "Disable Taking Damage" : ChatColor.GREEN + "Enable Taking Damage")
                        .get(),
                (m, e) -> {
                    player.performCommand("wl takedamage " + (target.isTakeDamage() ? "disable " : "enable ") + targetName);
                    openPlayerMenu(player, target);
                }
        );
        firstRow.add(new ItemBuilder(Material.RABBIT_FOOT)
                        .name(target.isCanCrit() ? ChatColor.RED + "Disable Crits" : ChatColor.GREEN + "Enable Crits")
                        .get(),
                (m, e) -> {
                    player.performCommand("wl crits " + (target.isCanCrit() ? "disable " : "enable ") + targetName);
                    openPlayerMenu(player, target);
                }
        );
        firstRow.add(new ItemBuilder(Material.AIR)
                        .get(),
                (m, e) -> {
                }
        );
        firstRow.add(new ItemBuilder(new Potion(PotionType.INSTANT_DAMAGE), 1, true)
                        .name(ChatColor.GREEN + "Kill")
                        .flags(ItemFlag.HIDE_POTION_EFFECTS)
                        .get(),
                (m, e) -> {
                    target.addDamageInstance(target, "God", 100000, 100000, 0, 100, false);
                    sendDebugMessage(player, ChatColor.GREEN + "Killed " + targetName, true);
                }
        );
        firstRow.add(new ItemBuilder(Material.WOOL, 1, (short) (PlayerSettings.getPlayerSettings(player.getUniqueId()).getWantedTeam() == Team.BLUE ? 14 : 11))
                        .name(ChatColor.GREEN + "Swap to the " + (PlayerSettings.getPlayerSettings(player.getUniqueId())
                                                                                .getWantedTeam() == Team.BLUE ? Team.RED.coloredPrefix() : Team.BLUE.coloredPrefix()) + ChatColor.GREEN + " team")
                        .get(),
                (m, e) -> {
                    Game game = target.getGame();
                    Team currentTeam = target.getTeam();
                    Team otherTeam = target.getTeam().enemy();
                    game.setPlayerTeam(player, otherTeam);
                    target.setTeam(otherTeam);
                    target.getGame().getState(PlayingState.class).ifPresent(s -> s.updatePlayerName(target));
                    PlayerSettings.getPlayerSettings(target.getUuid()).setWantedTeam(otherTeam);
                    LobbyLocationMarker randomLobbyLocation = LobbyLocationMarker.getRandomLobbyLocation(game, otherTeam);
                    if (randomLobbyLocation != null) {
                        Location teleportDestination = MapSymmetryMarker.getSymmetry(game)
                                                                        .getOppositeLocation(game,
                                                                                currentTeam,
                                                                                otherTeam,
                                                                                target.getLocation(),
                                                                                randomLobbyLocation.getLocation()
                                                                        );
                        target.teleport(teleportDestination);
                    }
                    target.updateArmor();
                    openPlayerMenu(player, target);
                    sendDebugMessage(player,
                            ChatColor.GREEN + "Swapped " + coloredName + ChatColor.GREEN + " to the " + otherTeam.coloredPrefix() + ChatColor.GREEN + " team",
                            true
                    );
                }
        );

        for (int i = 0; i < firstRow.size(); i++) {
            menu.setItem(i + 1, 1, firstRow.get(i).getA(), firstRow.get(i).getB());
        }

        MenuItemPairList secondRow = new MenuItemPairList();
        secondRow.add(new ItemBuilder(Material.SUGAR)
                        .name(ChatColor.GREEN + "Modify Speed")
                        .get(),
                (m, e) -> {
                }
        );
        secondRow.add(new ItemBuilder(new Potion(PotionType.INSTANT_HEAL), 1, true)
                        .name(ChatColor.GREEN + "Add Health")
                        .flags(ItemFlag.HIDE_POTION_EFFECTS)
                        .get(),
                (m, e) -> {
                    SignGUI.open(player, new String[]{"", "^^^^^^^", "Enter heal amount", "greater than 0"}, (p, lines) -> {
                        String amount = lines[0];
                        try {
                            int amountNumber = Integer.parseInt(amount);
                            if (amountNumber < 0) {
                                throw new NumberFormatException();
                            }
                            target.addHealingInstance(target, "God", amountNumber, amountNumber, 0, 100, false, false);
                            sendDebugMessage(player, ChatColor.GREEN + "Healed " + coloredName + ChatColor.GREEN + " for " + amountNumber, true);
                        } catch (NumberFormatException exception) {
                            p.sendMessage(ChatColor.RED + "Invalid number");
                        }
                        openPlayerMenu(player, target);
                    });
                }
        );
        secondRow.add(new ItemBuilder(Material.DIAMOND_SWORD)
                        .name(ChatColor.GREEN + "Take Damage")
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .get(),
                (m, e) -> {
                    SignGUI.open(player, new String[]{"", "^^^^^^^", "Enter damage amount", "greater than 0"}, (p, lines) -> {
                        String amount = lines[0];
                        try {
                            int amountNumber = Integer.parseInt(amount);
                            if (amountNumber < 0) {
                                throw new NumberFormatException();
                            }
                            target.addDamageInstance(target, "God", amountNumber, amountNumber, 0, 100, false);
                            sendDebugMessage(player, ChatColor.GREEN + "Damaged " + coloredName + ChatColor.GREEN + " for " + amountNumber, true);
                        } catch (NumberFormatException exception) {
                            p.sendMessage(ChatColor.RED + "Invalid number");
                        }
                        openPlayerMenu(player, target);
                    });
                }
        );
        secondRow.add(new ItemBuilder(Material.BREWING_STAND_ITEM)
                        .name(ChatColor.GREEN + "Cooldowns")
                        .get(),
                (m, e) -> PlayerOptionMenus.openCooldownsMenu(player, target)
        );
        secondRow.add(new ItemBuilder(Material.EYE_OF_ENDER)
                        .name(ChatColor.GREEN + "Teleport To")
                        .get(),
                (m, e) -> PlayerOptionMenus.openTeleportLocations(player, target)
        );
        secondRow.add(new ItemBuilder(Material.BANNER)
                        .name(ChatColor.GREEN + "Flag Options")
                        .get(),
                (m, e) -> PlayerOptionMenus.openFlagOptionMenu(player, target)
        );
        secondRow.add(new ItemBuilder(Material.NETHER_STAR)
                        .name(ChatColor.GREEN + "Change Spec")
                        .get(),
                (m, e) -> {
                    if (GameMode.isWaveDefense(target.getGame().getGameMode())) {
                        if (PermissionHandler.isAdmin(player)) {
                            sendDebugMessage(player, ChatColor.RED + "Changing spec is not advised.", true);
                        } else {
                            sendDebugMessage(player, ChatColor.RED + "Cannot change spec in wave defense.", true);
                            return;
                        }
                    }
                    PlayerOptionMenus.openSpecMenu(player, target);
                }
        );

        for (int i = 0; i < secondRow.size(); i++) {
            menu.setItem(i + 1, 2, secondRow.get(i).getA(), secondRow.get(i).getB());
        }

        menu.setItem(3, 4, MENU_BACK, (m, e) -> {
            if (player.getUniqueId() == target.getUuid()) {
                DebugMenu.openDebugMenu(player);
            } else {
                DebugMenuTeamOptions.openTeamSelectorMenu(player, target.getGame());
            }
        });
        menu.setItem(4, 4, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    static class PlayerOptionMenus {

        public static void openCooldownsMenu(Player player, WarlordsEntity target) {
            String name = target.getName();
            String coloredName = target.getColoredName();

            int menuY = Math.min(5 + StatusEffectCooldowns.values().length / 7, 6);
            Menu menu = new Menu("Cooldowns: " + name, 9 * menuY);

            MenuItemPairList firstRow = new MenuItemPairList();
            firstRow.add(new ItemBuilder(Material.BEACON)
                            .name(ChatColor.AQUA + "Manage Cooldowns")
                            .get(),
                    (m, e) -> {
                        CooldownOptionMenus.openCooldownManagerMenu(player, target);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.getOpenInventory().getTopInventory().getName().equals("CD Manager: " + name)) {
                                    CooldownOptionMenus.openCooldownManagerMenu(player, target);
                                } else {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Warlords.getInstance(), 20, 20);
                    }
            );
            firstRow.add(new ItemBuilder(Material.MILK_BUCKET)
                            .name(ChatColor.AQUA + "Clear All Cooldowns")
                            .get(),
                    (m, e) -> {
                        target.getCooldownManager().clearAllCooldowns();
                        sendDebugMessage(player, ChatColor.GREEN + "Cleared " + coloredName + ChatColor.GREEN + "'s Cooldowns", true);
                    }
            );

            firstRow.add(new ItemBuilder(Material.MILK_BUCKET)
                            .name(ChatColor.AQUA + "Clear All Buffs")
                            .get(),
                    (m, e) -> {
                        target.getCooldownManager().removeBuffCooldowns();
                        sendDebugMessage(player, ChatColor.GREEN + "Cleared " + coloredName + ChatColor.GREEN + "'s Buffs", true);
                    }
            );
            firstRow.add(new ItemBuilder(Material.MILK_BUCKET)
                            .name(ChatColor.AQUA + "Clear All Debuffs")
                            .get(),
                    (m, e) -> {
                        target.getCooldownManager().removeDebuffCooldowns();
                        sendDebugMessage(player, ChatColor.GREEN + "Cleared " + coloredName + ChatColor.GREEN + "'s Debuffs", true);
                    }
            );
            firstRow.add(new ItemBuilder(Material.MILK_BUCKET)
                            .name(ChatColor.AQUA + "Clear All Abilities")
                            .get(),
                    (m, e) -> {
                        target.getCooldownManager().removeAbilityCooldowns();
                        sendDebugMessage(player, ChatColor.GREEN + "Cleared " + coloredName + ChatColor.GREEN + "'s Ability Cooldowns", true);
                    }
            );

            for (int i = 0; i < firstRow.size(); i++) {
                menu.setItem(i + 1, 1, firstRow.get(i).getA(), firstRow.get(i).getB());
            }

            //effects
            int yLevel = 1;
            for (int i = 0; i < StatusEffectCooldowns.values().length; i++) {
                if (i % 7 == 0) {
                    yLevel++;
                }
                StatusEffectCooldowns cooldown = StatusEffectCooldowns.values()[i];
                menu.setItem((i % 7) + 1, yLevel,
                        new ItemBuilder(cooldown.itemStack)
                                .name(cooldown.color + cooldown.name)
                                .flags(ItemFlag.HIDE_ATTRIBUTES)
                                .get(),
                        (m, e) -> {
                            SignGUI.open(player, new String[]{"", "^^^^^^^", "Enter time of", "cooldown in seconds"}, (p, lines) -> {
                                String amount = lines[0];
                                try {
                                    int amountNumber = Integer.parseInt(amount);
                                    target.getCooldownManager().addRegularCooldown(cooldown.name,
                                            cooldown.actionBarName,
                                            cooldown.cooldownClass,
                                            cooldown.cooldownObject,
                                            target,
                                            cooldown.cooldownType,
                                            cooldownManager -> {
                                            },
                                            amountNumber * 20
                                    );
                                    if (cooldown == StatusEffectCooldowns.SPEED) {
                                        target.addSpeedModifier(target, "Speed Powerup", 40, amountNumber * 20, "BASE");
                                    }
                                    sendDebugMessage(player,
                                            ChatColor.GREEN + "Gave " + coloredName + " " + ChatColor.GREEN + amountNumber + " seconds of " + cooldown.name,
                                            true
                                    );
                                } catch (NumberFormatException exception) {
                                    p.sendMessage(ChatColor.RED + "Invalid number");
                                }
                                openCooldownsMenu(player, target);
                            });
                        }
                );
            }
            menu.setItem(3, menuY - 1, MENU_BACK, (m, e) -> openPlayerMenu(player, target));
            menu.setItem(4, menuY - 1, MENU_CLOSE, ACTION_CLOSE_MENU);
            menu.openForPlayer(player);
        }

        public static void openTeleportLocations(Player player, WarlordsEntity target) {
            Menu menu = new Menu("Teleport To: " + target.getName(), 9 * 5);

            Game game = target.getGame();
            int x = 0;
            int y = 0;
            for (DebugLocationMarker marker : game.getMarkers(DebugLocationMarker.class)) {
                menu.setItem(x, y, marker.getAsItem(), (m, e) -> {
                    target.teleport(marker.getLocation());
                    sendDebugMessage(player, ChatColor.GREEN + "Teleported " + target.getColoredName() + ChatColor.GREEN + " to " + marker.getName(), true);
                });

                x++;

                if (x > 8) {
                    x = 0;
                    y++;
                }
            }
            menu.setItem(3, 4, MENU_BACK, (m, e) -> openPlayerMenu(player, target));
            menu.setItem(4, 4, MENU_CLOSE, ACTION_CLOSE_MENU);
            menu.openForPlayer(player);
        }

        public static void openFlagOptionMenu(Player player, WarlordsEntity target) {
            Menu menu = new Menu("Flag Options: " + target.getName(), 9 * 4);
            int row = 0;
            for (FlagHolder holder : target.getGame().getMarkers(FlagHolder.class)) {
                if (holder.getTeam() == target.getTeam()) {
                    continue;
                }
                row++;
                MenuItemPairList menuItemPairList = new MenuItemPairList();
                menuItemPairList.add(new ItemBuilder(Material.BANNER)
                                .name(ChatColor.GREEN + "Pick Up Flag")
                                .get(),
                        (m, e) -> {
                            if (target.getCarriedFlag() == holder.getInfo()) {
                                sendDebugMessage(player, ChatColor.RED + "That player already has the flag", true);
                            } else {
                                FlagHolder.update(
                                        target.getGame(),
                                        info -> info.getFlag() instanceof PlayerFlagLocation && ((PlayerFlagLocation) info.getFlag()).getPlayer() == target ?
                                                GroundFlagLocation.of(info.getFlag()) :
                                                info == holder.getInfo() ?
                                                PlayerFlagLocation.of(info.getFlag(), target) :
                                                null
                                );
                                sendDebugMessage(player, ChatColor.GREEN + "Picked up the flag for " + target.getColoredName(), true);
                            }
                        }
                );
                menuItemPairList.add(new ItemBuilder(Material.BED)
                                .name(ChatColor.GREEN + "Return the Flag")
                                .get(),
                        (m, e) -> {
                            if (target.getCarriedFlag() == holder.getInfo()) {
                                holder.getInfo().setFlag(new SpawnFlagLocation(holder.getInfo().getSpawnLocation(), null));
                                sendDebugMessage(player, ChatColor.GREEN + "Returned the flag for " + target.getColoredName(), true);
                            } else {
                                sendDebugMessage(player, ChatColor.RED + "That player does not have the flag", true);
                            }
                        }
                );
                menuItemPairList.add(new ItemBuilder(Material.GRASS)
                                .name(ChatColor.GREEN + "Drop Flag")
                                .get(),
                        (m, e) -> {
                            if (target.getCarriedFlag() == holder.getInfo()) {
                                holder.getInfo().setFlag(GroundFlagLocation.of(holder.getFlag()));
                                sendDebugMessage(player, ChatColor.GREEN + "Dropped the flag for " + target.getColoredName(), true);
                            } else {
                                sendDebugMessage(player, ChatColor.RED + "That player does not have the flag", true);
                            }
                        }
                );
                menuItemPairList.add(new ItemBuilder(Material.REDSTONE_COMPARATOR)
                                .name(ChatColor.GREEN + "Set Multiplier")
                                .get(),
                        (m, e) -> {
                            if (target.getCarriedFlag() == holder.getInfo()) {
                                SignGUI.open(player, new String[]{"", "^^^^^^^", "Enter flag %", "0 < % < 10,000"}, (p, lines) -> {
                                    String amount = lines[0];
                                    try {
                                        int amountNumber = Integer.parseInt(amount);
                                        if (amountNumber < 0 || amountNumber > 10000) {
                                            throw new NumberFormatException();
                                        }
                                        if (target.getCarriedFlag() != null) {
                                            PlayerFlagLocation flag = ((PlayerFlagLocation) target.getCarriedFlag().getFlag());
                                            flag.setPickUpTicks(amountNumber * 60);
                                            sendDebugMessage(player,
                                                    ChatColor.GREEN + "Set the " + target.getTeam().name + ChatColor.GREEN + " flag carrier multiplier to " + amount + "%",
                                                    true
                                            );
                                        }
                                    } catch (NumberFormatException exception) {
                                        p.sendMessage(ChatColor.RED + "Invalid number");
                                    }
                                    openFlagOptionMenu(player, target);
                                });
                            } else {
                                sendDebugMessage(player, ChatColor.RED + "That player does not have the flag", true);
                            }
                        }
                );

                for (int i = 0; i < menuItemPairList.size(); i++) {
                    menu.setItem(i + 1, row, menuItemPairList.get(i).getA(), menuItemPairList.get(i).getB());
                }
            }
            menu.setItem(3, 3, MENU_BACK, (m, e) -> openPlayerMenu(player, target));
            menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
            menu.openForPlayer(player);
        }

        public static void openSpecMenu(Player player, WarlordsEntity target) {
            Menu menu = new Menu("Spec Menu: " + target.getName(), 9 * 6);
            Classes[] values = Classes.VALUES;
            for (int i = 0; i < values.length; i++) {
                Classes group = values[i];
                menu.setItem(2, i,
                        new ItemBuilder(group.item)
                                .name(ChatColor.GREEN + group.name)
                                .get(),
                        (m, e) -> {
                        }
                );
                List<Specializations> aClasses = group.subclasses;
                for (int j = 0; j < aClasses.size(); j++) {
                    int finalJ = j;
                    ItemBuilder spec = new ItemBuilder(aClasses.get(j).specType.itemStack).name(ChatColor.GREEN + aClasses.get(j).name);
                    if (target.getSpecClass() == aClasses.get(j)) {
                        spec.enchant(Enchantment.OXYGEN, 1);
                        spec.flags(ItemFlag.HIDE_ENCHANTS);
                    }
                    menu.setItem(4 + j, i,
                            spec.get(),
                            (m, e) -> openSkillBoostMenu(player, target, aClasses.get(finalJ))
                    );
                }
            }
            menu.setItem(3, 5, MENU_BACK, (m, e) -> openPlayerMenu(player, target));
            menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
            menu.openForPlayer(player);
        }

        public static void openSkillBoostMenu(Player player, WarlordsEntity target, Specializations selectedSpec) {
            Menu menu = new Menu("Skill Boost: " + target.getName(), 9 * 4);
            List<SkillBoosts> values = selectedSpec.skillBoosts;
            for (int i = 0; i < values.size(); i++) {
                SkillBoosts skillBoost = values.get(i);
                menu.setItem(
                        i + 2,
                        1,
                        new ItemBuilder(selectedSpec.specType.itemStack)
                                .name(ChatColor.RED + skillBoost.name + " (" + selectedSpec.name + ")")
                                .lore(WordWrap.wrapWithNewline(skillBoost.description, 150),
                                        "",
                                        ChatColor.YELLOW + "Click to select!"
                                ).get(),
                        (m, e) -> {
                            target.setSpec(selectedSpec, skillBoost);

                            target.getGame().getState(PlayingState.class).ifPresent(s -> s.updatePlayerName(target));
                            openSpecMenu(player, target);
                            sendDebugMessage(player,
                                    ChatColor.GREEN + "Changed " + target.getColoredName() + ChatColor.GREEN + "'s spec to " + selectedSpec.name,
                                    true
                            );
                        }
                );

            }
            menu.setItem(3, 3, MENU_BACK, (m, e) -> openSpecMenu(player, target));
            menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
            menu.openForPlayer(player);
        }

        static class CooldownOptionMenus {

            public static void openCooldownManagerMenu(Player player, WarlordsEntity target) {
                //int menuY = Math.min(4 + target.getCooldownManager().getCooldowns().size() / 7, 6); Menu shift annoying
                Menu menu = new Menu("CD Manager: " + target.getName(), 9 * 6);
                //general info
                menu.setItem(4, 0,
                        new ItemBuilder(HeadUtils.getHead(player))
                                .name(ChatColor.GREEN + "Cooldown Stats")
                                .lore(ChatColor.GREEN + "Total Cooldowns: " + target.getCooldownManager().getTotalCooldowns(),
                                        ChatColor.GREEN + "Active Cooldowns: " + target.getCooldownManager().getCooldowns().size()
                                )
                                .get(),
                        (m, e) -> {

                        }
                );
                //cooldowns
                int yLevel = 0;
                List<AbstractCooldown<?>> abstractCooldowns = new ArrayList<>(target.getCooldownManager().getCooldowns());
                abstractCooldowns.sort(Comparator.comparing(abstractCooldown -> abstractCooldown instanceof RegularCooldown ? ((RegularCooldown<?>) abstractCooldown).getTicksLeft() : 0));
                for (int i = 0; i < abstractCooldowns.size(); i++) {
                    if (i % 7 == 0) {
                        yLevel++;
                        if (yLevel > 4) {
                            break;
                        }
                    }
                    AbstractCooldown<?> abstractCooldown = abstractCooldowns.get(i);
                    menu.setItem((i % 7) + 1, yLevel,
                            new ItemBuilder(Utils.getWoolFromIndex(i))
                                    .name(ChatColor.GOLD + abstractCooldown.getName())
                                    .lore(abstractCooldown instanceof RegularCooldown ?
                                          ChatColor.GREEN + "Time Left: " + ChatColor.GOLD + (Math.round(((RegularCooldown<?>) abstractCooldown).getTicksLeft() / 20f * 10) / 10.0) + "s" :
                                          ChatColor.GREEN + "Time Left: " + ChatColor.GOLD + "N/A",
                                            ChatColor.GREEN + "From: " + abstractCooldown.getFrom().getColoredName()
                                    )
                                    .get(),
                            (m, e) -> openCooldownEditorMenu(player, target, abstractCooldown)
                    );
                }
                menu.setItem(3, 5, MENU_BACK, (m, e) -> openCooldownsMenu(player, target));
                menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
                menu.openForPlayer(player);
            }

            public static void openCooldownEditorMenu(Player player, WarlordsEntity target, AbstractCooldown<?> abstractCooldown) {
                Menu menu = new Menu(abstractCooldown.getName() + ": " + target.getName(), 9 * 4);

                MenuItemPairList menuItemPairList = new MenuItemPairList();
                menuItemPairList.add(new ItemBuilder(Material.MILK_BUCKET)
                                .name(ChatColor.AQUA + "Remove")
                                .get(),
                        (m, e) -> {
                            target.getCooldownManager().getCooldowns().remove(abstractCooldown);
                            openCooldownManagerMenu(player, target);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (player.getOpenInventory().getTopInventory().getName().equals("CD Manager: " + target.getName())) {
                                        openCooldownManagerMenu(player, target);
                                    } else {
                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer(Warlords.getInstance(), 20, 20);
                            sendDebugMessage(player,
                                    ChatColor.GREEN + "Removed " + target.getColoredName() + ChatColor.GREEN + "'s " + abstractCooldown.getName() + " cooldown",
                                    true
                            );
                        }
                );
                menuItemPairList.add(new ItemBuilder(Material.REDSTONE)
                                .name(ChatColor.AQUA + "Add duration")
                                .get(),
                        (m, e) -> {
                            if (!target.getCooldownManager().getCooldowns().contains(abstractCooldown)) {
                                openCooldownsMenu(player, target);
                                sendDebugMessage(player, ChatColor.RED + "That cooldown no longer exists", true);
                                return;
                            }
                            if (!(abstractCooldown instanceof RegularCooldown)) {
                                return;
                            }

                            SignGUI.open(player, new String[]{"", "^^^^^^^", "Enter seconds", "to add"}, (p, lines) -> {
                                String amount = lines[0];
                                try {
                                    int amountNumber = Integer.parseInt(amount);
                                    ((RegularCooldown<?>) abstractCooldown).subtractTime(-amountNumber * 20);
                                    sendDebugMessage(player,
                                            ChatColor.GREEN + "Added " + amountNumber + " seconds to " + target.getColoredName() + ChatColor.GREEN + "'s " + abstractCooldown.getName(),
                                            true
                                    );
                                } catch (NumberFormatException exception) {
                                    p.sendMessage(ChatColor.RED + "Invalid number");
                                }
                                openCooldownEditorMenu(player, target, abstractCooldown);
                            });
                        }
                );

                for (int i = 0; i < menuItemPairList.size(); i++) {
                    menu.setItem(i + 1, 1, menuItemPairList.get(i).getA(), menuItemPairList.get(i).getB());
                }

                menu.setItem(3, 3, MENU_BACK, (m, e) -> {
                    openCooldownManagerMenu(player, target);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.getOpenInventory().getTopInventory().getName().equals("CD Manager: " + target.getName())) {
                                openCooldownManagerMenu(player, target);
                            } else {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(Warlords.getInstance(), 20, 20);
                });
                menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
                menu.openForPlayer(player);
            }
        }
    }

}
