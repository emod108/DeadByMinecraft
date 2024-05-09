package me.mod108.deadbyminecraft.test;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.Set;

public class ScoreboardTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof final Player player))
            return true;

        final ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null)
            return true;

        final Scoreboard scoreboard = manager.getMainScoreboard();
        Team team = scoreboard.getTeam("testteam");
        if (team == null) {
            team = scoreboard.registerNewTeam("testteam");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        Set<String> entries = team.getEntries();
        boolean removed = false;
        for (String entry : entries) {
            if (entry.equalsIgnoreCase(player.getName())) {
                team.removeEntry(player.getName());
                removed = true;
            }
        }
        if (!removed)
            team.addEntry(player.getName());

        player.sendMessage("Done!");

        return true;
    }
}
