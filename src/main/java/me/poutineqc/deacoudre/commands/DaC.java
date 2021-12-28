package me.poutineqc.deacoudre.commands;

import me.poutineqc.deacoudre.*;
import me.poutineqc.deacoudre.achievements.AchievementsGUI;
import me.poutineqc.deacoudre.guis.ColorsGUI;
import me.poutineqc.deacoudre.guis.JoinGUI;
import me.poutineqc.deacoudre.guis.SetArenaBlocksGUI;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

public class DaC implements CommandExecutor {

	private static DeACoudre plugin;
	private static Configuration config;
	private static PlayerData playerData;
	private final ArenaData arenaData;
	private final SetArenaBlocksGUI chooseColorGUI;
	private final JoinGUI joinGUI;
	private final AchievementsGUI achievementsGUI;
	private final MySQL mysql;
	private final DacSign signData;

	public DaC(DeACoudre plugin) {
		DaC.plugin = plugin;
		DaC.config = plugin.getConfiguration();
		this.mysql = plugin.getMySQL();
		this.arenaData = plugin.getArenaData();
		DaC.playerData = plugin.getPlayerData();
		this.signData = plugin.getSignData();
		this.chooseColorGUI = plugin.getChooseColorGUI();
		this.joinGUI = plugin.getJoinGUI();
		this.achievementsGUI = plugin.getAchievementsGUI();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdValue, String[] args) {

		if(!(sender instanceof final Player player)) {
			sender.sendMessage("Only players can use DaC's commands.");
			return true;
		}

		Language local = playerData.getLanguageOfPlayer(player);

		if(args.length == 0) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m" + " ".repeat(90)));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					local.pluginDevelopper.replace("%developper%", plugin.getDescription().getAuthors().toString())));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					local.pluginVersion.replace("%version%", plugin.getDescription().getVersion())));
			local.sendMsg(player, local.pluginHelp.replace("%command%", cmdValue));
			player.sendMessage("\n");
			return true;
		}

		if(args[0].equalsIgnoreCase("help")) {
			sendHelp(player, cmdValue, args);
			return true;
		}

