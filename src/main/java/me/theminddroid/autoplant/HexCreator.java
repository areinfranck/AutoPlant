package me.theminddroid.autoplant;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HexCreator {

    private static final Pattern pattern = Pattern.compile("\\[(#[a-fA-F\\d]{6})]");

    public static String generateHexMessage(String message) {
        Matcher matcher = pattern.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, ChatColor.of(matcher.group(1)).toString());
        }
        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}
