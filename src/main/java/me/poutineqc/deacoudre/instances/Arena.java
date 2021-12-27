package me.poutineqc.deacoudre.instances;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import me.poutineqc.deacoudre.ArenaData;
import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.MySQL;
import me.poutineqc.deacoudre.Permissions;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.achievements.Achievement;
import me.poutineqc.deacoudre.commands.DacSign;
import me.poutineqc.deacoudre.events.PlayerDamage;
import me.poutineqc.deacoudre.tools.ColorManager;
import me.poutineqc.deacoudre.tools.JsonBuilder;
import me.poutineqc.deacoudre.tools.JsonBuilder.JsonElement;
import me.poutineqc.deacoudre.tools.Utils;

public class Arena {

	private static DeACoudre plugin;
	private static Configuration config;
	private static MySQL mysql;
	private static PlayerData playerData;
	private static ArenaData arenaData;
	private static Achievement achievements;
	protected static PlayerDamage playerDamage;

	private String name;
	private World world;
	private Location lobby;
	private Location plateform;
	private Location minPoint;
	private Location maxPoint;
	private int maxAmountPlayer;
	private int minAmountPlayer;
	private ColorManager colorManager;

	private List<User> users = new ArrayList<User>();
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

	private static List<Arena> arenas = new ArrayList<Arena>();

	public User getActivePlayer() {
		return activePlayer;
	}

	public Arena(DeACoudre plugin) {
		Arena.plugin = plugin;
		Arena.config = plugin.getConfiguration();
		Arena.mysql = plugin.getMySQL();
		Arena.arenaData = plugin.getArenaData();
		Arena.playerData = plugin.getPlayerData();
		Arena.achievements = plugin.getAchievement();
		Arena.playerDamage = plugin.getPlayerDamage();
	}

	public static void loadArenas() {
		arenas = new ArrayList<Arena>();

		if (mysql.hasConnection()) {
			try {
				ResultSet arenas = mysql.query("SELECT * FROM " + config.tablePrefix + "ARENAS;");
				while (arenas.next()) {
					String name = arenas.getString("name");
					World world = Bukkit.getWorld(arenas.getString("world"));

					Location minPoint = new Location(world, arenas.getInt("minPointX"), arenas.getInt("minPointY"),
							arenas.getInt("minPointZ"));
					Location maxPoint = new Location(world, arenas.getInt("maxPointX"), arenas.getInt("maxPointY"),
							arenas.getInt("maxPointZ"));

					Location lobby = new Location(world, arenas.getDouble("lobbyX"), arenas.getDouble("lobbyY"),
							arenas.getDouble("lobbyZ"));
					lobby.setPitch(arenas.getFloat("lobbyPitch"));
					lobby.setYaw(arenas.getFloat("lobbyYaw"));

					Location plateform = new Location(world, arenas.getDouble("plateformX"),
							arenas.getDouble("plateformY"), arenas.getDouble("plateformZ"));
					plateform.setPitch(arenas.getFloat("plateformPitch"));
					plateform.setYaw(arenas.getFloat("plateformYaw"));

					int minAmountPlayer = arenas.getInt("minAmountPlayer");
					int maxAmountPlayer = arenas.getInt("maxAmountPlayer");
					new Arena(name, world, minPoint, maxPoint, lobby, plateform, minAmountPlayer, maxAmountPlayer);
				}
			} catch (SQLException e) {
				plugin.getLogger().info("[MySQL] Error while loading arenas.");
			}
		} else {
			if (!arenaData.getData().contains("arenas"))
				return;

			for (String arenaName : arenaData.getData().getConfigurationSection("arenas").getKeys(false)) {

				ConfigurationSection cs = arenaData.getData().getConfigurationSection("arenas." + arenaName);
				assert cs != null;
				ConfigurationSection ccs;

				playerData.getData().set("arenas." + arenaName + ".material", null);
				playerData.savePlayerData();

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

				ccs = cs.getConfigurationSection("plateform");
				Location plateform = new Location(world, ccs.getDouble("x", 0), ccs.getDouble("y", 0),
						ccs.getDouble("z", 0));
				plateform.setPitch((float) ccs.getDouble("pitch", 0));
				plateform.setYaw((float) ccs.getDouble("yaw", 0));

				new Arena(arenaName, world, minPoint, maxPoint, lobby, plateform, minAmountPlayer, maxAmountPlayer);
			}
		}
	}

