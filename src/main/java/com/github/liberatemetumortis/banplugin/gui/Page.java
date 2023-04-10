package com.github.liberatemetumortis.banplugin.gui;

import com.github.liberatemetumortis.banplugin.BanPlugin;
import com.github.liberatemetumortis.banplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Page implements InventoryHolder {
    static int[] fillerSlots = {
             0,  1,  2,  3,  4,  5,  6,  7,  8,
             9,                             17,
            18,                             26,
            27,                             35,
            36,                             44,
                46, 47, 48, 49, 50, 51, 52,
    };
    static int[] usableSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public PlayerPages getPlayerPages() {
        return playerPages;
    }

    private PlayerPages playerPages;

    public Page(List<ItemStack> items, PlayerPages playerPages) {
        this.playerPages = playerPages;

        if(items.size() > 28) throw new IllegalArgumentException("Too many items! (Max 28)");
        for(int i = 0; i < items.size(); i++) {
            this.items[i] = items.get(i);
        }

        inventory = Bukkit.createInventory(this, 54, Utils.translateColors(BanPlugin.getInstance().getConfig().getString("gui.title")));
        for (int i = 0; i < this.items.length; i++) {
            inventory.setItem(usableSlots[i], this.items[i]);
        }

        for (int fillerSlot : fillerSlots) {
            inventory.setItem(fillerSlot, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }


    }
    ItemStack[] items = new ItemStack[28];

    private Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
