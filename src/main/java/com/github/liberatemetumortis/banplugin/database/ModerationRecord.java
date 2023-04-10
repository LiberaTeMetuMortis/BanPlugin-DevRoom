package com.github.liberatemetumortis.banplugin.database;

import com.github.liberatemetumortis.banplugin.BanPlugin;
import com.github.liberatemetumortis.banplugin.Utils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ModerationRecord {
    private static final ArrayList<ModerationRecord> moderationRecords = new ArrayList<>();
    public static ArrayList<ModerationRecord> getRecords(OfflinePlayer player) {
        return new ArrayList<>(moderationRecords.stream().filter(moderationRecord -> moderationRecord.getTargetID().equals(player.getUniqueId().toString())).toList());
    }
    public static boolean isBanned(OfflinePlayer player) {
        ArrayList<ModerationRecord> records = getRecords(player);
        if(records.isEmpty()) return false;
        ModerationRecord lastRecord = records.get(records.size() - 1);
        if(!lastRecord.isBan) return false;
        return lastRecord.getTimeExpires() == -1 || lastRecord.getTimeExpires() > System.currentTimeMillis();
    }
    public static void fetchBanRecords(Database db) {
        try(ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT * FROM moderations")) {
            while (rs.next()) {
                new ModerationRecord(
                        rs.getString("issuerID"),
                        rs.getString("issuerName"),
                        rs.getString("targetID"),
                        rs.getString("targetName"),
                        rs.getString("reason"),
                        rs.getLong("timeIssued"),
                        rs.getLong("timeExpires"),
                        rs.getBoolean("isBan")
                );
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer id = null;
    private final String issuerID;
    private final String issuerName;

    public String getTargetID() {
        return targetID;
    }

    private final String targetID;
    private final String targetName;
    private final String reason;
    private final Long timeIssued;

    public long getTimeExpires() {
        return timeExpires;
    }

    private final Long timeExpires;

    private final boolean isBan;

    public ModerationRecord(String issuerID, String issuerName, String targetID, String targetName, String reason, Long timeIssued, Long timeExpires, boolean isBan) {
        this.issuerID = issuerID;
        this.issuerName = issuerName;
        this.targetID = targetID;
        this.targetName = targetName;
        this.reason = reason;
        this.timeIssued = timeIssued;
        this.timeExpires = timeExpires;
        this.isBan = isBan;
        moderationRecords.add(this);
    }

    public void insertRecord(BanPlugin plugin)  {
        // We don't want to get SQL injected
        new BukkitRunnable(){
            private final Database db = plugin.getDb();
            @Override
            public void run() {
                try(PreparedStatement statement = db.getConnection().prepareStatement("INSERT INTO moderations (issuerID, issuerName, targetID, targetName, reason, timeIssued, timeExpires, isBan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    statement.setString(1, issuerID);
                    statement.setString(2, issuerName);
                    statement.setString(3, targetID);
                    statement.setString(4, targetName);
                    statement.setString(5, reason);
                    statement.setLong(6, timeIssued);
                    statement.setObject(7, timeExpires); // It must be object because it throws NullPointerException if it's null (when I use long)
                    statement.setBoolean(8, isBan);
                    statement.executeUpdate();
                    ResultSet idQuery = db.getConnection().createStatement().executeQuery("SELECT COUNT(*) as cnt FROM moderations;");
                    idQuery.next();
                    id = idQuery.getInt("cnt");
                }catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public ItemStack getGUIItem() {
        FileConfiguration config = BanPlugin.getInstance().getConfig();
        ItemStack item = new ItemStack(Material.getMaterial(config.getString(this.isBan ? "gui.banItem.material" : "gui.unbanItem.material")));
        item.setAmount(1);
        item.setDurability((short) config.getInt(this.isBan ? "gui.banItem.data" : "gui.unbanItem.data", 0));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.translateColors(config.getString(this.isBan ? "gui.banItem.name" : "gui.unbanItem.name")));
        meta.setLore(
                config.getStringList(this.isBan ? "gui.banItem.lore" : "gui.unbanItem.lore").stream()
                        .map(this::replacePlaceholders)
                        .map(Utils::translateColors)
                        .toList()
        );
        item.setItemMeta(meta);
        return item;
    }

    static private class Variables {
        private static final FileConfiguration config = BanPlugin.getInstance().getConfig();
        public static final Supplier<String> getPermanentBanExpireDate = () -> config.getString("permanentBanExpireDate");
        public static final Supplier<String> getTimeZone = () -> config.getString("timeZone");
        public static final Supplier<String> getTimeFormat = () -> config.getString("timeFormat");
        public static final Supplier<String> getNotExpired = () -> config.getString("notExpired");
        public static final Supplier<String> getExpired = () -> config.getString("expired");
    }
    public String replacePlaceholders(String string) {
        String expireTime;
        if (this.timeExpires != null) { // Check if it's a ban
            if (this.timeExpires == -1) { // Check if it's a permanent ban
                expireTime = Variables.getPermanentBanExpireDate.get();
            } else {
                expireTime = Utils.formatTimeWithDelay(Variables.getTimeZone.get(), Variables.getTimeFormat.get(), this.timeExpires-System.currentTimeMillis());
            }
        } else {
            expireTime = "";
        }

        String isExpired;
        if (this.timeExpires != null) { // Check if it's a ban
            if (this.timeExpires == -1) { // Check if it's a permanent ban
                isExpired = Variables.getNotExpired.get();
            } else if (this.timeExpires < System.currentTimeMillis()) { // Check if it's expired
                isExpired = Variables.getExpired.get();
            } else {
                isExpired = Variables.getExpired.get();
            }
        } else {
            isExpired = "";
        }

        return string
                .replace("{issuer}", this.issuerName)
                .replace("{target}", this.targetName)
                .replace("{reason}", this.reason != null ? this.reason : "")
                .replace("{issueTime}", Utils.formatTime(Variables.getTimeZone.get(), Variables.getTimeFormat.get(), this.timeIssued))
                .replace("{expireTime}", expireTime)
                .replace("{isExpired}", isExpired);
    }
}
