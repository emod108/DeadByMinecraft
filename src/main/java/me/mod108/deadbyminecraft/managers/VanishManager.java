package me.mod108.deadbyminecraft.managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class VanishManager implements CommandExecutor {
    private static final int NOT_VANISHED = -1;

    private final ArrayList<Player> hiddenPlayers = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof final Player player) {
            if (isHidden(player)) {
                show(player);
                player.sendMessage("You are not invisible anymore.");
            } else {
                hide(player);
                player.sendMessage("You are now invisible.");
            }
        }
        return true;
    }

    private int getHiddenIndex(final Player player) {
        for (int i = 0; i < hiddenPlayers.size(); ++i) {
            if (player.getUniqueId().equals(hiddenPlayers.get(i).getUniqueId()))
                return i;
        }
        return NOT_VANISHED;
    }

    public boolean isHidden(final Player player) {
        return getHiddenIndex(player) != NOT_VANISHED;
    }

    public void hide(final Player player) {
        if (isHidden(player))
            return;

        player.setInvisible(true);
        hiddenPlayers.add(player);
    }

    public void show(final Player player) {
        final int index = getHiddenIndex(player);
        if (index == NOT_VANISHED)
            return;

        player.setInvisible(false);
        hiddenPlayers.remove(index);
    }

    public ArrayList<Player> getHiddenPlayers() {
        return hiddenPlayers;
    }
}
