package me.mod108.deadbyminecraft.utility;
import me.mod108.crawlingplugin.CrawlingPlugin;
import me.mod108.deadbyminecraft.DeadByMinecraft;
import me.mod108.deadbyminecraft.targets.characters.Character;
import me.mod108.deadbyminecraft.targets.characters.Survivor;
import me.mod108.deadbyminecraft.targets.characters.Survivor.HealthState;
import me.mod108.deadbyminecraft.targets.characters.killers.Killer;
import me.mod108.deadbyminecraft.targets.props.*;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.targets.props.vaultable.Window;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Set;

public class Game {
    public final static int MAX_SURVIVORS_NUM = 4;

    // Escape lines, which survivors must to cross
    private final ArrayList<EscapeLine> escapeLines = new ArrayList<>();

    // 4 minutes is max time of endgame collapse
    private static final int MAX_ENDGAME_COLLAPSE_TIME = Timings.secondsToTicks(240);

    // This runnable purpose it to finish the game after certain time
    BukkitRunnable finishGameTask = null;

    // Players list
    private final ArrayList<Character> players;

    // Props list
    private final ArrayList<Prop> props;

    // Escape information
    private final ArrayList<Generator> generators = new ArrayList<>();
    private int generatorsLeft = 1;
    private final ArrayList<ExitGate> exitGates = new ArrayList<>();
    private boolean everythingIsPowered = false;
    private Hatch hatch = null;

    // Other props
    private final ArrayList<Pallet> pallets = new ArrayList<>();
    private final ArrayList<Window> windows = new ArrayList<>();
    private final ArrayList<Hook> hooks = new ArrayList<>();
    private final ArrayList<Locker> lockers = new ArrayList<>();

    // Player information
    private final Location killerSpawn;
    private Killer killer;
    private final ArrayList<Location> survivorSpawns;
    private final ArrayList<Survivor> survivors = new ArrayList<>();
    private int survivorsLeft = 0;
    private boolean survivorDowned = false;

    // Endgame collapse information
    private int endGameCollapseTimer = MAX_ENDGAME_COLLAPSE_TIME;
    private boolean endGameCollapseStarted = false;
    final BossBar endGameCollapseBar;

    // Scoreboard information
    Scoreboard scoreboard = null;
    private final static String killerTeamStr = "killer";
    private final static String survivorTeamStr = "survivor";
    private final ArrayList<MyPair<Integer, String>> scores = new ArrayList<>();
    private final static int escapeScoreIndex = 6;

    // Timer which updates game status every tick (player speed, stuns, generator regressions, etc)
    final BukkitRunnable gameUpdater = new BukkitRunnable() {
        @Override
        public void run() {
            if (isCancelled())
                return;
            if (endGameCollapseStarted) {
                endGameCollapseTimer -= survivorDowned ? 1 : 2;

                // Updating Boss bar
                float bossBarProgress = (float) endGameCollapseTimer / MAX_ENDGAME_COLLAPSE_TIME;
                if (bossBarProgress < 0)
                    bossBarProgress = 0;
                endGameCollapseBar.setProgress(bossBarProgress);

                // Sacrificing all survivors if the timer reaches 0
                if (endGameCollapseTimer <= 0) {
                    for (final Survivor survivor : survivors)
                        if (survivor.isAlive())
                            survivor.getSacrificed();
                }
            }

            // Updating all players
            for (final Character player : players)
                updatePlayer(player);

            // Regressing generators
            for (final Generator generator : generators)
                generator.regress();
        }
    };

