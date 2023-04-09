package com.github.liberatemetumortis.banplugin.handlers;

import com.github.liberatemetumortis.banplugin.BanPlugin;
import com.github.liberatemetumortis.banplugin.Utils;
import com.github.liberatemetumortis.banplugin.gui.Page;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import static com.github.liberatemetumortis.banplugin.database.ModerationRecord.isBanned;

public class EventHandlers implements Listener {
    private final BanPlugin plugin;
    public EventHandlers(BanPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        ConfigurationSection messages = plugin.getConfig().getConfigurationSection("messages");
        // Check if player is banned
        if(isBanned(event.getPlayer())){
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Utils.translateColors(messages.getString("loginDisallowed")));
        }
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        if(event.getClickedInventory().getHolder() instanceof Page) {
            event.setCancelled(true);
            if(event.getSlot() == 45) {
                ((Page) event.getClickedInventory().getHolder()).getPlayerPages().nextPage();
            } else if(event.getSlot() == 53) {
                ((Page) event.getClickedInventory().getHolder()).getPlayerPages().previousPage();
            }
        }
    }
}
