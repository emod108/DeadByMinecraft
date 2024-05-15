package me.mod108.deadbyminecraft.utility;
import me.mod108.crawlingplugin.CrawlingPlugin;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.Prop;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Game {
    // Additional generators so killer can't camp a single generator
    private static final int ADDITIONAL_GENERATORS = 2;

    // Players who play in this game session
    private final ArrayList<Character> players;

    // Props in the game
    private final ArrayList<Prop> props = new ArrayList<>();

    // Generators in the game
    private final int startingGenerators;
    private int generatorsLeft;

    // Timer which updates game status every tick (player speed, stuns, generator regressions, etc)
    final BukkitRunnable gameUpdater = new BukkitRunnable() {
        @Override
        public void run() {
            for (final Character player : players) {
                updatePlayer(player);
            }
        }
    };

    public Game(final ArrayList<Character> players) {
        this.players = players;

        startingGenerators = players.size() + ADDITIONAL_GENERATORS;
        generatorsLeft = startingGenerators;

        startGame();
    }

    private void startGame() {
        // Preparing all players for the game
        for (final Character player : players) {
            preparePlayer(player);
        }

        // Starting game timer
        gameUpdater.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    public void finishGame() {
        gameUpdater.cancel();

        // Clearing all props
        for (final Prop prop : props) {
            // Destroying props
            prop.destroy();
        }
        props.clear();

        // Resetting every player
        for (final Character player : players) {
            resetPlayer(player);
        }
    }

    // This method prepares player for the game
    private void preparePlayer(final Character player) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        player.setIsSpeedActive(true);
        player.getPlayer().getInventory().clear();

        plugin.healthRegainManager.disableHealthRegain(player.getPlayer());
        plugin.jumpingManager.disableJumping(player.getPlayer());
        plugin.sprintManager.disableSprinting(player.getPlayer());

        // Clearing any experience
        player.getPlayer().setExp(0.0f);
        player.getPlayer().setLevel(0);

        // Preparing killer
        if (player instanceof final Killer killer) {
            killer.applyKillerKit();
        }
    }

    // Updates this player. Must be called every tick
    private void updatePlayer(final Character player) {
        // Updating player's speed
        player.updateSpeed();

        if (player instanceof final Killer killer) {
            killer.decrementStunTime();
            killer.decrementAttackCooldownTime();

            // If killer has no action, then we use progress bar for attack/stun cooldown
            if (killer.getAction() == null) {
                // If killer is stunned
                if (killer.isStunned())
                    ProgressBar.setProgress(killer.getPlayer(), killer.getStunRecoverProgress());
                else // Attack recover time
                    ProgressBar.setProgress(killer.getPlayer(), killer.getAttackRecoverProgress());
            }
        } else if (player instanceof final Survivor survivor) {
            // Spawning blood particles under injured survivors
            final Survivor.HealthState healthState = survivor.getHealthState();
            if (healthState != Survivor.HealthState.HEALTHY && healthState != Survivor.HealthState.DEAD &&
                healthState != Survivor.HealthState.BEING_CARRIED) {
                survivor.bleed();
            }
        }
    }

    // This method fully resets player
    private void resetPlayer(final Character player) {
        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();

        // Resetting killer
        if (player instanceof final Killer killer) {
            // Stops carrying survivor. Does nothing if no survivors carried
            killer.stopCarrying();
        }

        player.cancelAction();
        player.setIsSpeedActive(false);
        player.getPlayer().getInventory().clear();

        plugin.freezeManager.unFreeze(player.getPlayer());
        plugin.healthRegainManager.enableHealthRegain(player.getPlayer());
        plugin.jumpingManager.enableJumping(player.getPlayer());
        plugin.sprintManager.enableSprinting(player.getPlayer());
        plugin.vanishManager.show(player.getPlayer());

        ProgressBar.resetProgress(player.getPlayer());
        CrawlingPlugin.getPlugin().getCrawlingManager().stopCrawling(player.getPlayer());
    }

    public Character getPlayer(final Player player) {
        for (final Character character : players) {
            if (character.getPlayer().getUniqueId().equals(player.getUniqueId()))
                return character;
        }
        return null;
    }

    public ArrayList<Character> getPlayers() {
        return players;
    }

    public Killer getKiller() {
        for (final Character character : players) {
            if (character instanceof Killer)
                return (Killer) character;
        }
        return null;
    }

    public ArrayList<Prop> getProps() {
        return props;
    }

    public void addProp(final Prop prop) {
        props.add(prop);

        if (!prop.isBuilt())
            prop.build();
    }
}