    public Game(final ArrayList<Character> players, final ArrayList<Prop> props, final Location killerSpawn,
                final ArrayList<Location> survivorSpawns) {
        this.players = players;
        this.props = props;
        this.killerSpawn = killerSpawn;
        this.survivorSpawns = survivorSpawns;

        // Assigning props
        for (final Prop prop : props)
            assignProp(prop);

        // Assigning player
        for (final Character player : players) {
            if (player instanceof final Survivor survivor) {
                ++generatorsLeft;
                survivors.add(survivor);
                ++survivorsLeft;
            } else {
                killer = (Killer) player;
            }
        }

        // Creating endgame collapse timer
        final String endGameCollapseTitle = ChatColor.RED + "ENDGAME COLLAPSE";
        endGameCollapseBar = Bukkit.createBossBar(endGameCollapseTitle, BarColor.YELLOW, BarStyle.SEGMENTED_12);
        endGameCollapseBar.setVisible(false);
    }

    // Assigns prop to a field
    private void assignProp(final Prop prop) {
        if (prop instanceof final Generator generator) {
            generators.add(generator);
            return;
        }

        if (prop instanceof final ExitGate exitGate) {
            exitGates.add(exitGate);
            return;
        }

        if (prop instanceof final Pallet pallet) {
            pallets.add(pallet);
        }

        if (prop instanceof final Window window) {
            windows.add(window);
            return;
        }

        if (prop instanceof final Hook hook) {
            hooks.add(hook);
            return;
        }

        if (prop instanceof final Locker locker) {
            lockers.add(locker);
            return;
        }

        if (prop instanceof final Hatch h)
            this.hatch = h;
    }

    public void startGame() {
        createScoreboard();

        // Preparing all players for the game
        for (final Character player : players)
            preparePlayer(player);

        for (final Prop prop : props) {
            if (prop instanceof Hatch && survivorsLeft > 1)
                continue;
            prop.build();
        }

        // Spawning players
        if (killerSpawn != null)
            killer.getPlayer().teleport(killerSpawn);
        for (int i = 0; i < survivors.size() && i < survivorSpawns.size(); ++i)
            survivors.get(i).getPlayer().teleport(survivorSpawns.get(i));

        // Starting game timer
        gameUpdater.runTaskTimer(DeadByMinecraft.getPlugin(), 0, 1);
    }

    public void finishGame() {
        gameUpdater.cancel();
        endGameCollapseBar.setVisible(false);
        endGameCollapseBar.removeAll();

        if (finishGameTask != null)
            finishGameTask.cancel();

        // Clearing all props
        for (final Prop prop : props) {
            // Destroying props
            prop.destroy();
        }
        props.clear();

        // Resetting every player
        for (final Character player : players)
            resetPlayer(player);
        deleteScoreboard();
    }

    // This method prepares player for the game
    private void preparePlayer(final Character player) {
        final Player playerEntity = player.getPlayer();

        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        addToScoreboard(player);
        endGameCollapseBar.addPlayer(playerEntity);
        player.setIsSpeedActive(true);
        playerEntity.getInventory().clear();
        playerEntity.setHealth(20);

        plugin.healthRegainManager.disableHealthRegain(playerEntity);
        plugin.jumpingManager.disableJumping(playerEntity);
        plugin.sprintManager.disableSprinting(playerEntity);

        // Clearing any experience
        playerEntity.setExp(0.0f);
        playerEntity.setLevel(0);

        // Setting game mode to adventure
        playerEntity.setGameMode(GameMode.ADVENTURE);

        // Preparing killer
        if (player instanceof final Killer k) {
            k.applyKillerKit();
            return;
        }

        // Preparing survivor
        final Survivor survivor = (Survivor) player;
        final WorldBorder worldBorder = Bukkit.createWorldBorder();
        playerEntity.setWorldBorder(worldBorder);
    }

