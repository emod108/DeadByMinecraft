package me.mod108.deadbyminecraft.utility;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ActionBar {
    private static final TextComponent emptyComponent = new TextComponent("");

    public static void setActionBar(final Player player, final String message) {
        if (message.equals(""))
            return;
        final TextComponent barText = new TextComponent(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, barText);
    }

    public static void resetActionBar(final Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, emptyComponent);
    }
}
