package com.github.liberatemetumortis.banplugin.database;

import com.github.liberatemetumortis.banplugin.BanPlugin;
import com.github.liberatemetumortis.banplugin.Utils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModerationRecord {
    private static ArrayList<ModerationRecord> moderationRecords = new ArrayList<>();
    public static ArrayList<ModerationRecord> getRecords(OfflinePlayer player) {
        ArrayList<ModerationRecord> list = new ArrayList<>(moderationRecords.stream().filter(moderationRecord -> moderationRecord.getTargetID().equals(player.getUniqueId().toString())).toList());
        return list;
    }
    public static boolean isBanned(OfflinePlayer player) {
        ArrayList<ModerationRecord> records = getRecords(player);
        if(records.size() == 0) return false;
        ModerationRecord lastRecord = records.get(records.size() - 1);
        if(!lastRecord.isBan) return false;
        return lastRecord.getTimeExpires() == -1 || lastRecord.getTimeExpires() > System.currentTimeMillis();
    }
    public static void fetchBanRecords(Database db) {
        try {
            ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT * FROM moderations");
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

    public void insertRecord(Database db)  {
        // We don't want to get SQL injected
        try {
            PreparedStatement statement = db.getConnection().prepareStatement("INSERT INTO moderations (issuerID, issuerName, targetID, targetName, reason, timeIssued, timeExpires, isBan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
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
            this.id = idQuery.getInt("cnt");
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack getGUIItem() {
        FileConfiguration config = BanPlugin.instance.getConfig();
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

    static private class variables {
        private static FileConfiguration config = BanPlugin.instance.getConfig();
        public static final Supplier<String> getTimeZone = () -> config.getString("timeZone");
        public static final Supplier<String> getTimeFormat = () -> config.getString("timeFormat");
        public static final Supplier<String> getPermanentBanExpireDate = () -> config.getString("permanentBanExpireDate");
        public static final Supplier<String> notExpired = () -> config.getString("notExpired");
        public static final Supplier<String> expired = () -> config.getString("expired");
    }
    public String replacePlaceholders(String string) {
        return string
                .replace("{issuer}", this.issuerName)
                .replace("{target}", this.targetName)
                .replace("{reason}", this.reason != null ? this.reason : "")
                .replace("{issueTime}", Utils.formatTime(variables.getTimeZone.get(), variables.getTimeFormat.get(), this.timeIssued))
                .replace("{expireTime}", this.timeExpires != null ? this.timeExpires == -1 ? variables.getPermanentBanExpireDate.get() : Utils.formatTimeWithDelay(variables.getTimeZone.get(), variables.getTimeFormat.get(), this.timeExpires-System.currentTimeMillis()) : "")
                .replace("{isExpired}", this.timeExpires != null ? this.timeExpires == -1 ? variables.notExpired.get() : this.timeExpires < System.currentTimeMillis() ? variables.expired.get() : variables.notExpired.get() : "");
    }
}