    // Updates this player. Must be called every tick
    private void updatePlayer(final Character player) {
        if (player instanceof final Killer k) {
            k.decrementStunTime();
            k.decrementAttackCooldownTime();

            // If killer has no action, then we use progress bar for attack/stun cooldown
            if (k.getAction() == null) {
                // If killer is stunned
                if (k.isStunned())
                    ProgressBar.setProgress(k.getPlayer(), k.getStunRecoverProgress());
                else // Attack recover time
                    ProgressBar.setProgress(k.getPlayer(), k.getAttackRecoverProgress());
            }
        } else if (player instanceof final Survivor survivor) {
            // Spawning blood particles under injured survivors
            final HealthState healthState = survivor.getHealthState();

            // We don't update disconnected survivors
            if (healthState == HealthState.DISCONNECTED)
                return;

            // Create blood particles on injured survivors
            if (survivor.getMovementState() != Character.MovementState.IN_LOCKER) {
                if (healthState == HealthState.INJURED || healthState == HealthState.DEEP_WOUND ||
                        healthState == HealthState.DYING || healthState == HealthState.HOOKED) {
                    survivor.bleed();
                }
            }

            // Playing terror radius
            survivor.playTerrorRadius(killer.getLocation());

            // Processing bleed-out and sacrifice timers
            survivor.processBleedOut();
            survivor.processSacrifice();
        }

        // Updating player's speed
        player.updateSpeed();
    }

