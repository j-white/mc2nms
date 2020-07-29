package org.opennms.mc2nms;
import org.bukkit.ChatColor;

public class ChatHelper {
    static final String prefix = "&e&lmc2nms &7➤ ";

    public static String format(String input) {
        return ChatColor.translateAlternateColorCodes('&', prefix+input);
    }
}
