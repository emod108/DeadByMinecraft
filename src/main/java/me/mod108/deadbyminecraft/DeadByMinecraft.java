package me.mod108.deadbyminecraft;

import me.mod108.deadbyminecraft.commands.AddPlayerCommand;
import me.mod108.deadbyminecraft.commands.CreateLobbyCommand;
import me.mod108.deadbyminecraft.commands.DeleteLobbyCommand;
import me.mod108.deadbyminecraft.commands.FinishGameCommand;
import me.mod108.deadbyminecraft.commands.GetKillersCommand;
import me.mod108.deadbyminecraft.commands.GetPlayersCommand;
import me.mod108.deadbyminecraft.commands.RemovePlayerCommand;
import me.mod108.deadbyminecraft.commands.StartGameCommand;
import me.mod108.deadbyminecraft.listeners.*;
import me.mod108.deadbyminecraft.managers.FreezeManager;
import me.mod108.deadbyminecraft.managers.HealthRegainManager;
import me.mod108.deadbyminecraft.managers.JumpingManager;
import me.mod108.deadbyminecraft.managers.SoundManager;
import me.mod108.deadbyminecraft.managers.SprintManager;
import me.mod108.deadbyminecraft.managers.VanishManager;
import me.mod108.deadbyminecraft.targets.props.ExitGate;
import me.mod108.deadbyminecraft.targets.props.Generator;
import me.mod108.deadbyminecraft.targets.props.Hook;
import me.mod108.deadbyminecraft.targets.props.Locker;
import me.mod108.deadbyminecraft.targets.props.vaultable.Pallet;
import me.mod108.deadbyminecraft.targets.props.vaultable.Window;
import me.mod108.deadbyminecraft.test.*;
import me.mod108.deadbyminecraft.utility.Game;
import me.mod108.deadbyminecraft.utility.Lobby;
import me.mod108.deadbyminecraft.utility.MapData;
import me.mod108.deadbyminecraft.utility.MapLoader;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DeadByMinecraft extends JavaPlugin {
    // Making props serializable
    static {
        ConfigurationSerialization.registerClass(Pallet.class, "Pallet");
        ConfigurationSerialization.registerClass(Window.class, "Window");
        ConfigurationSerialization.registerClass(ExitGate.class, "ExitGate");
        ConfigurationSerialization.registerClass(Generator.class, "Generator");
        ConfigurationSerialization.registerClass(Hook.class, "Hook");
        ConfigurationSerialization.registerClass(Locker.class, "Locker");
    }

    private static DeadByMinecraft plugin;

    // Used to adjust position of an object to make it centred
    public static final double CENTERING = 0.5;

    public static final String MAPS_FOLDER_NAME = "maps";

    // A way to get plugin instance
    public static DeadByMinecraft getPlugin() {
        return plugin;
    }

    // Different game managers
    public final FreezeManager freezeManager = new FreezeManager();
    public final HealthRegainManager healthRegainManager = new HealthRegainManager();
    public final JumpingManager jumpingManager = new JumpingManager();
    public final SoundManager soundManager = new SoundManager();
    public final SprintManager sprintManager = new SprintManager();
    public final VanishManager vanishManager = new VanishManager();
    public final MapLoader mapLoader = new MapLoader();

    // Game
    private Lobby lobby = null;
    private Game game = null;

    @Override
    public void onEnable() {
        plugin = this;
        final Server server = getServer();
        final PluginManager pluginManager = server.getPluginManager();

        // Creating maps folder
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        final File folder = new File(getDataFolder(), MAPS_FOLDER_NAME);
        if (!folder.exists()) {
            try {
                folder.mkdir();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        // Test commands
        registerCommand("getmovementspeed", new GetMovementSpeedCommand());
        registerCommand("setmovementspeed", new SetMovementSpeedCommand());
        registerCommand("sbtest", new ScoreboardTestCommand());
        registerCommand("spawnprop", new SpawnPropCommand());

        // Map loading commands
        registerCommand("createmap", mapLoader);
        registerCommand("loadmap", mapLoader);
        registerCommand("unloadmap", mapLoader);
        registerCommand("savemap", mapLoader);
        registerCommand("addprop", mapLoader);
        registerCommand("removelastprop", mapLoader);
        registerCommand("showprops", mapLoader);
        registerCommand("hideprops", mapLoader);

        // Game management commands
        registerCommand("createlobby", new CreateLobbyCommand());
        registerCommand("deletelobby", new DeleteLobbyCommand());
        registerCommand("addplayer", new AddPlayerCommand());
        registerCommand("removeplayer", new RemovePlayerCommand());
        registerCommand("getplayers", new GetPlayersCommand());
        registerCommand("getkillers", new GetKillersCommand());
        registerCommand("startgame", new StartGameCommand());
        registerCommand("finishgame", new FinishGameCommand());

        // Freeze manager
        pluginManager.registerEvents(freezeManager, this);
        registerCommand("freeze", freezeManager);

        // Health regain manager
        pluginManager.registerEvents(healthRegainManager, this);
        registerCommand("togglehealthregain", healthRegainManager);

        // Jumping manager
        registerCommand("togglejump", jumpingManager);

        // Sound manager
        registerCommand("testplaysound", soundManager);

        // Sprint manager
        pluginManager.registerEvents(sprintManager, this);
        registerCommand("togglesprint", sprintManager);

        // Vanish manager
        registerCommand("vanish", vanishManager);

        // Player attacking another player
        pluginManager.registerEvents(new EntityDamageEntityListener(), this);
        pluginManager.registerEvents(new KillerAttackSurvivorListener(), this);

        // Join / Leave events
        pluginManager.registerEvents(new PlayerLeaveListener(), this);
        pluginManager.registerEvents(new PlayerJoinListener(), this);

        // Player interaction
        pluginManager.registerEvents(new PlayerInteractListener(), this);
        pluginManager.registerEvents(new VaultListener(), this);
        pluginManager.registerEvents(new PalletInteractListener(), this);
        pluginManager.registerEvents(new LockerInteractListener(), this);
        pluginManager.registerEvents(new GeneratorInteractListener(), this);
        pluginManager.registerEvents(new ExitGateInteractListener(), this);
        pluginManager.registerEvents(new PlayerInteractEntityListener(), this);
        pluginManager.registerEvents(new KillerPickupSurvivorListener(), this);
        pluginManager.registerEvents(new SurvivorHealListener(), this);
        pluginManager.registerEvents(new EntityDismountListener(), this);
        pluginManager.registerEvents(new PlayerMoveListener(), this);

        // Inventory interactions
        pluginManager.registerEvents(new DropItemListener(), this);
        pluginManager.registerEvents(new InventoryClickListener(), this);

        // Hook Interactions
        pluginManager.registerEvents(new HookInteractListener(), this);
        pluginManager.registerEvents(new KillerHookSurvivorListener(), this);
        pluginManager.registerEvents(new SurvivorUnhookListener(), this);

        // Test
        registerCommand("getinjured", new GetInjuredCommand());
    }

    @Override
    public void onDisable() {
        if (game != null)
            game.finishGame();
        game = null;

        final MapData mapData = mapLoader.getMapData();
        if (mapData != null)
            mapData.hideProps();
    }

    private void registerCommand(final String command, final CommandExecutor executor) {
        final PluginCommand pluginCommand = getCommand(command);
        if (pluginCommand != null)
            pluginCommand.setExecutor(executor);
        else
            System.err.println("ERROR: Couldn't find command named \"" + command + "\"!");
    }

    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(final Lobby lobby) {
        this.lobby = lobby;
    }

    public Game getGame() {
        return game;
    }

    public void startGame() {
        if (game != null)
            return;

        if (lobby == null)
            return;

        lobby.removeOfflinePlayers();
        game = new Game(plugin.getLobby().getPlayers());
        game.startGame();
        lobby = null;
    }

    public void finishGame() {
        if (game != null) {
            game.finishGame();
            game = null;
        }
    }
}