package com.awokens.movingborder.Commands.Default;

import com.awokens.movingborder.Manager.WikiManager;
import com.awokens.movingborder.MovingBorder;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class WikiCmd {

    private final MovingBorder plugin;
    public WikiCmd(MovingBorder plugin) {
        this.plugin = plugin;

        WikiManager wiki = new WikiManager();
        wiki.setTitle("Wiki");
        wiki.setAuthor("Awokens");

        List<String> pages = new ArrayList<>();
        pages.add("<bold>      BORDER WIKI</bold><newline><newline>\n" +
                "Here you will find every custom feature made on here " +
                "<gold>The Spring Edition</gold>.<newline><newline>Remember," +
                " you can hover some text for more info if displayed.<newline>" +
                "<newline>Enjoy reading,<newline><blue>Awokens</blue>");
        pages.add("""
                <bold>THE BASICS</bold>
                • Each world has a mob controlling the world border's position. If the mob moves, then the border moves along with it as well. Essentially, this is survival, but encased by a border.
                • Enter /commands to view what commands you have access to""");
        pages.add("""
                <bold>MECHANICS</bold>
                • <hover:show_text:'Chance to drop 1-3 bone meal'>Grass Block</hover>
                • <hover:show_text:'Chance to drop 1-3 carrots'>Short Grass</hover>
                • <hover:show_text:'Increased chance to drop an apple'>Any leaves</hover>
                • <hover:show_text:'- Can frost walk on water
                - Cannot die...?'>Overworld Controller</hover>
                • <hover:show_text:'- Combat tagged for 5 seconds
                - Will die if logged whilst combat tagged'>Combat Tag</hover>"""
        );
        wiki.setPages(pages);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setAuthor(wiki.getAuthor());
        bookMeta.setTitle(wiki.getTitle());

        for (String page : wiki.getPages()) {
            bookMeta.addPages(MiniMessage.miniMessage().deserialize(page));
        }
        book.setItemMeta(bookMeta);

        new CommandAPICommand("wiki")
                .withAliases("help", "tutorial")
                .withFullDescription("Moving Border Book guide")
                .executesPlayer((player, args) -> {
                    player.openBook(book);
                    player.playSound(player, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
                }).register();

    }
}