	public Arena(String name, Player player) {
		this.name = name;
		world = player.getWorld();
		arenas.add(this);
		colorManager = new ColorManager(plugin, this);
		this.minAmountPlayer = 2;
		this.maxAmountPlayer = 8;

		Language local = playerData.getLanguage(config.language);
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		spectator = scoreboard.registerNewTeam("spectator");
		spectator.setCanSeeFriendlyInvisibles(true);
		setNameTagVisibilityNever();

		objective = scoreboard.registerNewObjective(name, "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				ChatColor.AQUA + name + " &f: " + local.keyWordScoreboardPlayers));
		objective.getScore(ChatColor.GOLD + "-------------------").setScore(1);
		objective.getScore(
				ChatColor.GOLD + local.keyWordGeneralMinimum + " = " + ChatColor.AQUA + String.valueOf(minAmountPlayer))
				.setScore(minAmountPlayer);
		objective.getScore(
				ChatColor.GOLD + local.keyWordGeneralMaximum + " = " + ChatColor.AQUA + String.valueOf(maxAmountPlayer))
				.setScore(maxAmountPlayer);

		if (mysql.hasConnection()) {
			mysql.update("INSERT INTO " + config.tablePrefix + "ARENAS (name, world) " + "VALUES ('" + name
					+ "','" + world.getName() + "');");
		} else {
			arenaData.getData().set("arenas." + name + ".world", world.getName());
			arenaData.saveArenaData();
		}
	}

	private void setNullIfDefault() {
		if ((0 == minPoint.getX()) && (0 == minPoint.getY()) && (0 == minPoint.getZ()))
			minPoint = null;

		if ((0 == maxPoint.getX()) && (0 == maxPoint.getY()) && (0 == maxPoint.getZ()))
			maxPoint = null;

		if ((0 == lobby.getX()) && (0 == lobby.getY()) && (0 == lobby.getZ()))
			lobby = null;

		if ((0 == plateform.getX()) && (0 == plateform.getY()) && (0 == plateform.getZ()))
			plateform = null;

		if (isReady())
			gameState = GameState.READY;

	}

	private boolean isReady() {
		return lobby != null && plateform != null && minPoint != null && maxPoint != null;
	}

	public Arena(String name, World world, Location minPoint, Location maxPoint, Location lobby, Location plateform,
			int minAmountPlayer, int maxAmountPlayer) {
		this.name = name;
		try {
			world.getName();
			this.world = world;
			this.minPoint = minPoint;
			this.maxPoint = maxPoint;
			this.lobby = lobby;
			this.plateform = plateform;
			setNullIfDefault();
		} catch (NullPointerException e) {
			this.world = null;
			this.minPoint = null;
			this.maxPoint = null;
			this.lobby = null;
			this.plateform = null;
		}

		this.minAmountPlayer = minAmountPlayer;
		this.maxAmountPlayer = maxAmountPlayer;
		colorManager = new ColorManager(plugin, this);

		Language local = playerData.getLanguage(config.language);

		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		spectator = scoreboard.registerNewTeam("spectator");
		spectator.setCanSeeFriendlyInvisibles(true);
		setNameTagVisibilityNever();

		objective = scoreboard.registerNewObjective(name, "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				ChatColor.AQUA + name + " &f: " + local.keyWordScoreboardPlayers));
		objective.getScore(ChatColor.GOLD + "-------------------").setScore(1);
		objective.getScore(
				ChatColor.GOLD + local.keyWordGeneralMinimum + " = " + ChatColor.AQUA + String.valueOf(minAmountPlayer))
				.setScore(minAmountPlayer);
		objective.getScore(
				ChatColor.GOLD + local.keyWordGeneralMaximum + " = " + ChatColor.AQUA + String.valueOf(maxAmountPlayer))
				.setScore(maxAmountPlayer);

		Logger logger = plugin.getLogger();

		if (this.minAmountPlayer < 2) {
			logger.info("The min amount of players for the arena " + name + " can't be below 2.");
			logger.info("Using by default '2'.");
			this.minAmountPlayer = 2;
		}

		if (this.maxAmountPlayer > 12) {
			logger.info("The max amount of players for the arena " + name + " can't be above 12.");
			logger.info("Using by default 12.");
			this.maxAmountPlayer = 12;
		}

		if (this.maxAmountPlayer > colorManager.getAvailableArenaBlocks().size()) {
			logger.info("The max amount of players for the arena " + name
					+ " can't be above the amount of available colors.");
			logger.info("Using by default " + colorManager.getAvailableArenaBlocks().size() + ".");
			this.maxAmountPlayer = colorManager.getAvailableArenaBlocks().size();
		}

		arenas.add(this);
	}

	private void setNameTagVisibilityNever() {
		try {
			Object craftTeam = Class
					.forName("org.bukkit.craftbukkit." + DeACoudre.NMS_VERSION + ".scoreboard.CraftTeam")
					.cast(spectator);
			Method method = craftTeam.getClass()
					.getMethod("setOption", Class.forName("org.bukkit.scoreboard.Team$Option"),
							Class.forName("org.bukkit.scoreboard.Team$OptionStatus"));
			method.setAccessible(true);
			method.invoke(craftTeam, Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			method.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteArena() {
		DacSign.arenaDelete(this);
		arenas.remove(this);

		if (mysql.hasConnection()) {
			mysql.update("DELETE FROM " + config.tablePrefix + "ARENAS WHERE name='" + name + "';");
		} else {
			arenaData.getData().set("arenas." + name, null);
			arenaData.saveArenaData();
		}
	}

	/********************************************
	 * Arena setup
	 */

	public void setLobby(Player player) {
		gameState = GameState.UNREADY;

		try {
			if (!world.getName().equalsIgnoreCase(player.getWorld().getName()))
				DacSign.removePlaySigns(this);
		} catch (NullPointerException e) {

		}

		world = player.getWorld();
		lobby = player.getLocation();
		lobby.add(new Vector(0, 0.5, 0));

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET world='" + world.getName() + "',lobbyX='"
					+ lobby.getX() + "',lobbyY='" + lobby.getY() + "',lobbyZ='" + lobby.getZ() + "',lobbyPitch='"
					+ lobby.getPitch() + "',lobbyYaw='" + lobby.getYaw() + "' WHERE name='" + name + "';");
		} else {
			ConfigurationSection cs = arenaData.getData().getConfigurationSection("arenas." + name);
			cs.set("world", world.getName());
			cs.set("lobby.x", lobby.getX());
			cs.set("lobby.y", lobby.getY());
			cs.set("lobby.z", lobby.getZ());
			cs.set("lobby.pitch", lobby.getPitch());
			cs.set("lobby.yaw", lobby.getYaw());
			arenaData.saveArenaData();
		}

		if (isReady())
			gameState = GameState.READY;
	}

	public void setPlateform(Player player) {
		gameState = GameState.UNREADY;

		if (!world.getName().equalsIgnoreCase(player.getWorld().getName()))
			DacSign.removePlaySigns(this);

		world = player.getWorld();
		plateform = player.getLocation();
		plateform.add(new Vector(0, 0.5, 0));

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET world='" + world.getName() + "',plateformX='"
					+ plateform.getX() + "',plateformY='" + plateform.getY() + "',plateformZ='" + plateform.getZ()
					+ "',plateformPitch='" + plateform.getPitch() + "',plateformYaw='" + plateform.getYaw()
					+ "' WHERE name='" + name + "';");
		} else {
			ConfigurationSection cs = arenaData.getData().getConfigurationSection("arenas." + name);
			cs.set("world", world.getName());
			cs.set("plateform.x", plateform.getX());
			cs.set("plateform.y", plateform.getY());
			cs.set("plateform.z", plateform.getZ());
			cs.set("plateform.pitch", plateform.getPitch());
			cs.set("plateform.yaw", plateform.getYaw());
			arenaData.saveArenaData();
		}

		if (isReady())
			gameState = GameState.READY;
	}

	public boolean setPool(Player player) {
		if(getWorldEdit() == null) {
			return false;
		}

		LocalSession session = getWorldEdit().getSession(player);
		if (session == null) {
			return false;
		}

		com.sk89q.worldedit.world.World world = session.getSelectionWorld();
		World bukkitWorld = BukkitAdapter.adapt(world);
		if (world == null || bukkitWorld == null) {
			return false;
		}

		Region s;
		try {
			s = session.getSelection(world);
		} catch(IncompleteRegionException e) {
			return false;
		}
		if (s == null) {
			return false;
		}

		gameState = GameState.UNREADY;

		if (!bukkitWorld.getName().equalsIgnoreCase(player.getWorld().getName()))
			DacSign.removePlaySigns(this);

		minPoint = new Location(bukkitWorld, s.getMinimumPoint().getBlockX(), s.getMinimumPoint().getBlockY(), s.getMinimumPoint().getBlockZ());
		maxPoint = new Location(bukkitWorld, s.getMaximumPoint().getBlockX(), s.getMaximumPoint().getBlockY(), s.getMaximumPoint().getBlockZ());
		setTotalTile();

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET world='" + bukkitWorld.getName() + "',minPointX='"
					+ minPoint.getBlockX() + "',minPointY='" + minPoint.getBlockY() + "',minPointZ='"
					+ minPoint.getBlockZ() + "',maxPointX='" + maxPoint.getBlockX() + "',maxPointY='"
					+ maxPoint.getBlockY() + "',maxPointZ='" + maxPoint.getBlockZ() + "' WHERE name='" + name + "';");
		} else {
			ConfigurationSection cs = arenaData.getData().getConfigurationSection("arenas." + name);
			cs.set("world", bukkitWorld.getName());
			cs.set("waterPool.minimum.x", minPoint.getBlockX());
			cs.set("waterPool.minimum.y", minPoint.getBlockY());
			cs.set("waterPool.minimum.z", minPoint.getBlockZ());
			cs.set("waterPool.maximum.x", maxPoint.getBlockX());
			cs.set("waterPool.maximum.y", maxPoint.getBlockY());
			cs.set("waterPool.maximum.z", maxPoint.getBlockZ());
			arenaData.saveArenaData();
		}

