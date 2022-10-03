package com.ebicep.warlords.pve.weapons.menu;

import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.database.repositories.player.pojos.pve.DatabasePlayerPvE;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.pve.Currencies;
import com.ebicep.warlords.pve.weapons.AbstractTierOneWeapon;
import com.ebicep.warlords.pve.weapons.AbstractTierTwoWeapon;
import com.ebicep.warlords.pve.weapons.AbstractWeapon;
import com.ebicep.warlords.pve.weapons.WeaponsPvE;
import com.ebicep.warlords.pve.weapons.weaponaddons.Salvageable;
import com.ebicep.warlords.pve.weapons.weaponaddons.StatsRerollable;
import com.ebicep.warlords.pve.weapons.weaponaddons.Upgradeable;
import com.ebicep.warlords.pve.weapons.weaponaddons.WeaponScore;
import com.ebicep.warlords.pve.weapons.weapontypes.StarterWeapon;
import com.ebicep.warlords.pve.weapons.weapontypes.legendaries.AbstractLegendaryWeapon;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.java.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.ebicep.warlords.menu.Menu.*;
import static com.ebicep.warlords.pve.weapons.menu.WeaponBindMenu.openWeaponBindMenu;

public class WeaponManagerMenu {

    public static final List<Currencies> STAR_PIECES = Arrays.asList(
            Currencies.COMMON_STAR_PIECE,
            Currencies.RARE_STAR_PIECE,
            Currencies.EPIC_STAR_PIECE,
            Currencies.LEGENDARY_STAR_PIECE
    );
    public static HashMap<UUID, PlayerMenuSettings> playerMenuSettings = new HashMap<>();

    public static void openWeaponInventoryFromExternal(Player player) {
        UUID uuid = player.getUniqueId();
        DatabaseManager.getPlayer(uuid, databasePlayer -> {
            List<AbstractWeapon> weaponInventory = databasePlayer.getPveStats().getWeaponInventory();

            playerMenuSettings.putIfAbsent(uuid, new PlayerMenuSettings());
            PlayerMenuSettings menuSettings = playerMenuSettings.get(uuid);
            menuSettings.setWeaponInventory(weaponInventory);
            menuSettings.sort();

            openWeaponInventoryFromInternal(player, databasePlayer);
        });
    }

