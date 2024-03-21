package com.awokens.movingborder.Manager;

import com.awokens.movingborder.MovingBorder;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.buttons.SGButtonListener;
import com.samjakob.spigui.menu.SGMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagManager {

    private final Plugin plugin;
    private static LuckPerms Luckperms;

    private final List<TagType> Tags = new ArrayList<>();

    public TagManager(Plugin plugin) {
        this.plugin = plugin;
        Luckperms = LuckPermsProvider.get();

        Tags.add(new TagType("<blue>♫</blue>", "jam", Statistic.NOTEBLOCK_PLAYED, 1)); // Play 1 music block
        Tags.add(new TagType("<gold>⛏</gold>", "mine", Statistic.MINE_BLOCK, Material.STONE, 1000)); // Break 1,000 blocks
        Tags.add(new TagType("<yellow>⌛</yellow>", "time", Statistic.PLAY_ONE_MINUTE, (20 * 60 * 60 * 24))); // Have 1 day of playtime
        Tags.add(new TagType("<gray>☠</gray>", "death", Statistic.DEATHS, 1000)); // Die 1,000 times
        Tags.add(new TagType("<red>⚔</red>", "kills", Statistic.PLAYER_KILLS, 1000)); // Kill 1,000 players

    }
    private Plugin getPlugin() { return this.plugin; }

    public List<TagType> getTags() {
        return this.Tags;
    }

    public static User getUser(Player player) {
        return Luckperms.getUserManager().getUser(player.getUniqueId());
    }

    public static String getTag(Player player) {

        String suffix = getUser(player).getCachedData().getMetaData().getSuffix();

        if (suffix == null) return "";
        return suffix;
    }

    public static void setTag(Player player, String tag) {

        User user = getUser(player);

        // clear suffixes data
        user.data().clear(NodeType.SUFFIX::matches);

        // fetch existing suffixes
        Map<Integer, String> inheritedSuffixes = user.getCachedData().getMetaData(QueryOptions.nonContextual()).getSuffixes();
        int priority = inheritedSuffixes.keySet().stream().mapToInt(i -> i + 10).max().orElse(10);

        // set new suffix
        Node node = SuffixNode.builder(tag, priority).build();

        // insert new suffix to user data
        user.data().add(node);

        // completable future save
        Luckperms.getUserManager().saveUser(user);
    }

    public static class TagType {

        private final String name;
        private final String permission;

        private final Statistic statType;

        private Material material;

        private final int statGoal;

        public TagType(String name, String permission, Statistic statType, int statGoal) {
            this.name = name;
            this.permission = permission;
            this.statType = statType;
            this.statGoal = statGoal;
        }

        public TagType(String name, String permission, Statistic statType, Material material, int statGoal) {
            this.name = name;
            this.permission = permission;
            this.statType = statType;
            this.material = material;
            this.statGoal = statGoal;
        }

        public boolean hasPassedStatGoal(Player player) {
            if (getStatType().isSubstatistic()) {
                return player.getStatistic(getStatType(), getMaterial()) >= getStatGoal();
            }
            return player.getStatistic(getStatType()) >= getStatGoal();
        }

        public Material getMaterial() {
            return this.material;
        }

        public String getName() {
            return name;
        }

        public String getPermission() {
            return "tags." + permission;
        }

        public Statistic getStatType() {
            return statType;
        }

        public int getStatGoal() {
            return statGoal;
        }


    }

    public static class TagInventory {

        private final SGMenu TagMenu;

        public TagInventory(Player player) {
            this.TagMenu = MovingBorder.GUIManager().create("&8Tags", 6);

            // Populate excluded/outer slots
            ItemStack background = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta backgroundItemMeta = background.getItemMeta();
            backgroundItemMeta.displayName(Component.text(""));
            background.setItemMeta(backgroundItemMeta);

            for (Integer slot : getExcludedSlots()) {
                TagMenu.setButton(slot, new SGButton(background));
            }

            List<SGButton> buttons = new ArrayList<>();

            for (TagType tag : MovingBorder.getTagManager().getTags()) {

                // tag item
                ItemStack icon = new ItemStack(Material.NAME_TAG);
                ItemMeta meta = icon.getItemMeta();
                meta.displayName(MiniMessage.miniMessage().deserialize(tag.getName())
                        .decoration(TextDecoration.ITALIC, false));

                // make currently selected tag glow as indication when viewing tags
                if (getTag(player).equalsIgnoreCase(tag.getName())) {
                    meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                icon.setItemMeta(meta);


                // button listener
                SGButtonListener listener = event -> {

                    // when clicked if player doesn't meet requirements,
                    // it will turn to barrier as indication
                    if (!player.hasPermission(tag.getPermission()) /* donator check*/ && !tag.hasPassedStatGoal(player) /* non-donator check */) {
                        int slot = event.getSlot();
                        event.getInventory().setItem(slot, new ItemStack(Material.BARRIER, 1));
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                        return;
                    }

                    // check if player's current tag is the clicked tag,
                    // then tell them you already have this selected.
                    if (getTag(player).equalsIgnoreCase(tag.getName())) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<red>You already have this tag selected.</red>"
                        ).decoration(TextDecoration.ITALIC, false));
                        return;
                    }

                    // set player's tag to the clicked tag
                    setTag(player, tag.getName());
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "Selected tag " + tag.getName()
                    ).decoration(TextDecoration.ITALIC, false));
                    player.openInventory(new TagInventory(player).getTagInventory()); // refresh inventory
                };

                // button creation w/ listener ^
                SGButton button = new SGButton(icon).withListener(listener);

                // add new button to slots
                buttons.add(button);
            }

            List<Integer> fillableSlots = getFillableSlots();
            SGButton[] page = getTagPage(1, buttons.toArray(new SGButton[0]));
            for (int i = 0; i < page.length; i++) {
                TagMenu.setButton(fillableSlots.get(i), page[i]);
            }

        }

        public List<Integer> getFillableSlots() {
            List<Integer> fillableSlots = new ArrayList<>();

            for (int j = 10; j <= 43; j += 9) {
                for (int i = j; i <= j + 6; i++) {
                    fillableSlots.add(i);
                }
            }
            return fillableSlots;
        }

        public List<Integer> getExcludedSlots() {
            List<Integer> excludedSlots = new ArrayList<>();
            for (int i = 0; i <= 53; i++) {
                if (!getFillableSlots().contains(i)) {
                    excludedSlots.add(i);
                }
            }
            return excludedSlots;
        }

        public SGButton[] getTagPage(int pageNumber, SGButton[] buttonList) {

            SGButton[] buttons = new SGButton[0];

            if (buttonList == null || buttonList.length < 1) return null;

            int itemsPerPage = 28;
            int startIndex = (pageNumber - 1) * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, buttonList.length);

            if (pageNumber < 1 || startIndex >= buttonList.length) {
                return buttons;
            }

            SGButton[] page = new SGButton[endIndex - startIndex];
            System.arraycopy(buttonList, startIndex, page, 0, endIndex - startIndex);

            return page;
        }

        public Inventory getTagInventory() {
            return TagMenu.getInventory();
        }
    }
}
