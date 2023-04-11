package com.github.liberatemetumortis.banplugin.handlers;

import com.github.liberatemetumortis.banplugin.BanPlugin;
import com.github.liberatemetumortis.banplugin.Utils;
import com.github.liberatemetumortis.banplugin.database.ModerationRecord;
import com.github.liberatemetumortis.banplugin.gui.PlayerPages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.github.liberatemetumortis.banplugin.Utils.translateColors;

public class CommandHandlers {
    private static FileConfiguration config;

    public CommandHandlers(BanPlugin plugin) {
        plugin.getCommand("ban").setExecutor(new BanCommand(plugin));
        plugin.getCommand("unban").setExecutor(new UnbanCommand(plugin));
        plugin.getCommand("history").setExecutor(new HistoryCommand(plugin));
        config = plugin.getConfig();
    }

    class BanCommand implements CommandExecutor {
        private final BanPlugin plugin;

        public BanCommand(BanPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

            if (args.length == 0) {
                sender.sendMessage(translateColors(config.getString("usages.ban")));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(translateColors(config.getString("messages.playerNotFound")));
                return true;
            }
            if (args.length == 1) { // No reason or duration
                banPlayer(sender, target, config.getString("defaultReason"), -1L);
                return true;
            }

            // We are sure about arguments length is greater than 1
            StringBuilder reason = new StringBuilder();
            Long timeInMillis = Utils.parseTime(args[1]);
            if (timeInMillis == 0L) {
                for (int i = 1; i < args.length; i++) {
                    reason.append(args[i]).append(" ");
                }
                timeInMillis = -1L;
            }
            else {
                for (int i = 2; i < args.length; i++) {
                    reason.append(args[i]).append(" ");
                }
            }
            banPlayer(sender, target, reason.isEmpty() ? config.getString("defaultReason") : reason.toString(), timeInMillis);
            return true;
        }

        private void banPlayer(CommandSender issuer, Player target, String reason, Long duration) {
            long now = System.currentTimeMillis();
            ModerationRecord moderationRecord;
            if (issuer instanceof Player) {
                moderationRecord = new ModerationRecord(
                        ((Player) issuer).getUniqueId().toString(),
                        issuer.getName(),
                        target.getUniqueId().toString(),
                        target.getName(),
                        reason,
                        now,
                        duration != -1 ? now + duration : -1,
                        true
                );
            } else {
                moderationRecord = new ModerationRecord(
                        "CONSOLE",
                        "CONSOLE",
                        target.getUniqueId().toString(),
                        target.getName(),
                        reason,
                        System.currentTimeMillis(),
                        duration != -1 ? System.currentTimeMillis() + duration : -1,
                        true
                );
            }
            moderationRecord.insertRecord(plugin);
            plugin.getLogger().info("Player named " + target.getName() + " was banned by " + issuer.getName() + " for " + reason + " for " + duration + " milliseconds");
            Bukkit.broadcastMessage(translateColors(
                    moderationRecord.replacePlaceholders(config.getString("messages.banBroadcast"))
            ));
            target.kickPlayer(translateColors(
                    moderationRecord.replacePlaceholders(config.getString("messages.youAreBanned"))
            ));
        }
    }

    class UnbanCommand implements CommandExecutor {
        private final BanPlugin plugin;

        public UnbanCommand(BanPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length == 0) {
                sender.sendMessage(translateColors(config.getString("usages.unban")));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);;
            if (!ModerationRecord.isBanned(target)) {
                sender.sendMessage(translateColors(config.getString("messages.playerNotBanned")));
                return true;
            }
            unbanPlayer(target, sender);
            return true;
        }

        public void unbanPlayer(OfflinePlayer target, CommandSender issuer) {
            ModerationRecord moderationRecord;
            if (issuer instanceof Player) {
                moderationRecord = new ModerationRecord(
                        ((Player) issuer).getUniqueId().toString(),
                        issuer.getName(),
                        target.getUniqueId().toString(),
                        target.getName(),
                        null,
                        System.currentTimeMillis(),
                        null,
                        false
                );
            } else {
                moderationRecord = new ModerationRecord(
                        "CONSOLE",
                        "CONSOLE",
                        target.getUniqueId().toString(),
                        target.getName(),
                        null,
                        System.currentTimeMillis(),
                        null,
                        false
                );
            }
            moderationRecord.insertRecord(plugin);
            plugin.getLogger().info("Player named " + target.getName() + " was unbanned by " + issuer.getName());
            Bukkit.broadcastMessage(translateColors(
                    moderationRecord.replacePlaceholders(config.getString("messages.unbanBroadcast"))
            ));
        }
    }

    class HistoryCommand implements CommandExecutor {
        private final BanPlugin plugin;

        public HistoryCommand(BanPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if(!(sender instanceof  Player)) {
                sender.sendMessage(translateColors(config.getString("messages.playerOnlyCommand")));
                return true;
            }
            Player playerSender = (Player) sender;
            if (args.length == 0) {
                playerSender.sendMessage(translateColors(config.getString("usages.history")));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            ArrayList<ModerationRecord> records = ModerationRecord.getRecords(target);
            if (records.size() == 0) {
                playerSender.sendMessage(translateColors(config.getString("messages.noHistory")));
                return true;
            }
            new PlayerPages(playerSender, records);
            return true;
        }
    }
}

