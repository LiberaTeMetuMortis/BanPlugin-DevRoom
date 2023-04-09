package com.github.liberatemetumortis.banplugin;
import com.github.liberatemetumortis.banplugin.database.ModerationRecord;
import net.md_5.bungee.api.ChatColor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String translateColors(String str) {
        if(str == null) return "";
        String parsedStr = str.replaceAll("\\{(#[0-9A-f]{6})\\}", "&$1");
        if(parsedStr.matches(".*&#[0-9A-f]{6}.*")){
            Pattern pattern = Pattern.compile("&(#[0-9A-f]{6})");
            Matcher matcher = pattern.matcher(parsedStr);
            while(matcher.find()) {
                String colorCode = matcher.group(1);
                ChatColor color = ChatColor.of(colorCode.substring(1));
                parsedStr = parsedStr.replaceFirst(Pattern.quote(matcher.group()), color.toString());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', parsedStr);
    }


    public static long parseTime(String timeString) {
        Pattern DURATION_PATTERN = Pattern.compile("(\\d+)(mo|m|w|d|h|s)");
        long totalMilliseconds = 0;
        Matcher matcher = DURATION_PATTERN.matcher(timeString);
        while (matcher.find()) {
            long duration = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "mo" -> totalMilliseconds += duration * 30 * 24 * 60 * 60 * 1000L;
                case "w" -> totalMilliseconds += duration * 7 * 24 * 60 * 60 * 1000L;
                case "d" -> totalMilliseconds += duration * 24 * 60 * 60 * 1000L;
                case "h" -> totalMilliseconds += duration * 60 * 60 * 1000L;
                case "m" -> totalMilliseconds += duration * 60 * 1000L;
                case "s" -> totalMilliseconds += duration * 1000L;
                default -> {}
            }
        }
        return totalMilliseconds;
    }

    public static String formatTimeWithDelay(String timeZoneId, String timeFormat, long delayInMillis) {
        // Create a ZonedDateTime object for the specified timezone
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(timeZoneId));
        zonedDateTime = zonedDateTime.plusSeconds(delayInMillis / 1000);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
        return zonedDateTime.format(formatter);
    }

    public static String formatTime(String timeZoneId, String timeFormat, long timeInMillis) {
        // Create a ZonedDateTime object for the specified timezone
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timeInMillis), ZoneId.of(timeZoneId));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
        return zonedDateTime.format(formatter);
    }

    public static ArrayList<ModerationRecord> reverseModerationRecordList(ArrayList<ModerationRecord> list) {
        ArrayList<ModerationRecord> reversedList = new ArrayList<>(list.size());
        for (int i = list.size() - 1; i >= 0; i--) {
            reversedList.add(list.get(i));
        }
        return reversedList;
    }
}