    public static void openWeaponInventoryFromInternal(Player player, DatabasePlayer databasePlayer) {
        PlayerMenuSettings menuSettings = playerMenuSettings.get(player.getUniqueId());
        int page = menuSettings.getPage();
        menuSettings.sort();
        List<AbstractWeapon> weaponInventory = new ArrayList<>(menuSettings.getSortedWeaponInventory());
        weaponInventory.removeIf(weapon -> weapon instanceof StarterWeapon);

        SortOptions sortedBy = menuSettings.getSortOption();
        WeaponsPvE filterBy = menuSettings.getRarityFilter();

        Menu menu = new Menu("Weapon Inventory", 9 * 6);

        for (int i = 0; i < 45; i++) {
            int weaponNumber = ((page - 1) * 45) + i;
            if (weaponNumber < weaponInventory.size()) {
                AbstractWeapon abstractWeapon = weaponInventory.get(weaponNumber);

                int column = i % 9;
                int row = i / 9;

                menu.setItem(column, row,
                        abstractWeapon.generateItemStack(),
                        (m, e) -> openWeaponEditor(player, databasePlayer, abstractWeapon)
                );
            } else {
                break;
            }
        }

        if (page - 1 > 0) {
            menu.setItem(0, 5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "Previous Page")
                            .lore(ChatColor.YELLOW + "Page " + (page - 1))
                            .get(),
                    (m, e) -> {
                        menuSettings.setPage(page - 1);
                        openWeaponInventoryFromInternal(player, databasePlayer);
                    }
            );
        }
        if (weaponInventory.size() > (page * 45)) {
            menu.setItem(8, 5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "Next Page")
                            .lore(ChatColor.YELLOW + "Page " + (page + 1))
                            .get(),
                    (m, e) -> {
                        menuSettings.setPage(page + 1);
                        openWeaponInventoryFromInternal(player, databasePlayer);
                    }
            );
        }

        DatabasePlayerPvE databasePlayerPvE = databasePlayer.getPveStats();
        menu.setItem(1, 5,
                new ItemBuilder(Material.NETHER_STAR)
                        .name(ChatColor.GREEN + "Your Star Pieces")
                        .lore(STAR_PIECES.stream()
                                .map(starPiece -> ChatColor.WHITE.toString() + databasePlayerPvE.getCurrencyValue(starPiece) + " " + starPiece.getColoredName() + (databasePlayerPvE.getCurrencyValue(
                                        starPiece) != 1 ? "s" : ""))
                                .collect(Collectors.joining("\n"))
                        )
                        .get(),
                (m, e) -> {
                }
        );
        menu.setItem(2, 5,
                new ItemBuilder(Material.FIREWORK_CHARGE)
                        .name(Currencies.SKILL_BOOST_MODIFIER.getColoredName() + "s" + ChatColor.GRAY + ": " +
                                ChatColor.WHITE + databasePlayerPvE.getCurrencyValue(Currencies.SKILL_BOOST_MODIFIER))
                        .flags(ItemFlag.HIDE_POTION_EFFECTS)
                        .get(),
                (m, e) -> {
                }
        );
        menu.setItem(3, 5,
                new ItemBuilder(Material.MILK_BUCKET)
                        .name(ChatColor.GREEN + "Reset Settings")
                        .lore(ChatColor.GRAY + "Reset the filter, sort, and order of weapons")
                        .get(),
                (m, e) -> {
                    menuSettings.setRarityFilter(WeaponsPvE.NONE);
                    menuSettings.setSortOption(SortOptions.DATE);
                    menuSettings.setAscending(true);
                    openWeaponInventoryFromInternal(player, databasePlayer);
                }
        );
        menu.setItem(5, 5,
                new ItemBuilder(Material.HOPPER)
                        .name(ChatColor.GREEN + "Filter By")
                        .lore(Arrays.stream(WeaponsPvE.VALUES)
                                .map(value -> (filterBy == value ? ChatColor.AQUA : ChatColor.GRAY) + value.name)
                                .collect(Collectors.joining("\n"))
                        )
                        .get(),
                (m, e) -> {
                    menuSettings.setRarityFilter(filterBy.next());
                    openWeaponInventoryFromInternal(player, databasePlayer);
                }
        );
        menu.setItem(6, 5,
                new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .name(ChatColor.GREEN + "Sort By")
                        .lore(Arrays.stream(SortOptions.VALUES)
                                .map(value -> (sortedBy == value ? ChatColor.AQUA : ChatColor.GRAY) + value.name)
                                .collect(Collectors.joining("\n"))
                        )
                        .get(),
                (m, e) -> {
                    menuSettings.setSortOption(sortedBy.next());
                    openWeaponInventoryFromInternal(player, databasePlayer);
                }
        );
        menu.setItem(7, 5,
                new ItemBuilder(Material.LEVER)
                        .name(ChatColor.GREEN + "Sort Order")
                        .lore(menuSettings.isAscending() ?
                                ChatColor.AQUA + "Ascending\n" + ChatColor.GRAY + "Descending" :
                                ChatColor.GRAY + "Ascending\n" + ChatColor.AQUA + "Descending"
                        )
                        .get(),
                (m, e) -> {
                    menuSettings.setAscending(!menuSettings.isAscending());
                    openWeaponInventoryFromInternal(player, databasePlayer);
                }
        );

        menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openWeaponEditor(Player player, DatabasePlayer databasePlayer, AbstractWeapon weapon) {
        Menu menu = new Menu("Weapon Editor", 9 * 6);

        menu.setItem(
                4,
                0,
                weapon.generateItemStack(),
                (m, e) -> {
                }
        );

        List<Pair<ItemStack, BiConsumer<Menu, InventoryClickEvent>>> weaponOptions = new ArrayList<>();
        //bind common/rare/epic/legendary
        weaponOptions.add(new Pair<>(
                new ItemBuilder(Material.SLIME_BALL)
                        .name(ChatColor.GREEN + "Bind Weapon")
                        .get(),
                (m, e) -> openWeaponBindMenu(player, databasePlayer, weapon)
        ));
        //fairy essence reskin commmon/rare/epic/legendary
        weaponOptions.add(new Pair<>(
                new ItemBuilder(Material.PAINTING)
                        .name(ChatColor.GREEN + "Skin Selector")
                        .get(),
                (m, e) -> WeaponSkinSelectorMenu.openWeaponSkinSelectorMenu(player, databasePlayer, weapon, 1)
        ));
        if (weapon instanceof AbstractTierOneWeapon) {
            //star piece
            weaponOptions.add(new Pair<>(
                    new ItemBuilder(Material.NETHER_STAR)
                            .name(ChatColor.GREEN + "Apply a Star Piece")
                            .get(),
                    (m, e) -> {
                        if (databasePlayer.getPveStats().getCurrencyValue(weapon.getRarity().starPieceCurrency) <= 0) {
                            player.sendMessage(ChatColor.RED + "You do not have a " + weapon.getRarity().starPieceCurrency.getColoredName() + ChatColor.RED + "!");
                            return;
                        }
                        WeaponStarPieceMenu.openWeaponStarPieceMenu(player, databasePlayer, (AbstractTierOneWeapon) weapon);
                    }
            ));
        }
        //salvage common/rare/epic
        if (weapon instanceof Salvageable) {
            weaponOptions.add(new Pair<>(
                    !(weapon instanceof AbstractTierTwoWeapon) ?
                            new ItemBuilder(Material.FURNACE)
                                    .name(ChatColor.GREEN + "Salvage Weapon")
                                    .lore(
                                            ChatColor.GRAY + "Click here to salvage this weapon and claim its materials.",
                                            "",
                                            ChatColor.YELLOW + "Shift-Click" + ChatColor.GRAY + " to instantly salvage this weapon.",
                                            "",
                                            ChatColor.GREEN + "Rewards: " + ((Salvageable) weapon).getSalvageRewardMessage(),
                                            "",
                                            ChatColor.RED + "WARNING: " + ChatColor.GRAY + "This action cannot be undone."
                                    )
                                    .get() :
                            new ItemBuilder(Material.FURNACE)
                                    .name(ChatColor.GREEN + "Salvage Weapon")
                                    .lore(
                                            ChatColor.GRAY + "Click here to salvage this weapon and claim its materials.",
                                            "",
                                            ChatColor.GREEN + "Rewards: " + ((Salvageable) weapon).getSalvageRewardMessage(),
                                            "",
                                            ChatColor.RED + "WARNING: " + ChatColor.GRAY + "This action cannot be undone."
                                    )
                                    .get(),
                    (m, e) -> {
                        if (weapon.isBound()) {
                            player.sendMessage(ChatColor.RED + "You cannot salvage a bound weapon!");
                            return;
                        }
                        Specializations weaponSpec = weapon.getSpecializations();
                        List<AbstractWeapon> sameSpecWeapons = databasePlayer.getPveStats().getWeaponInventory()
                                .stream()
                                .filter(w -> w.getSpecializations() == weaponSpec)
                                .collect(Collectors.toList());
                        if (sameSpecWeapons.size() == 1) {
                            player.sendMessage(ChatColor.RED + "You cannot salvage this weapon because you need to have at least one for each specialization!");
                            return;
                        }

                        if (!(weapon instanceof AbstractTierTwoWeapon) && e.isShiftClick()) {
                            WeaponSalvageMenu.salvageWeapon(player, databasePlayer, (AbstractWeapon & Salvageable) weapon);
                            openWeaponInventoryFromInternal(player, databasePlayer);
                        } else {
                            WeaponSalvageMenu.openWeaponSalvageConfirmMenu(player, databasePlayer, (AbstractWeapon & Salvageable) weapon);
                        }
                    }
            ));
        }
        //reroll common/rare/epic
        if (weapon instanceof StatsRerollable) {
            weaponOptions.add(new Pair<>(
                    new ItemBuilder(Material.WORKBENCH)
                            .name(ChatColor.GREEN + "Weapon Stats Reroll")
                            .get(),
                    (m, e) -> WeaponRerollMenu.openWeaponRerollMenu(player, databasePlayer, weapon)
            ));
        }
        //upgrade epic/legendary
        if (weapon instanceof Upgradeable) {
            weaponOptions.add(new Pair<>(
                    new ItemBuilder(Material.ANVIL)
                            .name(ChatColor.GREEN + "Upgrade Weapon")
                            .get(),
                    (m, e) -> {
                        if (((Upgradeable) weapon).getUpgradeLevel() >= ((Upgradeable) weapon).getMaxUpgradeLevel()) {
                            player.sendMessage(ChatColor.RED + "You can't upgrade this weapon anymore.");
                            return;
                        }
                        WeaponUpgradeMenu.openWeaponUpgradeMenu(player, databasePlayer, (AbstractWeapon & Upgradeable) weapon);
                    }
            ));
        }
        if (weapon instanceof AbstractLegendaryWeapon) {
            weaponOptions.add(new Pair<>(
                    new ItemBuilder(Material.NAME_TAG)
                            .name(ChatColor.GREEN + "Apply Title to Weapon")
                            .get(),
                    (m, e) -> {
                        WeaponTitleMenu.openWeaponTitleMenu(player, databasePlayer, (AbstractLegendaryWeapon) weapon, 1);
                    }
            ));
            weaponOptions.add(new Pair<>(
                    new ItemBuilder(Material.BOOKSHELF)
                            .name(ChatColor.GREEN + "Change Skill Boost")
                            .get(),
                    (m, e) -> {
                        WeaponSkillBoostMenu.openWeaponSkillBoostMenu(player, databasePlayer, (AbstractLegendaryWeapon) weapon);
                    }
            ));
        }

        int x = 2;
        int y = 1;
        for (Pair<ItemStack, BiConsumer<Menu, InventoryClickEvent>> option : weaponOptions) {
            menu.setItem(
                    x,
                    y,
                    option.getA(),
                    option.getB()
            );
            x += 2;
            if (x > 6) {
                x = 2;
                y += 2;
            }
        }

        menu.setItem(4, 5, MENU_BACK, (m, e) -> openWeaponInventoryFromInternal(player, databasePlayer));
        menu.openForPlayer(player);
    }

    public enum SortOptions {

        DATE("Date", (o1, o2) -> o1.getDate().compareTo(o2.getDate())),
        RARITY("Rarity", (o1, o2) -> o1.getRarity().compareTo(o2.getRarity())),
        WEAPON_SCORE("Weapon Score", (o1, o2) -> {
            //first check if implements WeaponScore
            if (o1 instanceof WeaponScore && o2 instanceof WeaponScore) {
                return Double.compare(((WeaponScore) o1).getWeaponScore(), ((WeaponScore) o2).getWeaponScore());
            } else {
                //check whether one of them implements WeaponScore
                if (o1 instanceof WeaponScore) {
                    return 1;
                } else if (o2 instanceof WeaponScore) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }),

        ;

        private static final SortOptions[] VALUES = values();
        public final String name;
        public final Comparator<AbstractWeapon> comparator;

        SortOptions(String name, Comparator<AbstractWeapon> comparator) {
            this.name = name;
            this.comparator = comparator;
        }

        public SortOptions next() {
            return VALUES[(this.ordinal() + 1) % VALUES.length];
        }
    }

    static class PlayerMenuSettings {
        private int page = 1;
        private List<AbstractWeapon> weaponInventory = new ArrayList<>();
        private List<AbstractWeapon> sortedWeaponInventory = new ArrayList<>();
        private WeaponsPvE rarityFilter = WeaponsPvE.NONE;
        private SortOptions sortOption = SortOptions.DATE;
        private boolean ascending = true; //ascending = smallest -> largest/recent

        public void sort() {
            sortedWeaponInventory = new ArrayList<>(weaponInventory);
            if (rarityFilter != WeaponsPvE.NONE) {
                sortedWeaponInventory.removeIf(weapon -> weapon.getRarity() != rarityFilter);
            }
            sortedWeaponInventory.sort(sortOption.comparator);
            if (!ascending) {
                Collections.reverse(sortedWeaponInventory);
            }
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public List<AbstractWeapon> getSortedWeaponInventory() {
            return sortedWeaponInventory;
        }

        public void setWeaponInventory(List<AbstractWeapon> weaponInventory) {
            this.weaponInventory = weaponInventory;
            this.sortedWeaponInventory = new ArrayList<>(weaponInventory);
        }

        public WeaponsPvE getRarityFilter() {
            return rarityFilter;
        }

        public void setRarityFilter(WeaponsPvE rarityFilter) {
            this.rarityFilter = rarityFilter;
        }

        public SortOptions getSortOption() {
            return sortOption;
        }

        public void setSortOption(SortOptions sortOption) {
            this.sortOption = sortOption;
        }

        public boolean isAscending() {
            return ascending;
        }

        public void setAscending(boolean ascending) {
            this.ascending = ascending;
        }


    }


}
