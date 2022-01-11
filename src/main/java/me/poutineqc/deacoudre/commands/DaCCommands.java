package me.poutineqc.deacoudre.commands;

import fr.prodrivers.bukkit.commons.sections.Section;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import me.poutineqc.deacoudre.*;
import me.poutineqc.deacoudre.achievements.AchievementsGUI;
import me.poutineqc.deacoudre.ui.ColorsGUI;
import me.poutineqc.deacoudre.ui.JoinGUI;
import me.poutineqc.deacoudre.ui.SetArenaBlocksGUI;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DaCCommands implements CommandExecutor, TabCompleter {
	private static DeACoudre plugin;
	private static Configuration config;
	private static PlayerData playerData;
	private final ArenaData arenaData;
	private final SetArenaBlocksGUI chooseColorGUI;
	private final JoinGUI joinGUI;
	private final ColorsGUI playerSelectColorGUI;
	private final AchievementsGUI achievementsGUI;
	private final MySQL mysql;
	private final DacSign signData;
	private final SectionManager sectionManager;

	public DaCCommands(DeACoudre plugin) {
		DaCCommands.plugin = plugin;
		DaCCommands.config = plugin.getConfiguration();
		this.mysql = plugin.getMySQL();
		this.arenaData = plugin.getArenaData();
		DaCCommands.playerData = plugin.getPlayerData();
		this.signData = plugin.getSignData();
		this.chooseColorGUI = plugin.getChooseColorGUI();
		this.joinGUI = plugin.getJoinGUI();
		this.playerSelectColorGUI = plugin.getPlayerSelectColorGUI();
		this.achievementsGUI = plugin.getAchievementsGUI();
		this.sectionManager = plugin.getSectionManager();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdValue, String[] args) {
		Language locale;
		if(sender instanceof Player player) {
			locale = playerData.getLanguageOfPlayer(player);
		} else {
			locale = Language.getDefaultLanguage();
		}

		if(args.length == 0) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m" + " ".repeat(90)));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					locale.pluginDevelopper.replace("%developper%", plugin.getDescription().getAuthors().toString())));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					locale.pluginVersion.replace("%version%", plugin.getDescription().getVersion())));
			locale.sendMsg(sender, locale.pluginHelp.replace("%command%", cmdValue));
			sender.sendMessage("\n");
			return true;
		}

		if(args[0].equalsIgnoreCase("help")) {
			sendHelp(sender, locale, cmdValue, args);
			return true;
		}

		Arena arena;
		String cmdName;
		DaCCommandDescription command = DaCCommandDescription.getCommand(args[0]);
		if(command != null) {
			if(!command.canExecute(sender)) {
				locale.sendMsg(sender, locale.errorCommandExecutableOnlyByPlayers);
				return true;
			}

			cmdName = command.getCommandName();

			if(!Permissions.hasPermission(sender, command, true)) {
				return true;
			}

			if(cmdName.equalsIgnoreCase("language")) {
				commandLanguage(sender, args, cmdValue);
				return true;
			}

			if(cmdName.equalsIgnoreCase("info")) {
				commandInfo(sender, args, cmdValue);
				return true;
			}

			if(cmdName.equalsIgnoreCase("list") || cmdName.equalsIgnoreCase("join")
					|| cmdName.equalsIgnoreCase("play")) {
				if(args.length > 1) {
					commandJoin(command, sender, args.length, args[1], true);
				} else {
					commandJoin(command, sender, args.length, "", true);
				}

				return true;
			}

			if(cmdName.equalsIgnoreCase("color")) {
				commandColor(command, sender);
				return true;
			}

			if(cmdName.equalsIgnoreCase("quit")) {
				commandQuitGame(command, sender);
				return true;
			}

			if(cmdName.equalsIgnoreCase("start")) {
				commandStartGame(command, sender);
				return true;
			}

			if(cmdName.equalsIgnoreCase("forcestart")) {
				commandForceStartGame(sender, locale);
				return true;
			}

			if(cmdName.equalsIgnoreCase("stats")) {
				commandStats(sender);
				return true;
			}

			if(cmdName.equalsIgnoreCase("reload")) {
				commandReload(sender, locale);
				return true;
			}

			if(cmdName.equalsIgnoreCase("filetomysql")) {
				commandFilesToMySQL(sender, locale);
				return true;
			}

			locale.sendMsg(sender, locale.editErrorNoArena);
			return true;
		}

		arena = Arena.getArenaFromName(args[0]);

		if(arena == null && !(args.length > 1 && "new".equals(args[1]))) {
			locale.sendMsg(sender, locale.errorArenaOrCommandNotFound);
			return true;
		}

		if(args.length == 1) {
			locale.sendMsg(sender, locale.editErrorNoParameter);
			return true;
		}

		command = DaCCommandDescription.getCommand(args[1]);
		if(command == null) {
			locale.sendMsg(sender, locale.errorCommandNotFound);
			return true;
		}

		if(!Permissions.hasPermission(sender, command, true)) {
			return true;
		}

		cmdName = command.getCommandName();

		if(!command.canExecute(sender)) {
			locale.sendMsg(sender, locale.errorCommandExecutableOnlyByPlayers);
			return true;
		}

		if(cmdName.equalsIgnoreCase("new")) {
			commandArenaNew(sender, args, locale, arena);

			return true;
		}

		assert arena != null;

		if(cmdName.equalsIgnoreCase("delete")) {
			arena.deleteArena();
			locale.sendMsg(sender, locale.editDeleteSuccess.replace("%arenaName%", arena.getShortName()));
			return true;
		}

		if(cmdName.equalsIgnoreCase("setcolor")) {
			commandArenaSetColor(sender, arena);
			return true;
		}

		if(cmdName.equalsIgnoreCase("setminplayer")) {
			commandArenaSetMinPlayer(sender, args, locale, arena);
			return true;
		}

		if(cmdName.equalsIgnoreCase("setmaxplayer")) {
			commandArenaSetMaxPlayer(sender, args, locale, arena);
			return true;
		}

		if(cmdName.equalsIgnoreCase("setlobby")) {
			commandArenSetLobby(sender, locale, arena);
			return true;
		}

		if(cmdName.equalsIgnoreCase("setplatform")) {
			commandArenaSetPlatform(sender, locale, arena);
			return true;
		}

		if(cmdName.equalsIgnoreCase("setpool")) {
			commandArenaSetPool(sender, locale, arena);

			return true;
		}

		if(cmdName.equalsIgnoreCase("setdisplayname")) {
			commandArenaSetDisplayName(sender, args, locale, arena);
			return true;
		}

		locale.sendMsg(sender, locale.errorCommandNotFound);
		return true;
	}

	private void commandStats(CommandSender sender) {
		if(!(sender instanceof Player player)) {
			return;
		}

		achievementsGUI.openStats(player);
	}

	private void commandLanguage(CommandSender sender, String[] args, String cmdValue) {
		if(!(sender instanceof Player player)) {
			return;
		}

		Language local = playerData.getLanguageOfPlayer(player);
		if(args.length == 1) {
			local.sendMsg(player, local.languageList);
			for(Entry<String, Language> language : Language.getLanguages().entrySet()) {
				player.sendMessage("- " + language.getValue().languageName);
			}

			return;
		}

		Entry<String, Language> entrySet = Language.getLanguage(args[1]);
		if(entrySet == null) {
			local.sendMsg(player, local.languageNotFound.replace("%cmd%", cmdValue));
			return;
		}

		playerData.setLanguage(player, entrySet.getKey());
		local = playerData.getLanguageOfPlayer(player);

		local.sendMsg(player, local.languageChangeSuccess.replace("%language%", args[1]));
	}

	private void commandInfo(CommandSender sender, String[] args, String cmdValue) {
		if(!(sender instanceof Player player)) {
			return;
		}

		Language local = playerData.getLanguageOfPlayer(player);
		if(args.length == 1) {
			local.sendMsg(player, local.joinInfoMissingName);
			local.sendMsg(player, local.joinInfoTooltip.replace("%cmd%", cmdValue));
			return;
		}

		Arena arena = Arena.getArenaFromName(args[1]);
		if(arena == null) {
			local.sendMsg(player, local.errorArenaNotExist.replace("%arena%", args[1]));
			local.sendMsg(player, local.joinInfoTooltip.replace("%cmd%", cmdValue));
			return;
		}

		local.sendMsg(player, local.joinInfoTooltip.replace("%cmd%", cmdValue));
		arena.displayInformation(player);
	}

	public void commandJoin(DaCCommandDescription command, CommandSender sender, int argsLength, String arenaName, boolean teleport) {
		if(!(sender instanceof Player player)) {
			return;
		}

		if(!Permissions.hasPermission(player, command, true)) {
			return;
		}

		Language local = playerData.getLanguageOfPlayer(player);

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena != null) {
			local.sendMsg(player, local.errorAlreadyInGame);
			return;
		}

		if(argsLength == 1 || command.getCommandName().equalsIgnoreCase("list")) {
			joinGUI.openJoinGui(player);
			return;
		}

		arena = Arena.getArenaFromName(arenaName);
		if(arena == null) {
			local.sendMsg(player, local.errorArenaNotExist.replace("%arena%", arenaName));
			return;
		}

		sectionManager.enter(player, arena.getFullSectionName());
	}

	public void commandColor(DaCCommandDescription command, CommandSender sender) {
		if(!(sender instanceof Player player)) {
			return;
		}

		if(!Permissions.hasPermission(player, command, true)) {
			return;
		}

		Language local = playerData.getLanguageOfPlayer(player);

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			local.sendMsg(player, local.errorNotInGame);
			return;
		}

		if(arena.getGameState() != GameState.READY && arena.getGameState() != GameState.STARTUP) {
			local.sendMsg(player, local.errorInGame);
			return;
		}

		this.playerSelectColorGUI.openColorsGui(player, local, arena);
	}

	public void commandQuitGame(DaCCommandDescription command, CommandSender sender) {
		if(!(sender instanceof Player player)) {
			return;
		}

		if(!Permissions.hasPermission(player, command, true)) {
			return;
		}

		Language local = playerData.getLanguageOfPlayer(player);

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			local.sendMsg(player, local.errorNotInGame);
			return;
		}

		Section currentSection = sectionManager.getCurrentSection(player);
		if(currentSection == null || !currentSection.getFullName().equals(arena.getFullSectionName())) {
			Log.severe("Player is seemingly in arena " + arena.getShortName() + ", but section manager says otherwise: player is in " + currentSection + ". Kicking him.");
			player.kick(local.errorInternal);
			return;
		}

		sectionManager.enter(player);
	}

	public void commandStartGame(DaCCommandDescription command, CommandSender sender) {
		if(!(sender instanceof Player player)) {
			return;
		}


		if(!Permissions.hasPermission(player, command, true)) {
			return;
		}

		Language local = playerData.getLanguageOfPlayer(player);

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			local.sendMsg(player, local.errorNotInGame);
			return;
		}

		if(arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
			local.sendMsg(player, local.errorGameStarted);
			return;
		}

		if(arena.getGameState() == GameState.STARTUP) {
			local.sendMsg(player, local.startAlreadyStarted);
			return;
		}

		if(arena.getNonEliminated().size() < arena.getMinPlayer()) {
			local.sendMsg(player, local.startErrorQuantity.replace("%minPlayers%", String.valueOf(arena.getMinPlayer()))
					.replace("%maxPlayers%", String.valueOf(arena.getMaxPlayer())));
			return;
		}

		if(arena.getNonEliminated().size() > arena.getMaxPlayer()) {
			local.sendMsg(player, local.startErrorQuantity.replace("%minPlayers%", String.valueOf(arena.getMinPlayer()))
					.replace("%maxPlayers%", String.valueOf(arena.getMaxPlayer())));
			return;
		}

		if(arena.getStartTime() + 30000 > System.currentTimeMillis()) {
			local.sendMsg(player, local.startCooldown);
			return;
		}

		arena.setGameState(GameState.STARTUP);
		arena.setStartTime();
		player.closeInventory();
		arena.countdown(arena, config.countdownTime * 20);

		if(plugin.getConfiguration().broadcastStart) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				Language localInstance = playerData.getLanguageOfPlayer(p);
				localInstance.sendMsg(p, localInstance.startBroadcast.replaceAll("%arena%", arena.getShortName())
						.replace("%time%", String.valueOf(config.countdownTime).toString()));
			}
		}
	}

	private void commandForceStartGame(CommandSender sender, Language local) {
		if(!(sender instanceof Player player)) {
			return;
		}


		Arena arena;
		arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			local.sendMsg(player, local.errorNotInGame);
			return;
		}

		if(arena.getGameState() != GameState.READY) {
			local.sendMsg(player, local.errorGameStarted);
			return;
		}

		if(arena.getUsers().size() == 1) {
			arena.startGame(true);
		} else {
			local.sendMsg(player, local.forcestartError);
		}
	}

	private void commandReload(CommandSender sender, Language local) {
		plugin.reload();

		local.sendMsg(sender, local.reloadSucess);
	}

	private void commandFilesToMySQL(final CommandSender sender, final Language local) {
		if(!mysql.hasConnection()) {
			local.sendMsg(sender, local.convertNoMysql);
			return;
		}

		if(playerData.getData().getBoolean("converted", false)) {
			local.sendMsg(sender, local.convertAlreadyDone);
			return;
		}

		local.sendMsg(sender, local.convertStart);
		new Thread(() -> {

			if(playerData.getData().contains("players")) {
				for(String UUID : playerData.getData().getConfigurationSection("players").getKeys(false)) {

					ConfigurationSection sc = playerData.getData()
							.getConfigurationSection("players." + UUID);
					String name = sc.getString("name");
					String language = sc.getString("language");
					int gamesPlayed = sc.getInt("gamesPlayed", 0);
					int gamesWon = sc.getInt("gamesWon", 0);
					int gamesLost = sc.getInt("gamesLost", 0);
					int DaCdone = sc.getInt("DaCdone", 0);
					Boolean completedArena = sc.getBoolean("challenges.completedArena", false);
					Boolean eightPlayersGame = sc.getBoolean("challenges.8playersGame", false);
					Boolean reachRound100 = sc.getBoolean("challenges.reachRound100", false);
					Boolean DaCon42 = sc.getBoolean("challenges.DaCon42", false);
					Boolean colorRivalery = sc.getBoolean("challenges.colorRivalery", false);
					Boolean longTime = sc.getBoolean("challenges.longTime", false);
					int timePlayed = sc.getInt("stats.timePlayed", 0);
					double moneyGains = sc.getDouble("stats.moneyGains", 0);

					ResultSet query = mysql.query(
							"SELECT * FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + UUID + "';");

					try {
						if(query.next()) {
							int gamesPlayedNew = query.getInt("gamesPlayed");
							int gamesWonNew = query.getInt("gamesWon");
							int gamesLostNew = query.getInt("gamesLost");
							int DaCdoneNew = query.getInt("DaCdone");
							Boolean completedArenaNew = query.getBoolean("completedArena");
							Boolean eightPlayersGameNew = query.getBoolean("8playersGame");
							Boolean reachRound100New = query.getBoolean("reachRound100");
							Boolean DaCon42New = query.getBoolean("DaCon42");
							Boolean colorRivaleryNew = query.getBoolean("colorRivalery");
							Boolean longTimeNew = query.getBoolean("longTime");
							int timePlayedNew = query.getInt("timePlayed");
							double moneyGainsNew = query.getDouble("money");

							mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET gamesPlayed='"
									+ (gamesPlayed + gamesPlayedNew) + "', gamesWon='"
									+ (gamesWon + gamesWonNew) + "', gamesLost='"
									+ (gamesLost + gamesLostNew) + "', DaCdone='" + (DaCdone + DaCdoneNew)
									+ "', completedArena='"
									+ ((completedArena || completedArenaNew) ? "1" : " 0")
									+ "', 8playersGame='"
									+ ((eightPlayersGame || eightPlayersGameNew) ? "1" : " 0")
									+ "', reachRound100='"
									+ ((reachRound100 || reachRound100New) ? "1" : " 0") + "', DaCon42='"
									+ ((DaCon42 || DaCon42New) ? "1" : " 0") + "', colorRivalery='"
									+ ((colorRivalery || colorRivaleryNew) ? "1" : " 0") + "', longTime='"
									+ ((longTime || longTimeNew) ? "1" : " 0") + "', timePlayed='"
									+ (timePlayed + timePlayedNew) + "', money='"
									+ (moneyGains + moneyGainsNew) + "' WHERE UUID='" + UUID + "';");

						} else {
							mysql.update("INSERT INTO " + config.tablePrefix
									+ "PLAYERS (UUID, name, language,"
									+ "gamesPlayed, gamesWon, gamesLost, DaCdone,"
									+ "completedArena, 8playersGame, reachRound100, DaCon42, colorRivalery, longTime,"
									+ "timePlayed, money) VALUES ('" + UUID + "','" + name + "','"
									+ language + "','" + gamesPlayed + "','"
									+ gamesWon + "','" + gamesLost + "','"
									+ DaCdone + "','" + ((completedArena) ? "1" : "0")
									+ "','" + ((eightPlayersGame) ? "1" : "0") + "','"
									+ ((reachRound100) ? "1" : "0") + "','" + ((DaCon42) ? "1" : "0")
									+ "','" + ((colorRivalery) ? "1" : "0") + "','"
									+ ((longTime) ? "1" : "0") + "','" + timePlayed + "','"
									+ moneyGains + "');");
						}
					} catch(SQLException e) {
						Log.severe("Error on player data conversion.", e);
					}
				}
			}

			playerData.getData().set("converted", true);
			playerData.savePlayerData();

			if(arenaData.getData().contains("arenas")) {
				for(String name : arenaData.getData().getConfigurationSection("arenas").getKeys(false)) {
					ResultSet query = mysql.query(
							"SELECT * FROM " + config.tablePrefix + "ARENAS WHERE name='" + name + "';");

					try {
						if(query.next()) {
						} else {
							ConfigurationSection sc = arenaData.getData()
									.getConfigurationSection("arenas." + name);
							String world = sc.getString("world");
							int minAmountPlayer = sc.getInt("minPlayer", 2);
							int maxAmountPlayer = sc.getInt("maxPlayer", 8);
							long colorIndice = sc.getLong("colorIndice", 2122219134);
							double platformX = sc.getDouble("platform.x", 0);
							double platformY = sc.getDouble("platform.y", 0);
							double platformZ = sc.getDouble("platform.z", 0);
							double platformYaw = sc.getDouble("platform.yaw", 0);
							double platformPitch = sc.getDouble("platform.pitch", 0);
							double lobbyX = sc.getDouble("lobby.x", 0);
							double lobbyY = sc.getDouble("lobby.y", 0);
							double lobbyZ = sc.getDouble("lobby.z", 0);
							double lobbyYaw = sc.getDouble("lobby.yaw", 0);
							double lobbyPitch = sc.getDouble("lobby.pitch", 0);
							int minPointX = sc.getInt("waterPool.minimum.x", 0);
							int minPointY = sc.getInt("waterPool.minimum.y", 0);
							int minPointZ = sc.getInt("waterPool.minimum.z", 0);
							int maxPointX = sc.getInt("waterPool.maximum.x", 0);
							int maxPointY = sc.getInt("waterPool.maximum.y", 0);
							int maxPointZ = sc.getInt("waterPool.maximum.z", 0);

							mysql.update("INSERT INTO " + config.tablePrefix
									+ "ARENAS (name, world, minAmountPlayer, maxAmountPlayer, colorIndice,"
									+ "platformX, platformY, platformZ, platformYaw, platformPitch,"
									+ "lobbyX, lobbyY, lobbyZ, lobbyYaw, lobbyPitch, minPointX,"
									+ "minPointY, minPointZ, maxPointX," + "maxPointY, maxPointZ) VALUES ('"
									+ name + "','" + world + "','" + minAmountPlayer + "','"
									+ maxAmountPlayer + "','" + colorIndice
									+ "','" + platformX + "','"
									+ platformY + "','" + platformZ
									+ "','" + platformYaw + "','"
									+ platformPitch + "','" + lobbyX
									+ "','" + lobbyY + "','" + lobbyZ
									+ "','" + lobbyYaw + "','" + lobbyPitch
									+ "','" + minPointX + "','" + minPointY
									+ "','" + minPointZ + "','" + maxPointX
									+ "','" + maxPointY + "','" + maxPointZ
									+ "');");

						}
					} catch(SQLException e) {
						Log.severe("Error on arena retrieval.", e);
					}
				}
			}

			if(signData.getData().contains("signs")) {
				for(String uuid : signData.getData().getConfigurationSection("signs").getKeys(false)) {

					ConfigurationSection sc = signData.getData().getConfigurationSection("signs." + uuid);
					String locationWorld = sc.getString("location.world");
					double locationX = sc.getDouble("location.X", 0);
					double locationY = sc.getDouble("location.Y", 0);
					double locationZ = sc.getDouble("location.Z", 0);

					ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix
							+ "SIGNS WHERE (locationWorld='" + locationWorld + "' AND locationX='"
							+ locationX + "' AND locationY='" + locationY + "' AND locationZ='" + locationZ
							+ "') OR uuid='" + uuid + "';");

					try {
						if(query.next()) {
						} else {
							String type = sc.getString("type");

							mysql.update("INSERT INTO " + config.tablePrefix
									+ "SIGNS (uuid, type ,locationWorld, locationX, locationY, locationZ) "
									+ "VALUES ('" + uuid + "','" + type + "','" + locationWorld + "','"
									+ locationX + "','" + locationY + "','" + locationZ + "');");

						}
					} catch(SQLException e) {
						Log.severe("Error on signs retrieval.", e);
					}
				}
			}

			Bukkit.getScheduler().runTask(plugin, () -> {
				Arena.loadArenas();
				DacSign.loadAllSigns();
			});

			local.sendMsg(sender, local.convertComplete);
		}).start();
	}


	private void commandArenaNew(CommandSender sender, String[] args, Language locale, Arena arena) {
		if(!(sender instanceof Player player)) {
			return;
		}

		if(arena != null) {
			locale.sendMsg(player, locale.editNewExists.replace("%arenaName%", args[1]));
			return;
		}

		if(args[0].contains(".")) {
			locale.sendMsg(player, locale.editNewLong);
			return;
		}

		new Arena(args[0], player);
		locale.sendMsg(player, locale.editNewSuccess.replace("%arenaName%", args[0]));
	}

	private void commandArenaSetColor(CommandSender sender, Arena arena) {
		if(!(sender instanceof Player player)) {
			return;
		}

		chooseColorGUI.openColorGUI(player, arena);
	}

	private void commandArenaSetMinPlayer(CommandSender sender, String[] args, Language locale, Arena arena) {
		if(args.length == 2) {
			locale.sendMsg(sender, locale.editLimitNoParameter);
		} else {
			arena.setMinimum(sender, args[2]);
		}
	}

	private void commandArenaSetMaxPlayer(CommandSender sender, String[] args, Language locale, Arena arena) {
		if(args.length == 2) {
			locale.sendMsg(sender, locale.editLimitNoParameter);
		} else {
			arena.setMaximum(sender, args[2]);
		}
	}

	private void commandArenSetLobby(CommandSender sender, Language locale, Arena arena) {
		if(!(sender instanceof Player player)) {
			return;
		}

		arena.setLobby(player);
		locale.sendMsg(player, locale.editLobbySuccess.replace("%arenaName%", arena.getShortName()));
	}

	private void commandArenaSetPlatform(CommandSender sender, Language locale, Arena arena) {
		if(!(sender instanceof Player player)) {
			return;
		}

		arena.setPlatform(player);
		locale.sendMsg(player, locale.editPlatformSuccess.replace("%arenaName%", arena.getShortName()));
	}

	private void commandArenaSetPool(CommandSender sender, Language locale, Arena arena) {
		if(!(sender instanceof Player player)) {
			return;
		}

		if(arena.setPool(player)) {
			locale.sendMsg(player, locale.editPoolSuccess.replace("%arenaName%", arena.getShortName()));
		} else {
			locale.sendMsg(player, locale.editPoolNoSelection);
		}
	}

	private void commandArenaSetDisplayName(CommandSender sender, String[] args, Language locale, Arena arena) {
		if(args.length == 2) {
			locale.sendMsg(sender, locale.editLimitNoParameter);
		} else {
			arena.setDisplayName(sender, args[2]);
		}
	}

	private void sendHelp(CommandSender sender, Language locale, String cmdValue, String[] args) {
		String header = "&8&m" + " ".repeat(30) + "&r &3DeACoudre &b" + locale.keyWordHelp + " &8&m"
				+ " ".repeat(30);

		if(args.length == 1) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help general &8- " + locale.helpDescriptionGeneral));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help game &8- " + locale.helpDescriptionGame));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help arena &8- " + locale.helpDescriptionArena));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help admin &8- " + locale.helpDescriptionAdmin));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help all &8- " + locale.helpDescriptionAll));
			sender.sendMessage("\n");
			return;
		}

		int pageNumber = 1;
		CommandType commandType;
		List<DaCCommandDescription> requestedCommands;

		try {
			pageNumber = Integer.parseInt(args[1]);
			if(pageNumber < 1) {
				pageNumber = 1;
			}

			commandType = CommandType.ALL;

			requestedCommands = DaCCommandDescription.getRequiredCommands(sender, commandType);
			if(pageNumber > Math.ceil((double) requestedCommands.size() / 3)) {
				pageNumber = (int) Math.ceil((double) requestedCommands.size() / 3);
			}

		} catch(NumberFormatException e) {
			try {
				commandType = CommandType.valueOf(args[1].toUpperCase());
			} catch(IllegalArgumentException e1) {
				commandType = CommandType.ALL;
			}

			requestedCommands = DaCCommandDescription.getRequiredCommands(sender, commandType);

			if(args.length > 2) {
				try {
					pageNumber = Integer.parseInt(args[2]);
					if(pageNumber < 1) {
						pageNumber = 1;
					}

					if(pageNumber > Math.ceil((double) requestedCommands.size() / 3)) {
						pageNumber = (int) Math.ceil((double) requestedCommands.size() / 3);
					}

				} catch(NumberFormatException ex) {
				}
			}
		}

		if(requestedCommands.size() == 0) {
			pageNumber = 0;
		}

		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&3" + locale.keyWordHelpCategory + ": &7" + commandType + ", &3" + locale.keyWordHelpPage
						+ ": &7" + pageNumber + "&8/&7"
						+ (int) (Math.ceil((double) requestedCommands.size() / 3))));

		if(pageNumber == 0) {
			locale.sendMsg(sender, locale.errorPermissionHelp);
			return;
		}

		for(int i = 3 * (pageNumber - 1); i < requestedCommands.size() && i < (3 * (pageNumber - 1)) + 3; i++) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + requestedCommands.get(i).getUsage().replace("%command%", cmdValue)));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					" &8- &7" + locale.getCommandsDescription().get(requestedCommands.get(i).getDescription())));
		}
		sender.sendMessage("\n");
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command commandString, @NotNull String alias, @NotNull String[] strings) {
		boolean advancedInfo = Permissions.hasPermission(commandSender, Permissions.permissionAdvancedInfo, false);

		if(strings.length > 1) {
			DaCCommandDescription command = DaCCommandDescription.getCommand(strings[0]);

			if(command != null) {
				if(command.canExecute(commandSender)) {
					try {
						String argumentCompletion = command.getArguments().get(strings.length - 2); // Reduce 1 to have index, and reduce 1 again to account for command
						Stream<String> possibilities = switch(argumentCompletion) {
							case "locale" -> Language.getLanguages().keySet().stream();
							case "arena" -> Arena.getArenas().stream().map(Arena::getShortName);
							case "helptopics" -> Arrays.stream(CommandType.values()).map(Enum::toString).map(String::toLowerCase);
							default -> Stream.empty();
						};

						possibilities = possibilities.filter(el -> el.startsWith(strings[1]));

						return possibilities.collect(Collectors.toList());
					} catch(IndexOutOfBoundsException e) {
						// Silently ignore, return empty completion
						return Collections.emptyList();
					}
				}

				return Collections.emptyList();
			}

			if(advancedInfo && strings.length == 2) {
				Arena arena = Arena.getArenaFromName(strings[0]);

				if(arena != null) {
					return DaCCommandDescription.getRequiredCommands(commandSender, CommandType.ARENA).stream()
							.map(DaCCommandDescription::getCommandName)
							.filter(el -> !el.equals("new"))
							.filter(el -> el.startsWith(strings[1]))
							.collect(Collectors.toList());
				} else {
					if(commandSender instanceof Player && (strings[1].isEmpty() || "new".startsWith(strings[1]))) {
						return Collections.singletonList("new");
					}
				}
			}

			return Collections.emptyList();
		}

		Stream<String> possibilities = DaCCommandDescription.getCommands().stream()
				.filter(cmd -> cmd.getType() != CommandType.ARENA)
				.filter(cmd -> cmd.canExecute(commandSender))
				.map(DaCCommandDescription::getCommandName);

		if(advancedInfo) {
			possibilities = Stream.concat(
					possibilities,
					Arena.getArenas().stream().map(Arena::getShortName)
			);
		}

		possibilities = possibilities.filter(el -> el.startsWith(strings[0]));

		return possibilities
				.collect(Collectors.toList());
	}
}
