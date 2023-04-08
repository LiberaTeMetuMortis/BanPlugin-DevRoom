package com.github.liberatemetumortis.banplugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class BanPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new CommandHandlers(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
