package me.poutineqc.deacoudre.achievements;

import me.poutineqc.deacoudre.*;
import me.poutineqc.deacoudre.instances.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Achievement {
	final public static String gamesPlayed = ".gamesPlayed";
	final public static String gamesWon = ".gamesWon";
	final public static String gamesLost = ".gamesLost";
	final public static String dacDone = ".DaCdone";

	final public static String completedArena = ".challenges.completedArena";
	final public static String eightPlayersGame = ".challenges.8playersGame";
	final public static String reachRoundHundred = ".challenges.reachRound100";
	final public static String dacOnFortyTwo = ".challenges.DaCon42";
	final public static String colorRivalery = ".challenges.colorRivalery";
	final public static String longTime = ".challenges.longTime";

	private final DeACoudre plugin;
	private final File achievementDataFile;
	private final ArrayList<ArrayList<AchievementsObject>> achievements = new ArrayList<>();
	private final Configuration config;
	private final PlayerData playerData;
	private final MySQL mysql;
	private FileConfiguration achievementData;

	public Achievement(DeACoudre plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfiguration();
		this.mysql = plugin.getMySQL();
		this.playerData = plugin.getPlayerData();

		achievementDataFile = new File(plugin.getDataFolder(), "achievements.yml");
		if(!achievementDataFile.exists()) {
			InputStream local = plugin.getResource("achievements.yml");
			if(local != null) {
				plugin.saveResource("achievements.yml", false);
			} else {
				Log.info("Could not find achievements.yml - Using default (en-US)");
			}
		}

		load_achievements();
	}

	public void load_achievements() {
		achievementData = YamlConfiguration.loadConfiguration(achievementDataFile);
		achievements.clear();

		String[] configNames = new String[]{ "amountOfGamesPlayed", "amountOfGamesWon", "amountOfGamesLost",
				"amountOfDaCsDone" };
		for(int i = 0; i < 4; i++) {
			achievements.add(new ArrayList<>());

			List<String> readData = achievementData.getStringList(configNames[i]);

			for(int j = 0; j < readData.size(); j++) {
				String[] individualData = readData.get(j).split(";");

				if(individualData.length < 2) {
					Log.info("Could not load the " + j + "'th data from the " + i + "'th achievement type.1");
					continue;
				}

				try {
					int level = Integer.parseInt(individualData[0]);
					double reward = Double.parseDouble(individualData[1]);
					achievements.get(i).add(new AchievementsObject(level, reward));
				} catch(NumberFormatException e) {
					Log.info("Could not load the " + j + "'th data from the " + i + "'th achievement type.2");
					continue;
				}
			}
		}

	}

	public ArrayList<ArrayList<AchievementsObject>> get_achievements() {
		return achievements;
	}

	public void testAchievement(String toCheck, User user) {
		testAchievement(toCheck, user.getPlayer());
	}

	public void testAchievement(String toCheck, Player player) {
		Achievement achievement = plugin.getAchievement();

		double rewardAmount = 0;
		External:
		switch(toCheck) {
			case gamesPlayed, gamesWon, gamesLost, dacDone -> {
				int listNo = 0;
				switch(toCheck) {
					case gamesPlayed:
						listNo = 0;
						break;
					case gamesWon:
						listNo = 1;
						break;
					case gamesLost:
						listNo = 2;
						break;
					case dacDone:
						listNo = 3;
				}
				for(AchievementsObject ao : achievement.get_achievements().get(listNo)) {

					int level = 0;
					if(mysql.hasConnection()) {
						ResultSet query = mysql.query("SELECT " + toCheck.substring(1) + " FROM " + config.tablePrefix
								+ "PLAYERS WHERE UUID='" + player.getUniqueId() + "';");
						try {
							if(query.next()) {
								level = query.getInt(toCheck.substring(1));
							}
						} catch(SQLException e) {
							Log.severe("Error on player achievement " + toCheck.substring(1) + " retrieval.", e);
						}
					} else {
						level = playerData.getData().getInt("players." + player.getUniqueId() + toCheck, 0);
					}

					if(level != ao.level()) {
						continue;
					}

					sendCongratulationMessage(player, toCheck, listNo, ao);
					if(DeACoudre.isEconomyEnabled()) {
						rewardAmount = ao.reward();
					}

					break External;
				}
			}
			default -> {
				boolean completed = true;
				if(mysql.hasConnection()) {
					ResultSet query = mysql.query("SELECT " + toCheck.substring(12) + " FROM " + config.tablePrefix
							+ "PLAYERS WHERE UUID='" + player.getUniqueId() + "';");
					try {
						if(query.next()) {
							completed = query.getBoolean(toCheck.substring(12));
						}
					} catch(SQLException e) {
						Log.severe("Error on player achievement " + toCheck.substring(12) + " retrieval.", e);
					}
				} else {
					completed = playerData.getData().getBoolean("players." + player.getUniqueId() + toCheck,
							false);
				}
				if(!completed) {

					if(mysql.hasConnection()) {
						mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET " + toCheck.substring(12)
								+ "='1' WHERE UUID='" + player.getUniqueId() + "';");
					} else {
						playerData.getData().set("players." + player.getUniqueId() + toCheck, true);
						playerData.savePlayerData();
					}

					sendCongratulationMessage(plugin, player, toCheck);

					if(DeACoudre.isEconomyEnabled()) {
						switch(toCheck) {
							case completedArena:
								rewardAmount = config.challengeRewardFinishArenaFirstTime;
								break;
							case eightPlayersGame:
								rewardAmount = config.challengeReward8PlayersGame;
								break;
							case reachRoundHundred:
								rewardAmount = config.challengeRewardReachRound100;
								break;
							case dacOnFortyTwo:
							case colorRivalery:
							case longTime:
								rewardAmount = config.hiddenChallengeReward;
								break;
							default:
								rewardAmount = 0;
						}

					}
				}
			}
		}

		if(rewardAmount != 0 && config.challengeReward && DeACoudre.isEconomyEnabled()) {
			DeACoudre.getEconomy().depositPlayer(player, rewardAmount);
			Language local = playerData.getLanguageOfPlayer(player);
			local.sendMsg(player,
					local.challengeRewardMoney.replace("%currency%", DeACoudre.getEconomy().currencyNamePlural())
							.replace("%amount%", String.valueOf(rewardAmount)));

			double previousAmount = 0;
			if(mysql.hasConnection()) {
				ResultSet query = mysql.query("SELECT money FROM " + config.tablePrefix + "PLAYERS WHERE UUID='"
						+ player.getUniqueId() + "';");
				try {
					if(query.next()) {
						previousAmount = query.getDouble("money");
					}
				} catch(SQLException e) {
					Log.severe("Error on player's money retrieval.", e);
				}

				mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET money='" + (previousAmount + rewardAmount)
						+ "' WHERE UUID='" + player.getUniqueId() + "';");
			} else {
				previousAmount = playerData.getData()
						.getDouble("players." + player.getUniqueId() + ".stats.moneyGains", 0);
				playerData.getData().set("players." + player.getUniqueId() + ".stats.moneyGains",
						(previousAmount + rewardAmount));
				playerData.savePlayerData();

			}
		}
	}

	private void sendCongratulationMessage(Player player, String toCheck, int listNo, AchievementsObject ao) {
		if(plugin.getConfiguration().broadcastAchievements) {
			for(Player p : getBroadcastList(player)) {
				Language local = playerData.getLanguageOfPlayer(p);

				switch(toCheck) {
					case gamesPlayed -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName()).replace(
							"%challenge%", local.challengeDisplayPlayed.replace("%amount%", String.valueOf(ao.level()))));
					case gamesWon -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName()).replace(
							"%challenge%", local.challengeDisplayWin.replace("%amount%", String.valueOf(ao.level()))));
					case gamesLost -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName()).replace(
							"%challenge%", local.challengeDisplayLost.replace("%amount%", String.valueOf(ao.level()))));
					case dacDone -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName()).replace(
							"%challenge%", local.challengeDisplayDaC.replace("%amount%", String.valueOf(ao.level()))));
				}
			}
		}
	}

	private List<Player> getBroadcastList(Player player) {
		List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
		if(!config.broadcastCongradulations) {
			players = new ArrayList<>();
			players.add(player);
		}
		return players;

	}

	private void sendCongratulationMessage(DeACoudre plugin, Player player, String toCheck) {

		if(plugin.getConfiguration().broadcastAchievements) {
			for(Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);

				switch(toCheck) {
					case completedArena -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayCompleteArena).toString());
					case eightPlayersGame -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplay8PlayersGame).toString());
					case reachRoundHundred -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayReachRound100).toString());
					case dacOnFortyTwo -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayAnswerToLife).toString());
					case colorRivalery -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayFight).toString());
					case longTime -> local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayMinecraftSnail).toString());
					default -> {
					}
				}

			}
		}
	}

}
