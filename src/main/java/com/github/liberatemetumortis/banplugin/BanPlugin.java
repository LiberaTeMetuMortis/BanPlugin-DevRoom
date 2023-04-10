package com.github.liberatemetumortis.banplugin;
import com.github.liberatemetumortis.banplugin.database.Database;
import com.github.liberatemetumortis.banplugin.handlers.CommandHandlers;
import com.github.liberatemetumortis.banplugin.handlers.EventHandlers;
import org.bukkit.plugin.java.JavaPlugin;

import static com.github.liberatemetumortis.banplugin.database.ModerationRecord.fetchBanRecords;

public final class BanPlugin extends JavaPlugin {
    public Database getDb() {
        return db;
    }

    public static BanPlugin getInstance() {
        return instance;
    }

    private Database db;
    private static BanPlugin instance;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        db = new Database(this);
        fetchBanRecords(db);
        new EventHandlers(this);
        new CommandHandlers(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
