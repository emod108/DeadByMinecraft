package me.mod108.deadbyminecraft.utility;

import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Lobby {
    public static final int MAX_SURVIVORS = 4;
    public static final int PLAYER_NOT_FOUND = -1;

    private final ArrayList<Character> players = new ArrayList<>();

    public ArrayList<Character> getPlayers() {
        return players;
    }

    public int getPlayersCount() {
        return players.size();
    }

    public int getSurvivorsCount() {
        return (int) players.stream().filter(Survivor.class::isInstance).count();
    }

    public boolean hasMaxSurvivors() {
        return getSurvivorsCount() == MAX_SURVIVORS;
    }

    public boolean hasKiller() {
        for (final Character player : players) {
            if (player instanceof Killer)
                return true;
        }
        return false;
    }

    public void addSurvivor(final Player player) {
        if (getSurvivorsCount() < MAX_SURVIVORS)
            players.add(new Survivor(player));
    }

    public void addKiller(final Killer player) {
        if (!hasKiller())
            players.add(player);
    }

    public boolean hasPlayer(final Player player) {
        for (final Character currentPlayer : players) {
            if (currentPlayer.getPlayer().getUniqueId().equals(player.getUniqueId()))
                return true;
        }
        return false;
    }

    private int findPlayer(final Player player) {
        for (int i = 0; i < players.size(); ++i) {
            if (players.get(i).getPlayer().getUniqueId().equals(player.getUniqueId()))
                return i;
        }
        return PLAYER_NOT_FOUND;
    }

    public Character getPlayer(final Player player) {
        final int index = findPlayer(player);
        if (index == PLAYER_NOT_FOUND)
            return null;

        return players.get(index);
    }

    public void removePlayer(final Player player) {
        final int index = findPlayer(player);
        if (index != PLAYER_NOT_FOUND)
            players.remove(index);
    }

    private int findKiller() {
        for (int i = 0; i < players.size(); ++i) {
            if (players.get(i) instanceof Killer)
                return i;
        }
        return PLAYER_NOT_FOUND;
    }

    // Removes killer if there's one
    public void removeKiller() {
        final int index = findKiller();
        if (index != PLAYER_NOT_FOUND)
            players.remove(index);
    }

    // Removes offline players
    // Returns true if offline players were found
    public boolean removeOfflinePlayers() {
        return players.removeIf(n -> !n.getPlayer().isOnline());
    }

    public void clearPlayers() {
        players.clear();
    }
}