		if (isReady())
			gameState = GameState.READY;

		return true;
	}

	public void setMaximum(Player player, String arg) {
		Language local = playerData.getLanguageOfPlayer(player);

		if (gameState != GameState.READY && gameState != GameState.UNREADY) {
			local.sendMsg(player, local.editLimitGameActive);
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			local.sendMsg(player, local.editLimitNaN);
			return;
		}

		if (amount < minAmountPlayer) {
			local.sendMsg(player, local.editLimitErrorMinMax);
			return;
		}

		if (amount > colorManager.getArenaBlocks().size()) {
			local.sendMsg(player, local.editColorColorLessPlayer);
			return;
		}

		if (amount > 12) {
			local.sendMsg(player, local.editLimitMaxAboveMax);
			return;
		}

		scoreboard.resetScores(ChatColor.GOLD + local.keyWordGeneralMaximum + " = " + ChatColor.AQUA
				+ String.valueOf(maxAmountPlayer));
		maxAmountPlayer = amount;
		objective.getScore(
				ChatColor.GOLD + local.keyWordGeneralMaximum + " = " + ChatColor.AQUA + String.valueOf(maxAmountPlayer))
				.setScore(3);

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET maxAmountPlayer=" + amount + " WHERE name='"
					+ name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".maxPlayer", amount);
			arenaData.saveArenaData();
		}

		local.sendMsg(player,
				local.editLimitMaxSuccess.replace("%amount%", String.valueOf(amount)).replace("%arenaName%", name));
	}

	public void setMinimum(Player player, String arg) {
		Language local = playerData.getLanguageOfPlayer(player);

		if (gameState != GameState.READY && gameState != GameState.UNREADY) {
			local.sendMsg(player, local.editLimitGameActive);
			return;
		}

		int amount;
		try {
			amount = Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			local.sendMsg(player, local.editLimitNaN);
			return;
		}

		if (amount < 2) {
			local.sendMsg(player, local.editLimitMinBelowMin);
			return;
		}
		if (amount > maxAmountPlayer) {
			local.sendMsg(player, local.editLimitErrorMinMax);
			return;
		}

		scoreboard.resetScores(ChatColor.GOLD + local.keyWordGeneralMinimum + " = " + ChatColor.AQUA
				+ String.valueOf(minAmountPlayer));
		minAmountPlayer = amount;
		objective.getScore(
				ChatColor.GOLD + local.keyWordGeneralMinimum + " = " + ChatColor.AQUA + String.valueOf(minAmountPlayer))
				.setScore(2);

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET minAmountPlayer=" + amount + " WHERE name='"
					+ name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".minPlayer", amount);
			arenaData.saveArenaData();
		}

		local.sendMsg(player,
				local.editLimitMinSuccess.replace("%amount%", String.valueOf(amount)).replace("%arenaName%", name));
	}

	public boolean isAllSet() {
		if (lobby != null && plateform != null && maxPoint != null && minPoint != null)
			return true;
		else
			return false;
	}

	/******************************************************************************************************
	 * Display Information
	 */

	public void displayInformation(Player player) {
		Language local = playerData.getLanguageOfPlayer(player);

		String stringGameState;
		switch (gameState) {
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
						+ local.keyWordHelpInformation + " &3: &b" + name + " &8&m" + " ".repeat(13)));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&3" + local.keyWordHelpCurrent + " " + local.keyWordGameState + ": &7" + stringGameState));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3" + local.keyWordHelpCurrent + " "
				+ local.keyWordHelpAmountPlayer + ": &7" + getNonEliminated().size()));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&3" + local.keyWordGeneralMinimum + " " + local.keyWordHelpAmountPlayer + ": &7" + minAmountPlayer));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&3" + local.keyWordGeneralMaximum + " " + local.keyWordHelpAmountPlayer + ": &7" + maxAmountPlayer));
		player.sendMessage("\n");

		if (!Permissions.hasPermission(player, Permissions.permissionAdvancedInfo, false))
			return;

		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&8&m" + " ".repeat(5) + "&r &3DeACoudre &b"
						+ local.keyWordHelpAdvanced + " &3: &b" + name + " &8&m" + " ".repeat(5)));
		if (world == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3" + local.keyWordHelpWorld + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordHelpWorld + ": &7" + world.getName()));
		}
		if (lobby == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3" + local.keyWordHelpLobby + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordHelpLobby + ": &7{" + ((int) (lobby.getX() * 100)) / (double) 100 + ", "
							+ ((int) (lobby.getY() * 100)) / (double) 100 + ", "
							+ ((int) (lobby.getZ() * 100)) / (double) 100 + "}"));
		}
		if (plateform == null) {
			player.sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&3" + local.keyWordHelpPlateform + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordHelpPlateform + ": &7{" + ((int) (plateform.getX() * 100)) / (double) 100 + ", "
							+ ((int) (plateform.getY() * 100)) / (double) 100 + ", "
							+ ((int) (plateform.getZ() * 100)) / (double) 100 + "}"));
		}
		if (minPoint == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordGeneralMinimum + local.keyWordHelpPool + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + local.keyWordGeneralMinimum + local.keyWordHelpPool + ": &7{" + minPoint.getBlockX() + ", "
							+ minPoint.getBlockY() + ", " + minPoint.getBlockZ() + "}"));
		}
		if (maxPoint == null) {
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

	public void addPlayerToTeam(Player player, boolean tpAuto) {
		Language local = playerData.getLanguageOfPlayer(player);

		Arena arena = Arena.getArenaFromPlayer(player);
		if (arena != null) {
			local.sendMsg(player, local.errorAlreadyInGame);
			return;
		}

		else if (gameState == GameState.UNREADY) {
			local.sendMsg(player, local.joinStateUnset);
			return;
		}

		boolean eliminated = false;

		if (gameState == GameState.ACTIVE || gameState == GameState.ENDING) {
			local.sendMsg(player, local.joinStateStarted);
			local.sendMsg(player, local.joinAsSpectator);
			eliminated = true;
		}

		if (!eliminated)
			if (getNonEliminated().size() >= maxAmountPlayer) {
				local.sendMsg(player, local.joinStateFull);
				local.sendMsg(player, local.joinAsSpectator);
				eliminated = true;
			}

		User user = new User(player, this, tpAuto, eliminated);
		users.add(user);

		if (player.getLocation().distance(lobby) > 1 && tpAuto) {
			local.sendMsg(player,
					ChatColor.RED + "Error: Could not teleport you to the lobby. Failed to join the game.");
			removeUserFromGame(user, false);
			return;
		}

		DacSign.updateSigns(this);

		if (!eliminated) {
			local.sendMsg(player, local.joinGamePlayer.replace("%arenaName%", name).replace("%amountInGame%",
					String.valueOf(getNonEliminated().size())));

			for (User u : users)
				if (u != user) {
					Language localInstance = playerData.getLanguageOfPlayer(u);
					localInstance.sendMsg(u, localInstance.joinGameOthers.replace("%player%", user.getDisplayName())
							.replace("%amountInGame%", String.valueOf(getNonEliminated().size())));
				}
		} else if (gameState == GameState.ACTIVE || gameState == GameState.ENDING) {
			user.maxStats(true);
		}

		if (getNonEliminated().size() >= minAmountPlayer && config.autostart && gameState == GameState.READY) {
			if (startTime + 30000 > System.currentTimeMillis()) {
				local.sendMsg(player, local.startAutoFail);
				return;
			}

			gameState = GameState.STARTUP;
			setStartTime();
			countdown(this, config.countdownTime * 20);

			if (plugin.getConfiguration().broadcastStart)
				for (Player p : Bukkit.getOnlinePlayers()) {
					Language localInstance = playerData.getLanguageOfPlayer(p);
					localInstance.sendMsg(p, localInstance.startBroadcast.replaceAll("%arena%", name).replace("%time%",
							String.valueOf(config.countdownTime).toString()));
				}
		}
	}

	public boolean removePlayerFromGame(Player player) {
		Arena arena = getArenaFromPlayer(player);
		if (arena == null) {
			Language local = playerData.getLanguageOfPlayer(player);
			local.sendMsg(player, local.errorNotInGame);
			return false;
		}

		removeUserFromGame(getUser(player), true);
		return true;
	}

	public void removeUserFromGame(User user, boolean messages) {
		User newUser = getNonEliminated().size() == maxAmountPlayer && getNonEliminated().size() < users.size()
				? getFirstWaitingPlayer() : null;

		if (!user.isEliminated()) {
			eliminateUser(user);

			if (messages) {
				Language local = playerData.getLanguageOfPlayer(user);
				local.sendMsg(user, local.quitGamePlayer);
				for (User u : getUsers())
					if (user != u) {
						Language localTemp = playerData.getLanguageOfPlayer(u);
						localTemp.sendMsg(u.getPlayer(),
								localTemp.quitGameOthers.replace("%player%", user.getDisplayName()));
					}
			}
		}

		users.remove(user);
		scoreboard.resetScores(user.getName());
		user.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		if ((gameState == GameState.READY || gameState == GameState.STARTUP) && newUser != null)
			if (newUser != null) {
				newUser.unEliminate(this);
				Language local = playerData.getLanguageOfPlayer(newUser);
				local.sendMsg(newUser, local.joinNewPlacePlayer.replace("%leaver%", user.getDisplayName()));
				for (User u : users)
					if (u != newUser) {
						Language localTemp = playerData.getLanguageOfPlayer(u);
						localTemp.sendMsg(u.getPlayer(),
								localTemp.joinNewPlaceOthers.replace("%player%", newUser.getDisplayName())
										.replace("%leaver%", user.getDisplayName()));
					}
			}

		DacSign.updateSigns(this);
		user.maxStats(false);
		user.returnOriginalPlayerStats();

		if (gameState == GameState.STARTUP)
			if (getNonEliminated().size() < minAmountPlayer)
				gameState = GameState.READY;

		if (gameState != GameState.ACTIVE)
			return;

		if (isOver()) {
			try {
				activePlayer.getPlayer().teleport(lobby);
			} catch (NullPointerException e) {
			}
			finishGame(false);
		} else if (user == activePlayer)
			nextPlayer();

	}

	private User getFirstWaitingPlayer() {
		for (User user : users)
			if (user.isEliminated())
				return user;
		return null;
	}

	private void eliminateUser(User user) {

		user.eliminate();

		if (gameState != GameState.ACTIVE)
			return;

		int gameLost = 0;
		if (mysql.hasConnection()) {
			ResultSet query = mysql.query(
					"SELECT gamesLost FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + user.getUUID() + "';");
			try {
				if (query.next())
					gameLost = query.getInt("gamesLost");
			} catch (SQLException e) {
				e.printStackTrace();
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

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT gamesPlayed, timePlayed FROM " + config.tablePrefix
					+ "PLAYERS WHERE UUID='" + user.getUUID() + "';");

			try {
				if (query.next()) {
					games = query.getInt("gamesPlayed");
					timePlayed = query.getInt("timePlayed");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			games = playerData.getData().getInt("players." + user.getUUID() + ".gamesPlayed", 0);
			timePlayed = playerData.getData().getInt("players." + user.getUUID() + ".stats.timePlayed", 0);
		}

		games++;
		timePlayed += System.currentTimeMillis() - startTime;

		if (mysql.hasConnection()) {
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
		List<User> temporary = new ArrayList<User>(getNonEliminated());

		for (User user : temporary) {
			Player player = user.getPlayer();
			Language local = playerData.getLanguageOfPlayer(player);
			if (user.getColor() == null) {
				user.setColor(colorManager.getRandomAvailableArenaBlock());

				local.sendMsg(user.getPlayer(),
						local.startRandomColor
								.replace("%material%", colorManager.getBlockMaterialName(user.getColor().getItem(), local)));
			}
		}

		if (!forceStart)
			for (User user : users) {
				Language local = playerData.getLanguageOfPlayer(user);
				local.sendMsg(user, local.startRandomOrder);
			}

		for (int i = 1; !temporary.isEmpty(); i++) {
			int j = r.nextInt(temporary.size());
			User user = temporary.get(j);
			user.setPlace(i);
			temporary.remove(j);

			if (!forceStart)
				for (User u : users) {
					Language local = playerData.getLanguageOfPlayer(u);
					local.sendMsg(u, local.startPosition.replace("%player%", user.getDisplayName()).replace("%posNo%",
							String.valueOf(i)));
				}
		}

		Language local = playerData.getLanguage(config.language);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				ChatColor.AQUA + name + " &f: " + local.keyWordScoreboardPoints));
		objective.getScore(ChatColor.GOLD + "-------------------").setScore(98);
		objective.getScoreboard().resetScores(ChatColor.GOLD + local.keyWordGeneralMinimum + " = " + ChatColor.AQUA
				+ String.valueOf(minAmountPlayer));
		objective.getScoreboard().resetScores(ChatColor.GOLD + local.keyWordGeneralMaximum + " = " + ChatColor.AQUA
				+ String.valueOf(maxAmountPlayer));

		if (getNonEliminated().size() == 8)
			for (User user : getNonEliminated())
				achievements.testAchievement(Achievement.eightPlayersGame, user.getPlayer());

		gameState = GameState.ACTIVE;
		DacSign.updateSigns(this);
		startTime = System.currentTimeMillis();

		fillWater();
		setTotalTile();

		activePlayer = lastPlayer();
		nextPlayer();
	}

	public void nextPlayer() {

		if (isDacFinished()) {
			if (forceStart)
				for (User user : getNonEliminated())
					user.eliminate();

			finishGame(true);
			return;
		}

		if (!forceStart)
			if (stallingAmount > config.maxFailBeforeEnding) {
				reviveConfirmationQueue();
				finishGame(false);
				return;
			}

		if (isLastPlayer(activePlayer)) {
			newRound();

			if (getRoundNo() == 100 && !forceStart)
				for (User user : getNonEliminated())
					achievements.testAchievement(Achievement.reachRoundHundred, user.getPlayer());

			setSomeoneLostFinal(false);
			for (User user : users)
				user.setRoundSuccess(false);
		}

		activePlayer.getPlayer().setLevel(0);
		activePlayer.getPlayer().setExp(0);
		activePlayer = new User(activePlayer.getPlace());

		final Arena arena = this;
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				activePlayer = getNextPlayer();
				Player player = activePlayer.getPlayer();
				Language local = null;
				try {
					local = playerData.getLanguageOfPlayer(player);
				} catch (NullPointerException e) {
					return;
				}

				if (config.verbose) {
					local.sendMsg(activePlayer, local.gameTurnPlayer);

					for (User user : users)
						if (activePlayer != user) {
							Language localInstance = playerData.getLanguageOfPlayer(user);
							localInstance.sendMsg(user, localInstance.gameTurnOthers.replace("%player%",
									activePlayer.getPlayer().getDisplayName()));
						}
				}

				player.teleport(plateform);
				activePlayer.maxStats(false);
				scoreboard.resetScores(activePlayer.getName());
				objective.getScore(ChatColor.AQUA + activePlayer.getName()).setScore(activePlayer.getPoint());

				timeOut(activePlayer, arena, config.timeBeforePlayerTimeOut * 20, roundNo);

				Utils.sendTitle(player, JsonBuilder.getJson(
						new JsonElement(local.keyWordJumpFast, ChatColor.GOLD, true, false, false, false, false)),
						JsonBuilder.getEmpty(), 5, 10, 5);
			}
		}, 6L);
	}

	public void flushConfirmationQueue(User user) {
		for (User u : getNonEliminated()) {
			if (u.isWaitingForConfirmation() && u.getPoint() == -1) {
				eliminateUser(u);

				Language local = playerData.getLanguageOfPlayer(u);
				local.sendMsg(u, local.gamePointsFlushPlayer.replace("%player%", user.getPlayer().getDisplayName()));

				for (User op : users)
					if (op != u) {
						Language localInstance = playerData.getLanguageOfPlayer(op);
						localInstance.sendMsg(op, localInstance.gamePointsFlushOthers
								.replace("%player%", user.getDisplayName()).replace("%looser%", u.getDisplayName()));
					}
			}

		}
	}

	public void reviveConfirmationQueue() {
		for (User user : getNonEliminated())
			if (user.isWaitingForConfirmation()) {
				user.addPoint();

				Language local = playerData.getLanguageOfPlayer(user);
				local.sendMsg(user, local.gamePointsRevivePlayer.replace("%points%", String.valueOf(user.getPoint())));

				for (User u : users)
					if (u != user) {
						Language localInstance = playerData.getLanguageOfPlayer(u);
						localInstance.sendMsg(u,
								localInstance.gamePointsReviveOthers.replace("%player%", user.getDisplayName())
										.replace("%points%", String.valueOf(user.getPoint())));
					}
			}
	}

	public void finishGame(boolean dacDone) {
		gameState = GameState.ENDING;

		double reward = 0;
		if (currentTile > 100)
			currentTile = 100;

		List<User> nonEliminated = getNonEliminated();

		if (nonEliminated.size() > 0) {
			if (nonEliminated.size() == 1) {

				for (Player p : getBroadcastCongratzList()) {
					Language localInstance = playerData.getLanguageOfPlayer(p);
					localInstance.sendMsg(p,
							localInstance.endingBroadcastSingle
									.replace("%player%", nonEliminated.get(0).getPlayer().getDisplayName())
									.replace("%arenaName%", name).toString());
				}

				reward = ((currentTile * currentTile / 10000) * (config.maxAmountReward - config.minAmountReward)
						+ config.minAmountReward);

			} else {
				if (dacDone) {

					for (User user : nonEliminated)
						achievements.testAchievement(Achievement.completedArena, user.getPlayer());

					for (Player player : getBroadcastCongratzList()) {
						Language localInstance = playerData.getLanguageOfPlayer(player);
						localInstance.sendMsg(player,
								localInstance.endingBroadcastMultiple
										.replace("%players%", getPlayerListToDisplay(localInstance))
										.replace("%arenaName%", name).toString());
					}

					reward = ((currentTile * currentTile / 10000) * (config.maxAmountReward - config.minAmountReward))
							+ config.minAmountReward + config.bonusCompletingArena;

				} else {
					for (User user : users) {
						Language localInstance = playerData.getLanguageOfPlayer(user);
						localInstance.sendMsg(user.getPlayer(), localInstance.endingStall.replace("%time%",
								String.valueOf(config.maxFailBeforeEnding)));
					}

					while (1 < nonEliminated.size()) {
						if (nonEliminated.get(0).getPoint() <= nonEliminated.get(1).getPoint()) {
							eliminateUser(nonEliminated.get(0));
							nonEliminated.remove(0);
						} else {
							eliminateUser(nonEliminated.get(1));
							nonEliminated.remove(1);
						}
					}

					for (Player player : getBroadcastCongratzList()) {
						Language localInstance = playerData.getLanguageOfPlayer(player);
						localInstance.sendMsg(player,
								localInstance.endingBroadcastSingle
										.replace("%player%", nonEliminated.get(0).getDisplayName())
										.replace("%arenaName%", name).toString());
					}

					reward = ((currentTile * currentTile / 10000) * (config.maxAmountReward - config.minAmountReward))
							+ config.minAmountReward;

				}
			}

			for (String s : config.dispatchCommands)
				if (!s.contains("%winner%"))
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), s.replace("%arena%", name));

			for (User user : nonEliminated) {
				Player player = user.getPlayer();
				String UUID = player.getUniqueId().toString();

				int gamesWon = 0;
				if (mysql.hasConnection()) {
					ResultSet query = mysql.query("SELECT gamesWon FROM " + config.tablePrefix + "PLAYERS WHERE UUID='"
							+ user.getUUID() + "';");
					try {
						if (query.next())
							gamesWon = query.getInt("gamesWon");
					} catch (SQLException e) {
						e.printStackTrace();
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

				for (String s : config.dispatchCommands)
					if (s.contains("%winner%"))
						Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
								s.replace("%arena%", name).replace("%winner%", player.getName()));

				if (config.economyReward) {

					Language localInstance = playerData.getLanguageOfPlayer(player);
					double newReward = Math.floor(reward);

					boolean op = false;
					if (player.isOp())
						op = true;

					player.setOp(false);

					for (short i = 255; i > 0; i--) {
						if (player.hasPermission(Permissions.PermissionMultiplier.replace("x", String.valueOf(i)))) {
							newReward = Math.floor(reward * (100 + (i * 25)) / 100);
							break;
						}
					}

					player.setOp(op);

					DeACoudre.getEconomy().depositPlayer(user.getPlayer(), newReward);
					localInstance.sendMsg(user.getPlayer(),
							localInstance.endingRewardMoney.replace("%amount%", String.valueOf(newReward))
									.replace("%currency%", DeACoudre.getEconomy().currencyNamePlural()));

					double moneyGains = 0;
					if (mysql.hasConnection()) {
						ResultSet query = mysql.query("SELECT money FROM " + config.tablePrefix + "PLAYERS WHERE UUID='"
								+ user.getUUID() + "';");
						try {
							if (query.next())
								moneyGains = query.getDouble("money");
						} catch (SQLException e) {
							e.printStackTrace();
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

				if (!config.itemReward.equalsIgnoreCase("none")) {
					Language localInstance = playerData.getLanguageOfPlayer(player);
					if (config.itemReward.equalsIgnoreCase("all")) {
						if (player.getInventory().firstEmpty() == -1) {
							localInstance.sendMsg(player, localInstance.endingRewardItemsSpaceOne);
							continue;
						}

						for (ItemStack itemReward : config.rewardItems) {
							if (player.getInventory().firstEmpty() == -1) {
								localInstance.sendMsg(player, localInstance.endingRewardItemsSpaceMultiple);
								continue;
							}

							player.getInventory().addItem(itemReward);

							if (itemReward.getItemMeta().hasDisplayName()) {
								localInstance.sendMsg(player,
										localInstance.endingRewardItemsReceive
												.replace("%amount%", String.valueOf(itemReward.getAmount()))
												.replace("%item%", itemReward.getItemMeta().getDisplayName()));
							} else {
								localInstance.sendMsg(player,
										localInstance.endingRewardItemsReceive
												.replace("%amount%", String.valueOf(itemReward.getAmount()))
												.replace("%item%", itemReward.getType().name()));
							}
						}
					} else {
						if (player.getInventory().firstEmpty() == -1) {
							localInstance.sendMsg(player, localInstance.endingRewardItemsSpaceOne);
							continue;
						}

						ItemStack itemReward = config.rewardItems.get(new Random().nextInt(config.rewardItems.size()));

						player.getInventory().addItem(itemReward);

						if (itemReward.getItemMeta().hasDisplayName()) {
							localInstance.sendMsg(player,
									localInstance.endingRewardItemsReceive
											.replace("%amount%", String.valueOf(itemReward.getAmount()))
											.replace("%item%", itemReward.getItemMeta().getDisplayName()));
						} else {
							localInstance.sendMsg(player,
									localInstance.endingRewardItemsReceive
											.replace("%amount%", String.valueOf(itemReward.getAmount()))
											.replace("%item%", itemReward.getType().name()));
						}
					}
				}
			}
		} else if (forceStart) {
			for (User user : users) {
				Language local = playerData.getLanguageOfPlayer(user);
				local.sendMsg(user, local.endingSimulation);
			}
		}

		if (config.teleportAfterEnding)
			for (User u : users) {
				Language local = playerData.getLanguageOfPlayer(u);
				local.sendMsg(u, local.endingTeleport);
			}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				kickPlayers();

				if (!config.resetPoolBeforeGame)
					fillWater();
			}
		}, config.teleportAfterEnding ? 100L : 0);
	}

	private List<Player> getBroadcastCongratzList() {
		List<Player> players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		if (!config.broadcastCongradulations) {
			players = new ArrayList<Player>();
			for (User user : users)
				players.add(user.getPlayer());
		}
		return players;
	}

	private void kickPlayers() {
		for (User user : users) {
			user.maxStats(false);
			user.returnOriginalPlayerStats();
		}

		Language local = playerData.getLanguage(config.language);

		spectator.unregister();

		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		spectator = scoreboard.registerNewTeam("spectator");
		spectator.setCanSeeFriendlyInvisibles(true);
		setNameTagVisibilityNever();

		objective = scoreboard.registerNewObjective(name, "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				ChatColor.AQUA + name + " &f: " + local.keyWordScoreboardPlayers));
		objective.getScore(ChatColor.GOLD + "-------------------").setScore(1);
		objective.getScore(
				ChatColor.GOLD + local.keyWordGeneralMinimum + " = " + ChatColor.AQUA + String.valueOf(minAmountPlayer))
				.setScore(2);
		objective.getScore(
				ChatColor.GOLD + local.keyWordGeneralMaximum + " = " + ChatColor.AQUA + String.valueOf(maxAmountPlayer))
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
		if (p instanceof WorldEditPlugin)
			return (WorldEditPlugin) p;
		else
			return null;
	}

	public CharSequence getPlayerListToDisplay(Language localInstance) {
		String playerListToDisplay = users.get(0).getPlayer().getDisplayName();

		for (int i = 1; i < users.size(); i++) {
			if (i < users.size() - 1) {
				playerListToDisplay = playerListToDisplay + localInstance.keyWordGeneralComma
						+ users.get(i).getPlayer().getDisplayName();
			} else {
				playerListToDisplay = playerListToDisplay + localInstance.keyWordGeneralAnd
						+ users.get(i).getPlayer().getDisplayName();
			}
		}

		return playerListToDisplay;
	}

	/******************************************************************************************************
	 * Smaller Calculating and Such Methodes DURING GAME
	 */

	private void setTotalTile() {
		totalTile = 0;
		for (int i = minPoint.getBlockX(); i <= maxPoint.getBlockX(); i++)
			external: for (int k = minPoint.getBlockZ(); k <= maxPoint.getBlockZ(); k++)
				for (int j = maxPoint.getBlockY(); j >= minPoint.getBlockY(); j--) {
					Block block = new Location(world, i, j, k).getBlock();
					if (block.isLiquid()) {
						totalTile++;
						continue external;
					} else if (block.getType() != Material.AIR)
						continue external;
				}
	}

	public void fillWater() {
		Set<Material> arenaMaterials = colorManager.getArenaMaterials();

		for (int x = minPoint.getBlockX(); x <= maxPoint.getBlockX(); x++) {
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
		for (int x = minPoint.getBlockX(); x <= maxPoint.getBlockX(); x++) {
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
		for (User user : users)
			if (user.getPlayer() == p)
				return user;
		return null;
	}

	private User lastPlayer() {
		User user = new User(0);
		for (User u : getNonEliminated())
			if (u.getPlace() > user.getPlace())
				user = u;
		return user;
	}

	public boolean isLastPlayer(User user) {
		return lastPlayer().getPlace() <= user.getPlace();
	}

	private User firstPlayer() {
		User user = new User(maxAmountPlayer);
		for (User u : getNonEliminated())
			if (u.getPlace() < user.getPlace())
				user = u;
		return user;
	}

	public User getNextPlayer() {
		if (isLastPlayer(activePlayer))
			return firstPlayer();

		User nextPlayer = new User(maxAmountPlayer);
		for (User user : getNonEliminated())
			if (user.getPlace() > activePlayer.getPlace() && user.getPlace() <= nextPlayer.getPlace())
				nextPlayer = user;
		return nextPlayer;
	}

	public boolean isOver() {
		return getNonEliminated().size() < 2 ? true : false;
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

	public String getName() {
		return name;
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

	public Location getPlateform() {
		return plateform;
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
		Language l = playerData.getLanguage(config.language);

		scoreboard.resetScores(
				ChatColor.GOLD + l.keyWordScoreboardRound + " = " + ChatColor.AQUA + String.valueOf(roundNo));
		++roundNo;
		objective.getScore(ChatColor.GOLD + l.keyWordScoreboardRound + " = " + ChatColor.AQUA + String.valueOf(roundNo))
				.setScore(99);

		if (config.verbose)
			for (User p : users) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p.getPlayer(), local.gameNewRound.replace("%round%", String.valueOf(roundNo)));
			}
	}

	public boolean isSomeoneSurvived() {
		for (User p : getNonEliminated()) {
			if (p.isRoundSuccess())
				return true;
		}

		return false;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
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
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {

				if (arena.gameState != GameState.STARTUP) {
					for (User user : arena.getUsers()) {
						user.maxStats(false);

						Player player = user.getPlayer();
						Language local = playerData.getLanguageOfPlayer(player);
						local.sendMsg(player, local.startStopped);
					}

					return;
				}

				int level = (int) Math.floor(time / 20) + 1;

				for (User user : arena.getUsers()) {
					user.getPlayer().setLevel(level);
					user.getPlayer().setExp((float) ((time % 20) / 20.0));
				}

				switch (time / 20) {
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					if (time % 20.0 == 0) {
						for (User user : arena.getUsers()) {
							user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

							Language local = playerData.getLanguageOfPlayer(user);

							Utils.sendTitle(user.getPlayer(),
									JsonBuilder.getJson(new JsonElement(String.valueOf(time / 20), ChatColor.GOLD, true,
											false, false, false, false)),
									JsonBuilder.getJson(new JsonElement(local.keyWordGeneralSeconds,
											ChatColor.DARK_GRAY, false, true, false, false, false)),
									5, 10, 5);
						}
					}
					break;
				case 0:
					if (time % 20.0 == 0) {
						for (User user : users)
							user.maxStats(true);

						arena.startGame(false);
						return;
					}
				}

				arena.countdown(arena, time - 1);
			}
		}, 1L);
	}

	public void timeOut(final User user, final Arena arena, final int time, final int round) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			public void run() {
				Player player = user.getPlayer();
				if (user != arena.activePlayer || arena.gameState != GameState.ACTIVE || arena.getRoundNo() != round) {
					return;
				}
				Language local = playerData.getLanguageOfPlayer(user);

				if (time == 0) {
					user.getPlayer().teleport(lobby);
					user.maxStats(true);
					if (!arena.forceStart)
						achievements.testAchievement(Achievement.longTime, user.getPlayer());

					if (config.timeOutKick) {

						local.sendMsg(player, local.gameTimeOutPlayer);

						for (User p : arena.users) {
							if (user != p) {
								Language localInstance = playerData.getLanguageOfPlayer(p);
								localInstance.sendMsg(p.getPlayer(),
										localInstance.gameTimeOutOthers.replace("%player%", user.getDisplayName()));
							}
						}

						arena.eliminateUser(user);

						if (arena.isOver()) {
							arena.getActivePlayer().getPlayer().teleport(arena.getLobby());
							arena.finishGame(false);
						} else {
							arena.nextPlayer();
						}
					} else {
						playerDamage.losingAlgorithm(player, arena, user);
					}

					return;
				}

				int level = (int) Math.floor(time / 20) + 1;
				player.setLevel(level);
				player.setExp((float) ((time % 20) / 20.0));

				switch (time / 20) {
				case 10:
				case 9:
				case 8:
				case 7:
				case 6:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					if (time % 20 == 0) {
						player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, (float) 1, 1);
					}
				}
				timeOut(user, arena, time - 1, round);
			}
		}, 1L);
	}

	public static List<Arena> getArenas() {
		return arenas;
	}

	public static Arena getArenaFromName(String arenaName) {
		for (Arena arena : arenas)
			if (arena.getName().equalsIgnoreCase(arenaName))
				return arena;
		return null;
	}

	public static Arena getArenaFromPlayer(Player player) {
		for (Arena a : arenas)
			for (User p : a.users)
				if (player == p.getPlayer())
					return a;
		return null;
	}

	public static List<Player> getAllPlayersInStartedGame() {
		List<Player> inGame = new ArrayList<Player>();
		for (Arena arena : arenas)
			if (arena.gameState == GameState.ACTIVE)
				for (User player : arena.users)
					inGame.add(player.getPlayer());
		return inGame;
	}

	public static List<Player> getAllOutsideGame(Arena arena) {
		List<Player> outsideGame = new ArrayList<Player>();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			Arena a = getArenaFromPlayer(player);
			if (a != null)
				if (getArenaFromPlayer(player) == arena)
					continue;
			outsideGame.add(player);
		}
		return outsideGame;
	}

	public GameState getGameState() {
		return gameState;
	}

	public ColorManager getColorManager() {
		return colorManager;
	}

	public List<User> getNonEliminated() {
		List<User> nonEliminated = new ArrayList<User>();
		for (User user : users)
			if (!user.isEliminated())
				nonEliminated.add(user);
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

	// public void updateScoreBoard() {
	// Language local = playerData.getLanguage(config.language);
	//
	// int activePlace;
	// try {
	// this.activePlayer.getPlayer();
	// activePlace = this.activePlayer.getPlace();
	// } catch (NullPointerException e) {
	// activePlace = getNextPlayer().getPlace();
	// }
	//
	// User activePlayer = this.activePlayer;
	// for (User user : getNonEliminated())
	// if (user.getPlace() == activePlace)
	// activePlayer = user;
	//
	// Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	// Objective objective = scoreboard.registerNewObjective("score", "dummy");
	// objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
	// ChatColor.AQUA + name + " &f: " + local.keyWordScoreboardPoints));
	// objective.getScore(
	// ChatColor.GOLD + local.keyWordScoreboardRound + " = " + ChatColor.AQUA +
	// String.valueOf(roundNo))
	// .setScore(99);
	// objective.getScore(ChatColor.GOLD + "-------------------").setScore(98);
	//
	// int position = 0;
	// int currentPlace = activePlace - 1;
	//
	// while (currentPlace != activePlace) {
	// if (currentPlace == 0)
	// currentPlace = lastPlayer().getPlace();
	//
	// for (User user : getNonEliminated())
	// if (user.getPlace() == currentPlace) {
	// objective.getScore(user.getName() + " = " + ChatColor.YELLOW +
	// user.getPoint())
	// .setScore(position++);
	// break;
	// }
	//
	// currentPlace--;
	// }
	//
	// objective.getScore(ChatColor.AQUA + activePlayer.getName() + " = " +
	// ChatColor.YELLOW + activePlayer.getPoint())
	// .setScore(position);
	//
	// for (User user : users) {
	// user.getPlayer().setScoreboard(scoreboard);
	// }
	// }

}