    // This method fully resets player
    public void resetPlayer(final Character player) {
        final Player playerEntity = player.getPlayer();

        final DeadByMinecraft plugin = DeadByMinecraft.getPlugin();
        removeFromScoreboard(player);
        player.removeAllAuras();

        // Removing world border
        playerEntity.setWorldBorder(null);

        // Resetting killer
        if (player instanceof final Killer k) {
            // Stops carrying survivor. Does nothing if no survivors carried
            k.stopCarrying();

            // Hiding exit gates blockers
            for (final EscapeLine escape : escapeLines) {
                escape.hideFromKiller(k);
            }
        }

        player.cancelAction();
        player.setIsSpeedActive(false);
        player.getPlayer().getInventory().clear();

        plugin.freezeManager.unFreeze(playerEntity.getUniqueId());
        plugin.healthRegainManager.enableHealthRegain(playerEntity);
        plugin.jumpingManager.enableJumping(playerEntity);
        plugin.sprintManager.enableSprinting(playerEntity);
        plugin.vanishManager.show(playerEntity);

        ProgressBar.resetProgress(playerEntity);
        playerEntity.setLevel(0);
        CrawlingPlugin.getPlugin().getCrawlingManager().stopCrawling(playerEntity);
        playerEntity.setGameMode(GameMode.ADVENTURE);

        // Teleporting player to spawn
        final World world = Bukkit.getWorld("world");
        if (world != null)
            playerEntity.teleport(world.getSpawnLocation());
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

    // This function must be called, when Health state of a survivor has changed
    public void survivorsHealthChanged(final Survivor survivor) {
        if (!survivor.isAlive()) {
            // Hiding his aura from everyone
            for (final Survivor otherSurvivor : survivors) {
                if (otherSurvivor.samePlayer(survivor))
                    continue;
                otherSurvivor.addAura(survivor);
            }
            killer.removeAura(survivor);
            survivor.removeAllAuras();

            // Checking game status
            --survivorsLeft;
            checkSurvivorsNum();
        }

        // Updating scoreboard
        int indexInScoreboard = 11;
        for (final Survivor survivorEntry : survivors) {
            if (survivorEntry.samePlayer(survivor)) {
                final Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
                if (objective != null) {
                    // Removing old score
                    for (int i = 0; i < scores.size(); ++i) {
                        final MyPair<Integer, String> pair = scores.get(i);
                        if (pair.first == indexInScoreboard) {
                            scoreboard.resetScores(pair.second);
                            scores.remove(i);
                            break;
                        }
                    }

                    // Adding new score
                    final String entry = ChatColor.BLUE + survivor.getPlayer().getName() + ": " +
                            Survivor.healthStateToStr(survivor.getHealthState());
                    final Score survivorScore = objective.getScore(entry);
                    survivorScore.setScore(indexInScoreboard);
                    scores.add(new MyPair<>(indexInScoreboard, entry));
                }
                break;
            }
            --indexInScoreboard;
        }

        // Endgame collapse
        boolean checkedEngameCollapse = false;
        for (final Survivor survivorEntry : survivors) {
            if (survivorEntry.isDowned()) {
                survivorDowned = true;
                endGameCollapseBar.setColor(BarColor.WHITE);
                checkedEngameCollapse = true;
                break;
            }
        }
        if (!checkedEngameCollapse) {
            survivorDowned = false;
            endGameCollapseBar.setColor(BarColor.YELLOW);
        }
        checkIfDoomed();
    }

    // Check if everyone who is left is hooked and can't unhook themselves
    public void checkIfDoomed() {
        boolean atLeastOneStanding = false;
        for (final Survivor leftSurvivor : survivors) {
            if (!leftSurvivor.isAlive())
                continue;

            if (leftSurvivor.getHealthState() != HealthState.HOOKED || leftSurvivor.canSelfUnhook()) {
                atLeastOneStanding = true;
                break;
            }
        }

        // If all survivors are dead or hooked, we sacrifice remaining survivor immediately
        if (!atLeastOneStanding) {
            for (final Survivor leftSurvivor : survivors)
                leftSurvivor.getSacrificed();
        }
    }

    // This function must be called, when generator was repaired
    public void generatorRepaired() {
        // If all needed generators were repaired
        --generatorsLeft;
        if (generatorsLeft <= 0)
            powerEverything();
        else { // Update scoreboard if there are still generators to be repaired
            final Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (objective != null) {
                // Deleting old escape score
                for (int i = 0; i < scores.size(); ++i) {
                    final MyPair<Integer, String> pair = scores.get(i);
                    if (pair.first == escapeScoreIndex) {
                        scoreboard.resetScores(pair.second);
                        scores.remove(i);
                        break;
                    }
                }

                // Adding new escape score
                final String escapeStr = ChatColor.YELLOW + "Generators left: " + generatorsLeft;
                final Score escapeScore = objective.getScore(escapeStr);
                escapeScore.setScore(escapeScoreIndex);
                scores.add(new MyPair<>(escapeScoreIndex, escapeStr));
            }
        }
    }

    private void powerEverything() {
        if (everythingIsPowered)
            return;
        everythingIsPowered = true;
        for (final Character player : players)
            player.getPlayer().sendMessage(ChatColor.YELLOW + "Exit gates have been powered!\n");

        // Powering gates
        for (final ExitGate gate : exitGates)
            gate.setGateState(ExitGate.ExitGateState.POWERED);

        // Powering all not powered generators
        for (final Generator generator : generators)
            generator.becomeRepaired();

        // Updating scoreboard
        final Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective != null) {
            // Deleting old escape score
            for (int i = 0; i < scores.size(); ++i) {
                final MyPair<Integer, String> pair = scores.get(i);
                if (pair.first == escapeScoreIndex) {
                    scoreboard.resetScores(pair.second);
                    scores.remove(i);
                    break;
                }
            }

            // Adding new escape score
            final String escapeStr = ChatColor.YELLOW + "Escape through Exit Gates!";
            final Score escapeScore = objective.getScore(escapeStr);
            escapeScore.setScore(escapeScoreIndex);
            scores.add(new MyPair<>(escapeScoreIndex, escapeStr));
        }
    }

    // This function should be called, when an exit gate was opened
    public void gateOpened(final EscapeLine escapeLine) {
        escapeLines.add(escapeLine);
        startEndGameCollapse();
    }

    // This function should be called, when the hatch was closed
    public void hatchClosed() {
        powerEverything();
        startEndGameCollapse();
    }

    // This function starts endgame collapse
    private void startEndGameCollapse() {
        if (endGameCollapseStarted)
            return;
        endGameCollapseStarted = true;
        endGameCollapseBar.setVisible(true);
    }

