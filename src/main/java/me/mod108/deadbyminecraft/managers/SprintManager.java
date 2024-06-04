package me.mod108.deadbyminecraft.managers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.ArrayList;

// A class which disables sprint ability using Minecraft's hunger mechanic
public class SprintManager implements Listener, CommandExecutor {
    private static final int ALLOWED_TO_SPRINT = -1;
    private final ArrayList<Player> sprintDisabled = new ArrayList<>();

    // Checks if player is in the list
    // And keeps his hunger at the same level
    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent e) {
        final HumanEntity entity = e.getEntity();
        if (entity instanceof final Player player) {
            if (!isAllowedToSprint(player))
                e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof final Player player) {
            if (isAllowedToSprint(player)) {
                disableSprinting(player);
                player.sendMessage(ChatColor.YELLOW+ "Sprinting disabled");
            } else {
                enableSprinting(player);
                player.sendMessage(ChatColor.YELLOW+ "Sprinting enabled");
            }
        }

        return true;
    }

    // Checks if player is in the list and returns his index
    // If he isn't in the list, returns ALLOWED_TO_SPRINT
    private int getAllowedToSprintIndex(final Player player) {
        for (int i = 0; i < sprintDisabled.size(); ++i) {
            if (player.getUniqueId().equals(sprintDisabled.get(i).getUniqueId()))
                return i;
        }
        return ALLOWED_TO_SPRINT;
    }

    public boolean isAllowedToSprint(final Player player) {
        return getAllowedToSprintIndex(player) == ALLOWED_TO_SPRINT;
    }

    // Disables player sprinting
    public void disableSprinting(final Player player) {
        if (isAllowedToSprint(player)) {
            player.setFoodLevel(6);
            sprintDisabled.add(player);
        }
    }

    // Enables player sprinting
    public void enableSprinting(final Player player) {
        final int index = getAllowedToSprintIndex(player);
        if (index != ALLOWED_TO_SPRINT) {
            sprintDisabled.remove(index);
            player.setFoodLevel(20);
            player.setSaturation(20);
        }
    }
}