		Arena arena;
		String cmdName;
		DacCommand command = DacCommand.getCommand(args[0]);
		if(command != null) {
			cmdName = command.getCommandName();

			if(!Permissions.hasPermission(player, command, true)) {
				return true;
			}

			if(cmdName.equalsIgnoreCase("language")) {
				changeLanguage(player, args, cmdValue);
				return true;
			}

			if(cmdName.equalsIgnoreCase("info")) {
				displayInfo(player, args, cmdValue);
				return true;
			}

			if(cmdName.equalsIgnoreCase("list") || cmdName.equalsIgnoreCase("join")
					|| cmdName.equalsIgnoreCase("play")) {
				if(args.length > 1) {
					commandJoin(command, player, args.length, args[1], true);
				} else {
					commandJoin(command, player, args.length, "", true);
				}

				return true;
			}

			if(cmdName.equalsIgnoreCase("color")) {
				openColorGUI(command, player);
				return true;
			}

			if(cmdName.equalsIgnoreCase("quit")) {
				quitGame(command, player);
				return true;
			}

			if(cmdName.equalsIgnoreCase("start")) {
				startGame(command, player);
				return true;
			}

			if(cmdName.equalsIgnoreCase("forcestart")) {
				arena = Arena.getArenaFromPlayer(player);
				if(arena == null) {
					local.sendMsg(player, local.errorNotInGame);
					return true;
				}

				if(arena.getGameState() != GameState.READY) {
					local.sendMsg(player, local.errorGameStarted);
					return true;
				}

				if(arena.getUsers().size() == 1) {
					arena.startGame(true);
				} else {
					local.sendMsg(player, local.forcestartError);
				}

				return true;
			}

			if(cmdName.equalsIgnoreCase("stats")) {
				achievementsGUI.openStats(player);
				return true;
			}

			if(args[0].equalsIgnoreCase("new")) {
				switch(args.length) {
					case 1 -> local.sendMsg(player, local.editNewNoName);
					case 2 -> {
						arena = Arena.getArenaFromName(args[1]);
						if(arena != null) {
							local.sendMsg(player, local.editNewExists.replace("%arenaName%", args[1]));
							break;
						}
						if(args[1].contains(".")) {
							local.sendMsg(player, local.editNewLong);
							break;
						}
						new Arena(args[1], player);
						local.sendMsg(player, local.editNewSuccess.replace("%arenaName%", args[1]));
					}
					default -> local.sendMsg(player, local.editNewLong);
				}
				return true;
			}

			if(cmdName.equalsIgnoreCase("reload")) {

				if(mysql.hasConnection()) {
					mysql.close();
				}

				config.loadConfig(plugin);
				if(config.mysql) {
					mysql.updateInfo(plugin);
				}

				plugin.initialiseEconomy();
				plugin.loadLanguages();

				if(!mysql.hasConnection()) {
					playerData.loadPlayerData();
					arenaData.loadArenaData();
				}

				plugin.getAchievement().load_achievements();

				Arena.loadArenas();
				DacSign.loadAllSigns();

				local = playerData.getLanguageOfPlayer(player);
				local.sendMsg(player, local.reloadSucess);
				return true;
			}

			if(cmdName.equalsIgnoreCase("filetomysql")) {

				if(!mysql.hasConnection()) {
					local.sendMsg(player, local.convertNoMysql);
					return true;
				}

				if(playerData.getData().getBoolean("converted", false)) {
					local.sendMsg(player, local.convertAlreadyDone);
					return true;
				}

				local.sendMsg(player, local.convertStart);
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
									double plateformX = sc.getDouble("plateform.x", 0);
									double plateformY = sc.getDouble("plateform.y", 0);
									double plateformZ = sc.getDouble("plateform.z", 0);
									double plateformYaw = sc.getDouble("plateform.yaw", 0);
									double plateformPitch = sc.getDouble("plateform.pitch", 0);
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
											+ "plateformX, plateformY, plateformZ, plateformYaw, plateformPitch,"
											+ "lobbyX, lobbyY, lobbyZ, lobbyYaw, lobbyPitch, minPointX,"
											+ "minPointY, minPointZ, maxPointX," + "maxPointY, maxPointZ) VALUES ('"
											+ name + "','" + world + "','" + minAmountPlayer + "','"
											+ maxAmountPlayer + "','" + colorIndice
											+ "','" + plateformX + "','"
											+ plateformY + "','" + plateformZ
											+ "','" + plateformYaw + "','"
											+ plateformPitch + "','" + lobbyX
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

					Language l = playerData.getLanguageOfPlayer(player);
					l.sendMsg(player, l.convertComplete);
				}).start();
				return true;
			}

			local.sendMsg(player, local.editErrorNoArena);
			return true;
		}

		arena = Arena.getArenaFromName(args[0]);
		if(arena == null) {
			local.sendMsg(player, local.errorArenaOrCommandNotFound);
			return true;
		}

		if(args.length == 1) {
			local.sendMsg(player, local.editErrorNoParameter);
			return true;
		}

		command = DacCommand.getCommand(args[1]);
		if(command == null) {
			local.sendMsg(player, local.errorCommandNotFound);
			return true;
		}

		if(!Permissions.hasPermission(player, command, true)) {
			return true;
		}

		cmdName = command.getCommandName();

		if(cmdName.equalsIgnoreCase("delete")) {
			arena.deleteArena();
			local.sendMsg(player, local.editDeleteSuccess.replace("%arenaName%", arena.getName()));
			return true;
		}

		if(cmdName.equalsIgnoreCase("setcolor")) {
			chooseColorGUI.openColorGUI(player, arena);
			return true;
		}

		if(cmdName.equalsIgnoreCase("setminplayer")) {
			if(args.length == 2) {
				local.sendMsg(player, local.editLimitNoParameter);
			} else {
				arena.setMinimum(player, args[2]);
			}

			return true;
		}

		if(cmdName.equalsIgnoreCase("setmaxplayer")) {
			if(args.length == 2) {
				local.sendMsg(player, local.editLimitNoParameter);
			} else {
				arena.setMaximum(player, args[2]);
			}

			return true;
		}

		if(cmdName.equalsIgnoreCase("setlobby")) {
			arena.setLobby(player);
			local.sendMsg(player, local.editLobbySuccess.replace("%arenaName%", arena.getName()));
			return true;
		}

		if(cmdName.equalsIgnoreCase("setplateform")) {
			arena.setPlateform(player);
			local.sendMsg(player, local.editPlateformSuccess.replace("%arenaName%", arena.getName()));
			return true;
		}

		if(cmdName.equalsIgnoreCase("setpool")) {
			if(arena.setPool(player)) {
				local.sendMsg(player, local.editPoolSuccess.replace("%arenaName%", arena.getName()));
			} else {
				local.sendMsg(player, local.editPoolNoSelection);
			}

			return true;
		}

		local.sendMsg(player, local.errorCommandNotFound);
		return true;
	}

	private void changeLanguage(Player player, String[] args, String cmdValue) {
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

	private void displayInfo(Player player, String[] args, String cmdValue) {
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

	public void commandJoin(DacCommand command, Player player, int argsLength, String arenaName, boolean teleport) {
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
			joinGUI.openJoinGui(player, 1);
			return;
		}

		arena = Arena.getArenaFromName(arenaName);
		if(arena == null) {
			local.sendMsg(player, local.errorArenaNotExist.replace("%arena%", arenaName));
			return;
		}

		arena.addPlayerToTeam(player, teleport);
	}

	public void openColorGUI(DacCommand command, Player player) {
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

		ColorsGUI.openColorsGui(player, local, arena);
	}

	public void quitGame(DacCommand command, Player player) {
		if(!Permissions.hasPermission(player, command, true)) {
			return;
		}

		Language local = playerData.getLanguageOfPlayer(player);

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			local.sendMsg(player, local.errorNotInGame);
			return;
		}

		arena.removePlayerFromGame(player);
	}

	public void startGame(DacCommand command, Player player) {
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
				localInstance.sendMsg(p, localInstance.startBroadcast.replaceAll("%arena%", arena.getName())
						.replace("%time%", String.valueOf(config.countdownTime).toString()));
			}
		}
	}

	private void sendHelp(Player player, String cmdValue, String[] args) {
		Language local = playerData.getLanguageOfPlayer(player);
		String header = "&8&m" + " ".repeat(30) + "&r &3DeACoudre &b" + local.keyWordHelp + " &8&m"
				+ " ".repeat(30);

		if(args.length == 1) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help general &8- " + local.helpDescriptionGeneral));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help game &8- " + local.helpDescriptionGame));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help arena &8- " + local.helpDescriptionArena));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help admin &8- " + local.helpDescriptionAdmin));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3/" + cmdValue + " help all &8- " + local.helpDescriptionAll));
			player.sendMessage("\n");
			return;
		}

		int pageNumber = 1;
		CommandType commandType;
		List<DacCommand> requestedCommands;

		try {
			pageNumber = Integer.parseInt(args[1]);
			if(pageNumber < 1) {
				pageNumber = 1;
			}

			commandType = CommandType.ALL;

			requestedCommands = DacCommand.getRequiredCommands(player, commandType);
			if(pageNumber > Math.ceil((double) requestedCommands.size() / 3)) {
				pageNumber = (int) Math.ceil((double) requestedCommands.size() / 3);
			}

		} catch(NumberFormatException e) {
			switch(args[1].toLowerCase()) {
				case "game":
					commandType = CommandType.GAME_COMMANDS;
					break;
				case "arena":
					commandType = CommandType.ARENA_COMMANDS;
					break;
				case "admin":
					commandType = CommandType.ADMIN_COMMANDS;
					break;
				case "general":
					commandType = CommandType.GENERAL;
					break;
				default:
					commandType = CommandType.ALL;
			}

			requestedCommands = DacCommand.getRequiredCommands(player, commandType);

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

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&3" + local.keyWordHelpCategory + ": &7" + commandType + ", &3" + local.keyWordHelpPage
						+ ": &7" + pageNumber + "&8/&7"
						+ (int) (Math.ceil((double) requestedCommands.size() / 3))));

		if(pageNumber == 0) {
			local.sendMsg(player, local.errorPermissionHelp);
			return;
		}

		for(int i = 3 * (pageNumber - 1); i < requestedCommands.size() && i < (3 * (pageNumber - 1)) + 3; i++) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&3" + requestedCommands.get(i).getUsage().replace("%command%", cmdValue)));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					" &8- &7" + local.getCommandsDescription().get(requestedCommands.get(i).getDescription())));
		}
		player.sendMessage("\n");
	}
}
