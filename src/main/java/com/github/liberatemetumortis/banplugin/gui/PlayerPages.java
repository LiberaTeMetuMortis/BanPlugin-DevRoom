package com.github.liberatemetumortis.banplugin.gui;

import com.github.liberatemetumortis.banplugin.BanPlugin;
import com.github.liberatemetumortis.banplugin.Utils;
import com.github.liberatemetumortis.banplugin.database.ModerationRecord;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class PlayerPages {
    ArrayList<Page> pages = new ArrayList<>();
    private int currentPage = 0;
    private final Player senderPlayer;
    public PlayerPages(Player senderPlayer, ArrayList<ModerationRecord> records) {
        this.senderPlayer = senderPlayer;
        FileConfiguration config = BanPlugin.getInstance().getConfig();
        ArrayList<ModerationRecord> reverseRecords = Utils.reverseModerationRecordList(records);
        for(int i = 0; i < reverseRecords.size(); i += 28) {
            pages.add(new Page(reverseRecords.subList(i, Math.min(i + 28, reverseRecords.size())).stream().map(ModerationRecord::getGUIItem).toList(), this));
        }
        for(Page page : pages) { // Add next and previous page buttons
            ItemStack previousPage = new ItemStack(Material.getMaterial(config.getString("gui.previousPage.material")));
            ItemMeta previousPageMeta = previousPage.getItemMeta();
            previousPageMeta.setDisplayName(Utils.translateColors(config.getString("gui.previousPage.name")));
            previousPageMeta.setLore(config.getStringList("gui.previousPage.lore").stream().map(Utils::translateColors).toList());
            previousPage.setItemMeta(previousPageMeta);
            page.getInventory().setItem(45, previousPage);
            ItemStack nextPage = new ItemStack(Material.getMaterial(config.getString("gui.nextPage.material")));
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(Utils.translateColors(config.getString("gui.nextPage.name")));
            nextPageMeta.setLore(config.getStringList("gui.nextPage.lore").stream().map(Utils::translateColors).toList());
            nextPage.setItemMeta(nextPageMeta);
            page.getInventory().setItem(53, nextPage);
        }
        senderPlayer.openInventory(pages.get(currentPage).getInventory());
    }
    public void previousPage() {
        if(currentPage + 1 < this.pages.size()) {
            this.currentPage++;
            this.senderPlayer.closeInventory();
            this.senderPlayer.openInventory(this.pages.get(this.currentPage).getInventory());
        }
    }

    public void nextPage() {
        if(currentPage - 1 >= 0) {
            this.currentPage--;
            this.senderPlayer.closeInventory();
            this.senderPlayer.openInventory(this.pages.get(this.currentPage).getInventory());
        }
    }
}