    // This function checks if game should end or hatch should spawn
    private void checkSurvivorsNum() {
        if (survivorsLeft == 1)
            hatch.build();
        else if (survivorsLeft == 0) { // Finishing the game in 5 seconds
            finishGameTask = new BukkitRunnable() {
                @Override
                public void run() {
                    finishGameTask = null;
                    DeadByMinecraft.getPlugin().finishGame();
                }
            };
            finishGameTask.runTaskLater(DeadByMinecraft.getPlugin(), Timings.secondsToTicks(5));
        }
    }

    public ArrayList<Survivor> getSurvivors() {
        return survivors;
    }

    public Killer getKiller() {
        return killer;
    }

    public ArrayList<Generator> getGenerators() {
        return generators;
    }

    public ArrayList<ExitGate> getExitGates() {
        return exitGates;
    }

    public ArrayList<Pallet> getPallets() {
        return pallets;
    }

    public ArrayList<Window> getWindows() {
        return windows;
    }

    public ArrayList<Hook> getHooks() {
        return hooks;
    }

    public ArrayList<Locker> getLockers() {
        return lockers;
    }

    public Hatch getHatch() {
        return hatch;
    }

    public ArrayList<EscapeLine> getEscapeLines() {
        return escapeLines;
    }

    private void createScoreboard() {
        // Creating scoreboard
        final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        assert scoreboardManager != null;
        scoreboard = scoreboardManager.getNewScoreboard();

        // Creating teams
        createTeam(killerTeamStr);
        createTeam(survivorTeamStr);

        // Creating objective
        final Objective objective = scoreboard.registerNewObjective("Dead by Minecraft",
                Criteria.DUMMY, ChatColor.GOLD + "Dead by Minecraft");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Creating scores
        int currentScore = 12;
        final Score emptyAfterObjective = objective.getScore("");
        emptyAfterObjective.setScore(currentScore);
        --currentScore;

        // Survivors scores
        for (final Survivor survivor : survivors) {
            final String entry = ChatColor.BLUE + survivor.getPlayer().getName() + ": " +
                    Survivor.healthStateToStr(survivor.getHealthState());
            final Score survivorScore = objective.getScore(entry);
            survivorScore.setScore(currentScore);
            scores.add(new MyPair<>(currentScore, entry));
            --currentScore;
        }

        // Empty space after survivors
        final Score spacingScore = objective.getScore("");
        spacingScore.setScore(currentScore);
        --currentScore;

        // Escape score
        final String escapeStr = ChatColor.YELLOW + "Generators left: " + generatorsLeft;
        final Score escapeScore = objective.getScore(escapeStr);
        escapeScore.setScore(escapeScoreIndex);
        scores.add(new MyPair<>(escapeScoreIndex, escapeStr));
    }

    private Team createTeam(final String teamStr) {
        Team team = scoreboard.getTeam(teamStr);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamStr);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.setColor(ChatColor.RED);
        }
        return team;
    }

    private void deleteScoreboard() {
        // Unregistering killer team
        final Team killerTeam = scoreboard.getTeam(killerTeamStr);
        if (killerTeam != null) {
            final Set<String> entries = killerTeam.getEntries();
            for (final String entry : entries)
                killerTeam.removeEntry(entry);
            killerTeam.unregister();
        }

        // Unregistering survivor team
        final Team survivorTeam = scoreboard.getTeam(survivorTeamStr);
        if (survivorTeam != null) {
            final Set<String> entries = survivorTeam.getEntries();
            for (final String entry : entries)
                survivorTeam.removeEntry(entry);
            survivorTeam.unregister();
        }

        scores.clear();
        scoreboard = null;
    }

    private void addToScoreboard(final Character player) {
        player.getPlayer().setScoreboard(scoreboard);

        // Adding to a team
        final String teamStr = player instanceof Killer ? killerTeamStr : survivorTeamStr;
        Team team = scoreboard.getTeam(teamStr);
        if (team == null)
            team = createTeam(teamStr);
        team.addEntry(player.getPlayer().getName());
    }

    private void removeFromScoreboard(final Character player) {
        final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager != null)
            player.getPlayer().setScoreboard(scoreboardManager.getMainScoreboard());
    }
}
