package com.github.liberatemetumortis.banplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandHandlers {
    public CommandHandlers(BanPlugin plugin) {
        plugin.getCommand("ban").setExecutor(new BanCommand(plugin));
        plugin.getCommand("unban").setExecutor(new UnbanCommand(plugin));
    }
    static class BanCommand implements CommandExecutor {
        private final BanPlugin plugin;
        public BanCommand(BanPlugin plugin) {
            this.plugin = plugin;
        }
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return false;
        }
    }
    
    static class UnbanCommand implements CommandExecutor {
        private final BanPlugin plugin;
        public UnbanCommand(BanPlugin plugin) {
            this.plugin = plugin;
        }
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return false;
        }
    }
}
