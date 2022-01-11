package me.poutineqc.deacoudre.instances;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import me.poutineqc.deacoudre.*;
import me.poutineqc.deacoudre.achievements.Achievement;
import me.poutineqc.deacoudre.commands.DacSign;
import me.poutineqc.deacoudre.events.PlayerDamage;
import me.poutineqc.deacoudre.ui.ArenaUI;
import me.poutineqc.deacoudre.ui.InventoryBar;
import me.poutineqc.deacoudre.sections.ArenaSection;
import me.poutineqc.deacoudre.tools.ColorManager;
import me.poutineqc.deacoudre.tools.Utils;
import me.poutineqc.deacoudre.ui.PlayerUI;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Arena {
	protected static PlayerDamage playerDamage;
	private static DeACoudre plugin;
	private static Configuration config;
	private static MySQL mysql;
	private static PlayerData playerData;
	private static ArenaData arenaData;
	private static Achievement achievements;
	private static SectionManager sectionManager;
	private static ArenaUI arenaUI;
	private static PlayerUI playerUI;
	private static List<Arena> arenas = new ArrayList<>();
	private final List<User> users = new ArrayList<>();
	private final String shortName;
	private String displayName;
	private World world;
	private Location lobby;
	private Location platform;
	private Location minPoint;
	private Location maxPoint;
	private int maxAmountPlayer;
	private int minAmountPlayer;
	private ColorManager colorManager;
	private User activePlayer;
	private boolean someoneLostFinal = false;
	private int stallingAmount = 0;
	private int roundNo = 0;
	private int currentTile = 0;
	private int totalTile;
	private long startTime;
	private boolean forceStart;
	private GameState gameState = GameState.UNREADY;
	private Objective objective;
	private Scoreboard scoreboard;
	private Team spectator;

	private ArenaSection section;

	public static void init(DeACoudre plugin) {
		Arena.plugin = plugin;
		Arena.config = plugin.getConfiguration();
		Arena.mysql = plugin.getMySQL();
		Arena.arenaData = plugin.getArenaData();
		Arena.playerData = plugin.getPlayerData();
		Arena.achievements = plugin.getAchievement();
		Arena.playerDamage = plugin.getPlayerDamage();
		Arena.sectionManager = plugin.getSectionManager();
		Arena.arenaUI = plugin.getArenaUI();
		Arena.playerUI = plugin.getPlayerUI();
	}

	public Arena(String shortName, Player player) {
		this(shortName, shortName, player.getWorld(), null, null, null, null, 2, 8);

		arenaData.getData().set("arenas." + shortName + ".world", world.getName());
		arenaData.getData().set("arenas." + shortName + ".displayName", shortName);
		arenaData.saveArenaData();
	}

	public Arena(String shortName, String displayName, World world, Location minPoint, Location maxPoint, Location lobby, Location platform,
	             int minAmountPlayer, int maxAmountPlayer) {
		this.shortName = shortName;
		this.displayName = displayName;

		try {
			world.getName();
			this.world = world;
		} catch(NullPointerException e) {
			this.world = null;
		}

		try {
			this.minPoint = minPoint;
			this.maxPoint = maxPoint;
			this.lobby = lobby;
			this.platform = platform;
			setNullIfDefault();
		} catch(NullPointerException e) {
			this.minPoint = null;
			this.maxPoint = null;
			this.lobby = null;
			this.platform = null;
		}

		this.minAmountPlayer = minAmountPlayer;
		this.maxAmountPlayer = maxAmountPlayer;
		colorManager = new ColorManager(plugin, this);

		Language local = Language.getDefaultLanguage();

		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		spectator = scoreboard.registerNewTeam("spectator");
		spectator.setCanSeeFriendlyInvisibles(true);
		setNameTagVisibilityNever();

		objective = scoreboard.registerNewObjective(this.shortName, "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				ChatColor.AQUA + this.displayName + " &f: " + local.keyWordScoreboardPlayers));
		objective.getScore(ChatColor.GOLD + "-------------------").setScore(1);
		objective.getScore(
						ChatColor.GOLD + local.keyWordGeneralMinimum + " = " + ChatColor.AQUA + minAmountPlayer)
				.setScore(minAmountPlayer);
		objective.getScore(
						ChatColor.GOLD + local.keyWordGeneralMaximum + " = " + ChatColor.AQUA + maxAmountPlayer)
				.setScore(maxAmountPlayer);

		if(this.minAmountPlayer < 2) {
			Log.info("The min amount of players for the arena " + this.shortName + " can't be below 2.");
			Log.info("Using by default '2'.");
			this.minAmountPlayer = 2;
		}

		if(this.maxAmountPlayer > 12) {
			Log.info("The max amount of players for the arena " + this.shortName + " can't be above 12.");
			Log.info("Using by default 12.");
			this.maxAmountPlayer = 12;
		}

		if(this.maxAmountPlayer > colorManager.getAvailableArenaBlocks().size()) {
			Log.info("The max amount of players for the arena " + this.shortName
					+ " can't be above the amount of available colors.");
			Log.info("Using by default " + colorManager.getAvailableArenaBlocks().size() + ".");
			this.maxAmountPlayer = colorManager.getAvailableArenaBlocks().size();
		}

		arenas.add(this);

		// Delay section creation to give a fully-constructed object
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			this.section = new ArenaSection(playerData, this);
			sectionManager.register(this.section);
		}, 1);
	}

	public static void loadArenas() {
		arenas = new ArrayList<>();

		if(!arenaData.getData().contains("arenas")) {
			return;
		}

		for(String arenaShortName : arenaData.getData().getConfigurationSection("arenas").getKeys(false)) {
			ConfigurationSection cs = arenaData.getData().getConfigurationSection("arenas." + arenaShortName);
			assert cs != null;
			ConfigurationSection ccs;

			playerData.getData().set("arenas." + arenaShortName + ".material", null);
			playerData.savePlayerData();

			String displayName = cs.getString("displayName");
			if(displayName == null) {
				displayName = arenaShortName;
			}

			String worldName = cs.getString("world");
			assert worldName != null;
			World world = Bukkit.getWorld(worldName);
			assert world != null;
			int minAmountPlayer = cs.getInt("minPlayer", 2);
			int maxAmountPlayer = cs.getInt("maxPlayer", 8);

			ccs = cs.getConfigurationSection("waterPool.minimum");
			Location minPoint = new Location(world, ccs.getInt("x", 0), ccs.getInt("y", 0), ccs.getInt("z", 0));

			ccs = cs.getConfigurationSection("waterPool.maximum");
			Location maxPoint = new Location(world, ccs.getInt("x", 0), ccs.getInt("y", 0), ccs.getInt("z", 0));

			ccs = cs.getConfigurationSection("lobby");
			Location lobby = new Location(world, ccs.getDouble("x", 0), ccs.getDouble("y", 0),
					ccs.getDouble("z", 0));
			lobby.setPitch((float) ccs.getDouble("pitch", 0));
			lobby.setYaw((float) ccs.getDouble("yaw", 0));

			ccs = cs.getConfigurationSection("platform");
			Location platform = new Location(world, ccs.getDouble("x", 0), ccs.getDouble("y", 0),
					ccs.getDouble("z", 0));
			platform.setPitch((float) ccs.getDouble("pitch", 0));
			platform.setYaw((float) ccs.getDouble("yaw", 0));

			new Arena(arenaShortName, displayName, world, minPoint, maxPoint, lobby, platform, minAmountPlayer, maxAmountPlayer);
		}
	}

	public static List<Arena> getArenas() {
		return arenas;
	}

	public static Arena getArenaFromName(String arenaName) {
		return arenas.stream().filter(a -> a.getShortName().equalsIgnoreCase(arenaName)).findFirst().orElse(null);
	}

	public static Arena getArenaFromPlayer(Player player) {
		for(Arena a : arenas) {
			for(User p : a.users) {
				if(player == p.getPlayer()) {
					return a;
				}
			}
		}
		return null;
	}

	public static List<Player> getAllPlayersInStartedGame() {
		return arenas.stream()
				.filter(a -> a.gameState == GameState.ACTIVE)
				.map(a -> a.users)
				.flatMap(Collection::stream)
				.map(User::getPlayer)
				.collect(Collectors.toList());
	}

	public static List<Player> getAllOutsideGame(Arena arena) {
		List<Player> outsideGame = new ArrayList<>();
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			Arena a = getArenaFromPlayer(player);
			if(a != null) {
				if(getArenaFromPlayer(player) == arena) {
					continue;
				}
			}
			outsideGame.add(player);
		}
		return outsideGame;
	}

	public String getFullSectionName() {
		return section.getFullName();
	}

	public User getActivePlayer() {
		return activePlayer;
	}

	private void setNullIfDefault() {
		if((0 == minPoint.getX()) && (0 == minPoint.getY()) && (0 == minPoint.getZ())) {
			minPoint = null;
		}

		if((0 == maxPoint.getX()) && (0 == maxPoint.getY()) && (0 == maxPoint.getZ())) {
			maxPoint = null;
		}

		if((0 == lobby.getX()) && (0 == lobby.getY()) && (0 == lobby.getZ())) {
			lobby = null;
		}

		if((0 == platform.getX()) && (0 == platform.getY()) && (0 == platform.getZ())) {
			platform = null;
		}

		if(isReady()) {
			gameState = GameState.READY;
		}
	}

	private boolean isReady() {
		return lobby != null && platform != null && minPoint != null && maxPoint != null;
	}

	private void setNameTagVisibilityNever() {
		spectator.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
	}

	public void deleteArena() {
		DacSign.arenaDelete(this);
		arenas.remove(this);

		arenaData.getData().set("arenas." + shortName, null);
		arenaData.saveArenaData();
	}

	public boolean setPool(Player player) {
		if(getWorldEdit() == null) {
			return false;
		}

		LocalSession session = getWorldEdit().getSession(player);
		if(session == null) {
			return false;
		}

		com.sk89q.worldedit.world.World world = session.getSelectionWorld();
		World bukkitWorld = BukkitAdapter.adapt(world);
		if(world == null || bukkitWorld == null) {
			return false;
		}

		Region s;
		try {
			s = session.getSelection(world);
		} catch(IncompleteRegionException e) {
			return false;
		}
		if(s == null) {
			return false;
		}

		gameState = GameState.UNREADY;

		if(!bukkitWorld.getName().equalsIgnoreCase(player.getWorld().getName())) {
			DacSign.removePlaySigns(this);
		}

		minPoint = new Location(bukkitWorld, s.getMinimumPoint().getBlockX(), s.getMinimumPoint().getBlockY(), s.getMinimumPoint().getBlockZ());
		maxPoint = new Location(bukkitWorld, s.getMaximumPoint().getBlockX(), s.getMaximumPoint().getBlockY(), s.getMaximumPoint().getBlockZ());
		setTotalTile();

		ConfigurationSection cs = arenaData.getData().getConfigurationSection("arenas." + shortName);
		cs.set("world", bukkitWorld.getName());
		cs.set("waterPool.minimum.x", minPoint.getBlockX());
		cs.set("waterPool.minimum.y", minPoint.getBlockY());
		cs.set("waterPool.minimum.z", minPoint.getBlockZ());
		cs.set("waterPool.maximum.x", maxPoint.getBlockX());
		cs.set("waterPool.maximum.y", maxPoint.getBlockY());
		cs.set("waterPool.maximum.z", maxPoint.getBlockZ());
		arenaData.saveArenaData();

		if(isReady()) {
			gameState = GameState.READY;
		}

		return true;
	}

	public void setMaximum(CommandSender sender, String arg) {
		Language locale;
		if(sender instanceof Player player) {
			locale = playerData.getLanguageOfPlayer(player);
		} else {
			locale = Language.getDefaultLanguage();
		}
		Language defaultLocale = Language.getDefaultLanguage();

		if(gameState != GameState.READY && gameState != GameState.UNREADY) {
			locale.sendMsg(sender, locale.editLimitGameActive);
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(arg);
		} catch(NumberFormatException e) {
			locale.sendMsg(sender, locale.editLimitNaN);
			return;
		}

		if(amount < minAmountPlayer) {
			locale.sendMsg(sender, locale.editLimitErrorMinMax);
			return;
		}

		if(amount > colorManager.getArenaBlocks().size()) {
			locale.sendMsg(sender, locale.editColorColorLessPlayer);
			return;
		}

		if(amount > 12) {
			locale.sendMsg(sender, locale.editLimitMaxAboveMax);
			return;
		}

		scoreboard.resetScores(ChatColor.GOLD + defaultLocale.keyWordGeneralMaximum + " = " + ChatColor.AQUA
				+ maxAmountPlayer);
		maxAmountPlayer = amount;
		objective.getScore(
						ChatColor.GOLD + defaultLocale.keyWordGeneralMaximum + " = " + ChatColor.AQUA + maxAmountPlayer)
				.setScore(3);

		arenaData.getData().set("arenas." + shortName + ".maxPlayer", amount);
		arenaData.saveArenaData();

		locale.sendMsg(sender,
				locale.editLimitMaxSuccess.replace("%amount%", String.valueOf(amount)).replace("%arenaName%", shortName));
	}

	public void setMinimum(CommandSender sender, String arg) {
		Language locale;
		if(sender instanceof Player player) {
			locale = playerData.getLanguageOfPlayer(player);
		} else {
			locale = Language.getDefaultLanguage();
		}
		Language defaultLocale = Language.getDefaultLanguage();

		if(gameState != GameState.READY && gameState != GameState.UNREADY) {
			locale.sendMsg(sender, locale.editLimitGameActive);
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(arg);
		} catch(NumberFormatException e) {
			locale.sendMsg(sender, locale.editLimitNaN);
			return;
		}

		if(amount < 2) {
			locale.sendMsg(sender, locale.editLimitMinBelowMin);
			return;
		}
		if(amount > maxAmountPlayer) {
			locale.sendMsg(sender, locale.editLimitErrorMinMax);
			return;
		}

		scoreboard.resetScores(ChatColor.GOLD + defaultLocale.keyWordGeneralMinimum + " = " + ChatColor.AQUA
				+ minAmountPlayer);
		minAmountPlayer = amount;
		objective.getScore(
						ChatColor.GOLD + defaultLocale.keyWordGeneralMinimum + " = " + ChatColor.AQUA + minAmountPlayer)
				.setScore(2);

		arenaData.getData().set("arenas." + shortName + ".minPlayer", amount);
		arenaData.saveArenaData();

		locale.sendMsg(sender,
				locale.editLimitMinSuccess.replace("%amount%", String.valueOf(amount)).replace("%arenaName%", shortName));
	}

	public void setDisplayName(CommandSender sender, String displayName) {
		Language locale;
		if(sender instanceof Player player) {
			locale = playerData.getLanguageOfPlayer(player);
		} else {
			locale = Language.getDefaultLanguage();
		}

		if(gameState != GameState.READY && gameState != GameState.UNREADY) {
			locale.sendMsg(sender, locale.editLimitGameActive);
			return;
		}

		if(displayName.isEmpty()) {
			locale.sendMsg(sender, locale.editErrorNoParameter);
			return;
		}

		this.displayName = displayName;

		arenaData.getData().set("arenas." + shortName + ".displayName", displayName);
		arenaData.saveArenaData();

		locale.sendMsg(sender,
				Utils.replaceInComponent(
						locale.editNameSuccess,
						"%newName%", Component.text(displayName),
						"%arena%", Component.text(shortName)
				)
		);
	}

	public boolean isAllSet() {
		return lobby != null && platform != null && maxPoint != null && minPoint != null;
	}

	/******************************************************************************************************
	 * Display Information
	 */

	public void displayInformation(Player player) {
		Language local = playerData.getLanguageOfPlayer(player);

		String stringGameState;
		switch(gameState) {
			case ACTIVE:
				stringGameState = local.keyWordGameStateActive;
				break;
			case READY:
				stringGameState = local.keyWordGameStateReady;
				break;
			case STARTUP:
				stringGameState = local.keyWordGameStateStartup;
				break;
			case ENDING:
			case UNREADY:
			default:
				stringGameState = local.keyWordGameStateUnset;
		}

		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&8&m" + " ".repeat(13) + "&r &3DeACoudre &b"
						+ local.keyWordHelpInformation + " &3: &b" + displayName + " &8&m" + " ".repeat(13)));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&3" + local.keyWordHelpCurrent + " " + local.keyWordGameState + ": &7" + stringGameState));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3" + local.keyWordHelpCurrent + " "
				+ local.keyWordHelpAmountPlayer + ": &7" + getNonEliminated().size()));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&3" + local.keyWordGeneralMinimum + " " + local.keyWordHelpAmountPlayer + ": &7" + minAmountPlayer));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&3" + local.keyWordGeneralMaximum + " " + local.keyWordHelpAmountPlayer + ": &7" + maxAmountPlayer));
		player.sendMessage("\n");

		if(!Permissions.hasPermission(player, Permissions.permissionAdvancedInfo, false)) {
			return;
		}

		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&8&m" + " ".repeat(5) + "&r &3DeACoudre &b"
						+ local.keyWordHelpAdvanced + " &3: &b" + displayName + " &8&m" + " ".repeat(5)));
		if(world == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3" + local.keyWordHelpWorld + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordHelpWorld + ": &7" + world.getName()));
		}
		if(lobby == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3" + local.keyWordHelpLobby + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordHelpLobby + ": &7{" + ((int) (lobby.getX() * 100)) / (double) 100 + ", "
							+ ((int) (lobby.getY() * 100)) / (double) 100 + ", "
							+ ((int) (lobby.getZ() * 100)) / (double) 100 + "}"));
		}
		if(platform == null) {
			player.sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&3" + local.keyWordHelpPlatform + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordHelpPlatform + ": &7{" + ((int) (platform.getX() * 100)) / (double) 100 + ", "
							+ ((int) (platform.getY() * 100)) / (double) 100 + ", "
							+ ((int) (platform.getZ() * 100)) / (double) 100 + "}"));
		}
		if(minPoint == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordGeneralMinimum + local.keyWordHelpPool + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordGeneralMinimum + local.keyWordHelpPool + ": &7{" + minPoint.getBlockX() + ", "
							+ minPoint.getBlockY() + ", " + minPoint.getBlockZ() + "}"));
		}
		if(maxPoint == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordGeneralMaximum + local.keyWordHelpPool + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordGeneralMaximum + local.keyWordHelpPool + ": &7{" + maxPoint.getBlockX() + ", "
							+ maxPoint.getBlockY() + ", " + maxPoint.getBlockZ() + "}"));
		}
		player.sendMessage("\n");
	}

	/******************************************************************************************************
	 * Big Game Methodes
	 */

	public boolean addPlayerToTeam(Player player) {
		// No need to check if player is already in a game, sections ensure that he will quit previous game appropriately

		if(gameState == GameState.UNREADY) {
			arenaUI.onPlayerJoinedUnsetArena(player);
			return false;
		}

		boolean eliminated = false;

		if(gameState == GameState.ACTIVE || gameState == GameState.ENDING) {
			arenaUI.onPlayerJoinedActiveOrEndingArena(player);
			eliminated = true;
		}

		if(!eliminated) {
			if(getNonEliminated().size() >= maxAmountPlayer) {
				arenaUI.onPlayerJoinedNonStartedFullGame(player);
				eliminated = true;
			}
		}

		User user = new User(playerData, player, this, eliminated);
		users.add(user);

		if(player.getLocation().distance(lobby) > 1) {
			arenaUI.onLobbyTeleportFailed(player);
			// No need to teleport player, returning false will force Section Manager to move player elsewhere
			return false;
		}

		InventoryBar.giveArenaLobbyTools(this, user, playerData.getLanguageOfPlayer(player));

		DacSign.updateSigns(this);

		if(!eliminated) {
			arenaUI.onPlayerJoined(user, this);
		} else if(gameState == GameState.ACTIVE || gameState == GameState.ENDING) {
			user.maxStats(true);
		}

		if(getNonEliminated().size() >= minAmountPlayer && config.autostart && gameState == GameState.READY) {
			if(startTime + 30000 > System.currentTimeMillis()) {
				arenaUI.onAutoStartFailed(this);
				// Return true as this is not a fatal error
				return true;
			}

			gameState = GameState.STARTUP;
			setStartTime();
			countdown(this, config.countdownTime * 20);

			arenaUI.onCountdownStarted(this);
		}

		return true;
	}

	public boolean removePlayerFromGame(Player player) {
		Arena arena = getArenaFromPlayer(player);
		if(arena == null) {
			Language local = playerData.getLanguageOfPlayer(player);
			local.sendMsg(player, local.errorNotInGame);
			return false;
		}

		User user = getUser(player);

		if(user.getColor() != null) {
			user.getColor().setAvailable(true);
		}

		User newUser = getNonEliminated().size() == maxAmountPlayer && getNonEliminated().size() < users.size()
				? getFirstWaitingPlayer() : null;

		if(!user.isEliminated()) {
			eliminateUser(user);

			if(gameState != GameState.ENDING) {
				arenaUI.onPlayerQuitRunningGame(user, this);
			}
		}

		users.remove(user);
		scoreboard.resetScores(user.getName());
		user.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		if((gameState == GameState.READY || gameState == GameState.STARTUP) && newUser != null) {
			newUser.unEliminate(this);

			arenaUI.onPlayerJoinedActiveGameBecauseOfLeaver(user, newUser, this);
		}

		DacSign.updateSigns(this);
		user.maxStats(false);
		user.returnOriginalPlayerStats();

		if(gameState == GameState.STARTUP) {
			if(getNonEliminated().size() < minAmountPlayer) {
				gameState = GameState.READY;
			}
		}

		if(gameState != GameState.ACTIVE) {
			// Player left while game is not active, nothing more to do
			return true;
		}

		if(isOver()) {
			try {
				activePlayer.getPlayer().teleport(lobby);
			} catch(NullPointerException e) {
			}
			finishGame(false);
		} else if(user == activePlayer) {
			nextPlayer();
		}

		return true;
	}

	private User getFirstWaitingPlayer() {
		for(User user : users) {
			if(user.isEliminated()) {
				return user;
			}
		}
		return null;
	}

	private void eliminateUser(User user) {
		user.eliminate();

		if(gameState != GameState.ACTIVE) {
			return;
		}

		int gameLost = 0;
		if(mysql.hasConnection()) {
			ResultSet query = mysql.query(
					"SELECT gamesLost FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + user.getUUID() + "';");
			try {
				if(query.next()) {
					gameLost = query.getInt("gamesLost");
				}
			} catch(SQLException e) {
				Log.severe("Error on player's number of lost games retrieval.", e);
			}

			mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET gamesLost='" + ++gameLost + "' WHERE UUID='"
					+ user.getUUID() + "';");
		} else {
			gameLost = playerData.getData().getInt("players." + user.getUUID() + ".gamesLost", 0) + 1;
			playerData.getData().set("players." + user.getUUID() + ".gamesLost", gameLost);
			playerData.savePlayerData();
		}

		achievements.testAchievement(Achievement.gamesLost, user.getPlayer());

		updateStats(user);
	}

	private void updateStats(User user) {
		int games = 0;
		int timePlayed = 0;

		if(mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT gamesPlayed, timePlayed FROM " + config.tablePrefix
					+ "PLAYERS WHERE UUID='" + user.getUUID() + "';");

			try {
				if(query.next()) {
					games = query.getInt("gamesPlayed");
					timePlayed = query.getInt("timePlayed");
				}
			} catch(SQLException e) {
				Log.severe("Error on player's number of games played retrieval.", e);
			}
		} else {
			games = playerData.getData().getInt("players." + user.getUUID() + ".gamesPlayed", 0);
			timePlayed = playerData.getData().getInt("players." + user.getUUID() + ".stats.timePlayed", 0);
		}

		games++;
		timePlayed += System.currentTimeMillis() - startTime;

		if(mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET timePlayed='" + timePlayed + "', gamesPlayed='"
					+ games + "' WHERE UUID='" + user.getUUID() + "';");
		} else {
			playerData.getData().set("players." + user.getUUID() + ".stats.timePlayed", timePlayed);
			playerData.getData().set("players." + user.getUUID() + ".gamesPlayed", games);
			playerData.savePlayerData();
		}

		achievements.testAchievement(Achievement.gamesPlayed, user.getPlayer());
	}

	public void startGame(boolean forceStart) {
		this.forceStart = forceStart;
		Random r = new Random();
		List<User> temporary = new ArrayList<>(getNonEliminated());

		for(User user : temporary) {
			Player player = user.getPlayer();

			if(user.getColor() == null) {
				user.setColor(colorManager.getRandomAvailableArenaBlock());

				playerUI.onAssignedColor(user);
			}

			InventoryBar.giveGameTools(user);
		}

		if(!forceStart) {
			arenaUI.onArenaNonForcedStart(this);
		}

		for(int i = 1; !temporary.isEmpty(); i++) {
			int j = r.nextInt(temporary.size());
			User user = temporary.get(j);
			user.setPlace(i);
			temporary.remove(j);

			if(!forceStart) {
				playerUI.onPlayerAssignedPosition(user, this, i);
			}
		}

		arenaUI.onArenaStart(this);

		if(getNonEliminated().size() == 8) {
			for(User user : getNonEliminated()) {
				achievements.testAchievement(Achievement.eightPlayersGame, user.getPlayer());
			}
		}

		gameState = GameState.ACTIVE;
		DacSign.updateSigns(this);
		startTime = System.currentTimeMillis();

		fillWater();
		setTotalTile();

		activePlayer = lastPlayer();
		nextPlayer();
	}

	public void nextPlayer() {
		if(isDacFinished()) {
			if(forceStart) {
				for(User user : getNonEliminated()) {
					user.eliminate();
				}
			}

			finishGame(true);
			return;
		}

		if(!forceStart) {
			if(stallingAmount > config.maxFailBeforeEnding) {
				reviveConfirmationQueue();
				finishGame(false);
				return;
			}
		}

		if(isLastPlayer(activePlayer)) {
			newRound();

			if(getRoundNo() == 100 && !forceStart) {
				for(User user : getNonEliminated()) {
					achievements.testAchievement(Achievement.reachRoundHundred, user.getPlayer());
				}
			}

			setSomeoneLostFinal(false);
			for(User user : users) {
				user.setRoundSuccess(false);
			}
		}

		activePlayer.getPlayer().setLevel(0);
		activePlayer.getPlayer().setExp(0);
		activePlayer = new User(activePlayer.getPlace());

		final Arena arena = this;
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			activePlayer = getNextPlayer();

			arenaUI.onPlayerChange(activePlayer, this);

			activePlayer.getPlayer().teleport(platform);
			activePlayer.maxStats(false);

			timeOut(activePlayer, arena, config.timeBeforePlayerTimeOut * 20, roundNo);
		}, 6L);
	}

	public void flushConfirmationQueue(User eliminator) {
		for(User eliminated : getNonEliminated()) {
			if(eliminated.isWaitingForConfirmation() && eliminated.getPoint() == -1) {
				eliminateUser(eliminated);

				playerUI.onNonLastPlayerEliminatedAfterOtherSuccess(eliminated, eliminator, this);
			}
		}
	}

	public void reviveConfirmationQueue() {
		for(User user : getNonEliminated()) {
			if(user.isWaitingForConfirmation()) {
				user.addPoint();

				playerUI.onNonLastPlayerRevivedAfterNoSuccess(user, this);
			}
		}
	}

	public void finishGame(boolean dacDone) {
		gameState = GameState.ENDING;

		double reward = 0;
		if(currentTile > 100) {
			currentTile = 100;
		}

		List<User> nonEliminated = getNonEliminated();

		if(nonEliminated.size() > 0) {
			if(nonEliminated.size() == 1) {
				arenaUI.onFinishSingleNonEliminated(this);

				reward = ((currentTile * currentTile / 10000.0) * (config.maxAmountReward - config.minAmountReward)
						+ config.minAmountReward);
			} else {
				if(dacDone) {
					for(User user : nonEliminated) {
						achievements.testAchievement(Achievement.completedArena, user.getPlayer());
					}

					arenaUI.onFinishCompleted(this);

					reward = ((currentTile * currentTile / 10000.0) * (config.maxAmountReward - config.minAmountReward))
							+ config.minAmountReward + config.bonusCompletingArena;
				} else {
					while(1 < nonEliminated.size()) {
						if(nonEliminated.get(0).getPoint() <= nonEliminated.get(1).getPoint()) {
							eliminateUser(nonEliminated.get(0));
							nonEliminated.remove(0);
						} else {
							eliminateUser(nonEliminated.get(1));
							nonEliminated.remove(1);
						}
					}

					arenaUI.onFinishNonCompleted(this);

					reward = ((currentTile * currentTile / 10000.0) * (config.maxAmountReward - config.minAmountReward))
							+ config.minAmountReward;
				}
			}

			for(String s : config.dispatchCommands) {
				if(!s.contains("%winner%")) {
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), s.replace("%arena%", shortName));
				}
			}

			for(User user : nonEliminated) {
				Player player = user.getPlayer();
				String UUID = player.getUniqueId().toString();

				int gamesWon = 0;
				if(mysql.hasConnection()) {
					ResultSet query = mysql.query("SELECT gamesWon FROM " + config.tablePrefix + "PLAYERS WHERE UUID='"
							+ user.getUUID() + "';");
					try {
						if(query.next()) {
							gamesWon = query.getInt("gamesWon");
						}
					} catch(SQLException e) {
						Log.severe("Error on player's number of won games retrieval.", e);
					}

					mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET gamesWon='" + ++gamesWon
							+ "' WHERE UUID='" + user.getUUID() + "';");
				} else {
					gamesWon = playerData.getData().getInt("players." + UUID + ".gamesWon") + 1;
					playerData.getData().set("players." + UUID + ".gamesWon", gamesWon);
					playerData.savePlayerData();
				}

				achievements.testAchievement(Achievement.gamesWon, user);

				updateStats(user);

				for(String s : config.dispatchCommands) {
					if(s.contains("%winner%")) {
						Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
								s.replace("%arena%", shortName).replace("%winner%", player.getName()));
					}
				}

				if(config.economyReward) {
					double newReward = Math.floor(reward);

					boolean op = player.isOp();

					player.setOp(false);

					for(short i = 255; i > 0; i--) {
						if(player.hasPermission(Permissions.PermissionMultiplier.replace("x", String.valueOf(i)))) {
							newReward = Math.floor(reward * (100 + (i * 25)) / 100);
							break;
						}
					}

					player.setOp(op);

					DeACoudre.getEconomy().depositPlayer(user.getPlayer(), newReward);
					playerUI.onRewarded(player, newReward);

					double moneyGains = 0;
					if(mysql.hasConnection()) {
						ResultSet query = mysql.query("SELECT money FROM " + config.tablePrefix + "PLAYERS WHERE UUID='"
								+ user.getUUID() + "';");
						try {
							if(query.next()) {
								moneyGains = query.getDouble("money");
							}
						} catch(SQLException e) {
							Log.severe("Error on player's money retrieval.", e);
						}

						mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET money='" + (moneyGains + newReward)
								+ "' WHERE UUID='" + user.getUUID() + "';");
					} else {
						moneyGains = playerData.getData().getDouble("players." + UUID + ".stats.moneyGains")
								+ newReward;
						playerData.getData().set("players." + UUID + ".stats.moneyGains", moneyGains);
						playerData.savePlayerData();
					}
				}

				if(!config.itemReward.equalsIgnoreCase("none")) {
					if(config.itemReward.equalsIgnoreCase("all")) {
						if(player.getInventory().firstEmpty() == -1) {
							playerUI.onRewardedSingleNoSpaceLeft(player);
							continue;
						}

						for(ItemStack itemReward : config.rewardItems) {
							if(player.getInventory().firstEmpty() == -1) {
								playerUI.onRewardedMultipleNoSpaceLeft(player);
								continue;
							}

							player.getInventory().addItem(itemReward);

							playerUI.onRewarded(player, itemReward);
						}
					} else {
						if(player.getInventory().firstEmpty() == -1) {
							playerUI.onRewardedSingleNoSpaceLeft(player);
							continue;
						}

						ItemStack itemReward = config.rewardItems.get(new Random().nextInt(config.rewardItems.size()));

						player.getInventory().addItem(itemReward);

						playerUI.onRewarded(player, itemReward);
					}
				}
			}
		} else if(forceStart) {
			arenaUI.onForceStartedArenaFinished(this);
		}

		if(config.teleportAfterEnding) {
			arenaUI.onArenaFinishedPlayerTeleported(this);
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			kickPlayers();

			if(!config.resetPoolBeforeGame) {
				fillWater();
			}
		}, config.teleportAfterEnding ? 100L : 0);
	}

	public List<Player> getBroadcastCongratulationList() {
		List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
		if(!config.broadcastCongradulations) {
			players = new ArrayList<>();
			for(User user : users) {
				players.add(user.getPlayer());
			}
		}
		return players;
	}

	private void kickPlayers() {
		Collection<User> usersToKick = new ArrayList<>(users);

		for(User user : usersToKick) {
			user.maxStats(false);
			user.returnOriginalPlayerStats();

			sectionManager.enter(user.getPlayer());
		}

		Language local = Language.getDefaultLanguage();

		spectator.unregister();

		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		spectator = scoreboard.registerNewTeam("spectator");
		spectator.setCanSeeFriendlyInvisibles(true);
		setNameTagVisibilityNever();

		objective = scoreboard.registerNewObjective(shortName, "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				ChatColor.AQUA + displayName + " &f: " + local.keyWordScoreboardPlayers));
		objective.getScore(ChatColor.GOLD + "-------------------").setScore(1);
		objective.getScore(
						ChatColor.GOLD + local.keyWordGeneralMinimum + " = " + ChatColor.AQUA + minAmountPlayer)
				.setScore(2);
		objective.getScore(
						ChatColor.GOLD + local.keyWordGeneralMaximum + " = " + ChatColor.AQUA + maxAmountPlayer)
				.setScore(3);
		users.clear();
		roundNo = 0;
		stallingAmount = 0;
		currentTile = 0;
		startTime = 0;
		gameState = GameState.READY;
		DacSign.updateSigns(this);
	}

	/******************************************************************************************************
	 * Smaller Calculating and Such Methodes OUTSIDE OF GAME
	 */

	private WorldEditPlugin getWorldEdit() {
		Plugin p = Bukkit.getPluginManager().getPlugin("WorldEdit");
		if(p instanceof WorldEditPlugin) {
			return (WorldEditPlugin) p;
		} else {
			return null;
		}
	}

	public CharSequence getPlayerListToDisplay(Language localInstance) {
		StringBuilder playerListToDisplay = new StringBuilder(users.get(0).getPlayer().getDisplayName());

		for(int i = 1; i < users.size(); i++) {
			if(i < users.size() - 1) {
				playerListToDisplay.append(localInstance.keyWordGeneralComma).append(users.get(i).getPlayer().getDisplayName());
			} else {
				playerListToDisplay.append(localInstance.keyWordGeneralAnd).append(users.get(i).getPlayer().getDisplayName());
			}
		}

		return playerListToDisplay.toString();
	}

	/******************************************************************************************************
	 * Smaller Calculating and Such Methodes DURING GAME
	 */

	private void setTotalTile() {
		totalTile = 0;
		for(int i = minPoint.getBlockX(); i <= maxPoint.getBlockX(); i++) {
			external:
			for(int k = minPoint.getBlockZ(); k <= maxPoint.getBlockZ(); k++) {
				for(int j = maxPoint.getBlockY(); j >= minPoint.getBlockY(); j--) {
					Block block = new Location(world, i, j, k).getBlock();
					if(block.isLiquid()) {
						totalTile++;
						continue external;
					} else if(block.getType() != Material.AIR) {
						continue external;
					}
				}
			}
		}
	}

	public void fillWater() {
		Set<Material> arenaMaterials = colorManager.getArenaMaterials();

		for(int x = minPoint.getBlockX(); x <= maxPoint.getBlockX(); x++) {
			for(int y = maxPoint.getBlockY(); y >= minPoint.getBlockY(); y--) {
				nextBlock:
				for(int z = minPoint.getBlockZ(); z <= maxPoint.getBlockZ(); z++) {
					Location location = new Location(world, x, y, z);
					Block block = location.getBlock();

					if(arenaMaterials.contains(block.getType())) {
						block.setType(Material.WATER);
						continue nextBlock;
					}
				}
			}
		}
	}

	public void resetArena(ItemStack item) {
		for(int x = minPoint.getBlockX(); x <= maxPoint.getBlockX(); x++) {
			for(int y = maxPoint.getBlockY(); y >= minPoint.getBlockY(); y--) {
				nextBlock:
				for(int z = minPoint.getBlockZ(); z <= maxPoint.getBlockZ(); z++) {
					Location location = new Location(world, x, y, z);
					Block block = location.getBlock();

					if(item.getType() == block.getType()) {
						block.setType(Material.WATER);
						continue nextBlock;
					}
				}
			}
		}
	}

	public User getUser(Player p) {
		for(User user : users) {
			if(user.getPlayer() == p) {
				return user;
			}
		}
		return null;
	}

	private User lastPlayer() {
		User user = new User(0);
		for(User u : getNonEliminated()) {
			if(u.getPlace() > user.getPlace()) {
				user = u;
			}
		}
		return user;
	}

	public boolean isLastPlayer(User user) {
		return lastPlayer().getPlace() <= user.getPlace();
	}

	private User firstPlayer() {
		User user = new User(maxAmountPlayer);
		for(User u : getNonEliminated()) {
			if(u.getPlace() < user.getPlace()) {
				user = u;
			}
		}
		return user;
	}

	public User getNextPlayer() {
		if(isLastPlayer(activePlayer)) {
			return firstPlayer();
		}

		User nextPlayer = new User(maxAmountPlayer);
		for(User user : getNonEliminated()) {
			if(user.getPlace() > activePlayer.getPlace() && user.getPlace() <= nextPlayer.getPlace()) {
				nextPlayer = user;
			}
		}
		return nextPlayer;
	}

	public boolean isOver() {
		return getNonEliminated().size() < 2;
	}

	/******************************************************************************************************
	 * Getters and Setters
	 */

	public List<User> getUsers() {
		return users;
	}

	public void bumpCurrentTile() {
		currentTile++;
	}

	public Location getMinPoolPoint() {
		return minPoint;
	}

	public Location getMaxPoolPoint() {
		return maxPoint;
	}

	public String getShortName() {
		return shortName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime() {
		startTime = System.currentTimeMillis();
	}

	public int getMinPlayer() {
		return minAmountPlayer;
	}

	public int getMaxPlayer() {
		return maxAmountPlayer;
	}

	public Location getLobby() {
		return lobby;
	}

	/********************************************
	 * Arena setup
	 */

	public void setLobby(Player player) {
		gameState = GameState.UNREADY;

		try {
			if(!world.getName().equalsIgnoreCase(player.getWorld().getName())) {
				DacSign.removePlaySigns(this);
			}
		} catch(NullPointerException e) {

		}

		world = player.getWorld();
		lobby = player.getLocation();
		lobby.add(new Vector(0, 0.5, 0));

		ConfigurationSection cs = arenaData.getData().getConfigurationSection("arenas." + shortName);
		cs.set("world", world.getName());
		cs.set("lobby.x", lobby.getX());
		cs.set("lobby.y", lobby.getY());
		cs.set("lobby.z", lobby.getZ());
		cs.set("lobby.pitch", lobby.getPitch());
		cs.set("lobby.yaw", lobby.getYaw());
		arenaData.saveArenaData();

		if(isReady()) {
			gameState = GameState.READY;
		}
	}

	public Location getPlatform() {
		return platform;
	}

	public void setPlatform(Player player) {
		gameState = GameState.UNREADY;

		if(!world.getName().equalsIgnoreCase(player.getWorld().getName())) {
			DacSign.removePlaySigns(this);
		}

		world = player.getWorld();
		platform = player.getLocation();
		platform.add(new Vector(0, 0.5, 0));

		ConfigurationSection cs = arenaData.getData().getConfigurationSection("arenas." + shortName);
		cs.set("world", world.getName());
		cs.set("platform.x", platform.getX());
		cs.set("platform.y", platform.getY());
		cs.set("platform.z", platform.getZ());
		cs.set("platform.pitch", platform.getPitch());
		cs.set("platform.yaw", platform.getYaw());
		arenaData.saveArenaData();

		if(isReady()) {
			gameState = GameState.READY;
		}
	}

	public boolean isDacFinished() {
		return currentTile == totalTile;
	}

	public void resetStallingAmount() {
		stallingAmount = 0;
	}

	public void bumpStallingAmount() {
		stallingAmount++;
	}

	private void newRound() {
		int previousRoundNo = roundNo;
		roundNo++;

		arenaUI.onNewRound(this, previousRoundNo);
	}

	public boolean isSomeoneSurvived() {
		for(User p : getNonEliminated()) {
			if(p.isRoundSuccess()) {
				return true;
			}
		}

		return false;
	}

	public int getRoundNo() {
		return roundNo;
	}

	public boolean isSomeoneLostFinal() {
		return someoneLostFinal;
	}

	public void setSomeoneLostFinal(boolean someoneLostFinal) {
		this.someoneLostFinal = someoneLostFinal;
	}

	public boolean isForceStart() {
		return forceStart;
	}

	public void countdown(final Arena arena, final int time) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

			if(arena.gameState != GameState.STARTUP) {
				for(User user : arena.getUsers()) {
					user.maxStats(false);

					arenaUI.onCountdownCancelled(user.getPlayer());
				}

				return;
			}

			if(time == 0) {
				for(User user : arena.getUsers()) {
					user.maxStats(true);
				}

				arena.startGame(false);
			} else {
				arenaUI.onCountdownStep(this, time);
			}

			arena.countdown(arena, time - 1);
		}, 1L);
	}

	public void timeOut(final User user, final Arena arena, final int time, final int round) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			Player player = user.getPlayer();
			if(user != arena.activePlayer || arena.gameState != GameState.ACTIVE || arena.getRoundNo() != round) {
				return;
			}

			if(time == 0) {
				user.getPlayer().teleport(lobby);
				user.maxStats(true);
				if(!arena.forceStart) {
					achievements.testAchievement(Achievement.longTime, user.getPlayer());
				}

				if(config.timeOutKick) {
					playerUI.onPlayerTimeOut(user, this);

					arena.eliminateUser(user);

					if(arena.isOver()) {
						arena.getActivePlayer().getPlayer().teleport(arena.getLobby());
						arena.finishGame(false);
					} else {
						arena.nextPlayer();
					}
				} else {
					onJumpFailed(user);
				}

				return;
			}

			playerUI.onPlayerTimeTick(player, time);

			timeOut(user, arena, time - 1, round);
		}, 1L);
	}

	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public ColorManager getColorManager() {
		return colorManager;
	}

	public List<User> getNonEliminated() {
		List<User> nonEliminated = new ArrayList<>();
		for(User user : users) {
			if(!user.isEliminated()) {
				nonEliminated.add(user);
			}
		}
		return nonEliminated;
	}

	public Objective getObjective() {
		return objective;
	}

	public Team getSpectator() {
		return spectator;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public World getWorld() {
		return world;
	}

	public void onJumpInPool(User user, Location getTo) {
		Player player = user.getPlayer();

		while(getTo.add(new Vector(0, 1, 0)).getBlock().isLiquid()) {
		}
		getTo.add(new Vector(0, -1, 0));

		Location north = new Location(getTo.getWorld(), getTo.getBlockX(), getTo.getBlockY(), getTo.getBlockZ() - 1);
		Location south = new Location(getTo.getWorld(), getTo.getBlockX(), getTo.getBlockY(), getTo.getBlockZ() + 1);
		Location east = new Location(getTo.getWorld(), getTo.getBlockX() + 1, getTo.getBlockY(), getTo.getBlockZ());
		Location west = new Location(getTo.getWorld(), getTo.getBlockX() - 1, getTo.getBlockY(), getTo.getBlockZ());

		playerUI.onJumpSucceeded(user, this, getTo);

		resetStallingAmount();
		bumpCurrentTile();

		if(!north.getBlock().isLiquid() && !south.getBlock().isLiquid() && !west.getBlock().isLiquid()
				&& !east.getBlock().isLiquid()) {
			// Player has done a DaC

			user.addPoint();

			int DaCdone = 0;
			if(mysql.hasConnection()) {
				ResultSet query = mysql.query(
						"SELECT DaCdone FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + user.getUUID() + "';"
				);
				try {
					if(query.next()) {
						DaCdone = query.getInt("DaCdone");
					}
				} catch(SQLException e) {
					Log.severe("Error on player's number of DaC retrieval.", e);
				}

				mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET DaCdone='" + ++DaCdone + "' WHERE UUID='"
						+ user.getUUID() + "';");
			} else {
				DaCdone = playerData.getData().getInt("players." + player.getUniqueId() + ".DaCdone") + 1;
				playerData.getData().set("players." + player.getUniqueId() + ".DaCdone", DaCdone);
				playerData.savePlayerData();
			}

			if(!isForceStart()) {
				achievements.testAchievement(Achievement.dacDone, player);

				if(getRoundNo() == 42) {
					achievements.testAchievement(Achievement.dacOnFortyTwo, player);
				}
			}

			playerUI.onDaC(this, user);
		} else {
			// Player successfully went in the pool
			playerUI.onRegularJump(this, user);
		}

		user.setRoundSuccess(true);
		flushConfirmationQueue(user);

		if(!isOver() || isForceStart()) {
			nextPlayer();
		} else {
			user.getPlayer().setVelocity(new Vector());
			user.getPlayer().setFallDistance(0);
			finishGame(false);
		}
	}

	public void onJumpFailed(User user) {
		final Player player = user.getPlayer();

		user.removePoint();
		player.teleport(getLobby());
		user.maxStats(true);

		playerUI.onJumpFailed(user, this);

		bumpStallingAmount();

		if(isForceStart()) {
			if(user.getPoint() == -1) {
				user.eliminate();

				playerUI.onPlayerEliminated(user, this);

				finishGame(false);
			} else {
				nextPlayer();

				playerUI.onPlayerLostLife(user, this);
			}
			return;
		}

		if(isSomeoneSurvived()) {
			if(user.getPoint() == -1) {
				// IF someone already succeeded
				// AND damaged player lost his LAST life

				user.eliminate();

				playerUI.onPlayerEliminated(user, this);
			} else {
				// IF someone already succeeded
				// AND damaged player lost normal life

				playerUI.onPlayerLostLife(user, this);
			}
		} else {
			if(isLastPlayer(user)) {
				if(user.getPoint() == -1) {
					// IF everybody failed this round
					// AND damaged player is last
					// AND damaged player lost his LAST life

					user.addPoint();
					setSomeoneLostFinal(true);

					playerUI.onLastRoundPlayerEliminatedAndRevivalOfEveryone(user, this);
				} else if(isSomeoneLostFinal()) {
					// IF everybody failed this round
					// AND damaged player is last
					// AND damaged player lost normal life
					// AND other player lost his LAST life

					user.addWaitingForConfirmation();

					playerUI.onLastRoundPlayerLostLifeRevivalOfEveryone(user, this);
				} else {
					// IF everybody failed this round
					// AND damaged player is last
					// AND damaged player lost normal life
					// AND nobody lost his LAST life

					playerUI.onPlayerLostLife(user, this);
				}
			} else {
				if(user.getPoint() == -1) {
					// IF everybody failed this round
					// AND damaged player NOT last
					// AND damaged player lost his LAST life

					user.addWaitingForConfirmation();
					setSomeoneLostFinal(true);

					playerUI.onNonLastRoundPlayerEliminated(user, this);
				} else {
					// IF everybody failed this round
					// AND damaged player NOT last
					// AND damaged player lost normal life

					user.addWaitingForConfirmation();

					playerUI.onNonLastRoundPlayerLostLife(user, this);
				}
			}
		}

		if(isLastPlayer(user) && isSomeoneLostFinal()) {
			reviveConfirmationQueue();
		}

		if(isOver()) {
			finishGame(false);
		} else {
			nextPlayer();
		}
	}
}
