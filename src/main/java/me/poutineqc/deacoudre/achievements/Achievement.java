package me.poutineqc.deacoudre.achievements;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.MySQL;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.User;

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

	private DeACoudre plugin;

	private FileConfiguration achievementData;
	private File achievementDataFile;

	private ArrayList<ArrayList<AchievementsObject>> achievements = new ArrayList<ArrayList<AchievementsObject>>();
	private Configuration config;
	private PlayerData playerData;
	private MySQL mysql;

	public Achievement(DeACoudre plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfiguration();
		this.mysql = plugin.getMySQL();
		this.playerData = plugin.getPlayerData();

		achievementDataFile = new File(plugin.getDataFolder(), "achievements.yml");
		if (!achievementDataFile.exists()) {
			InputStream local = plugin.getResource("achievements.yml");
			if (local != null)
				plugin.saveResource("achievements.yml", false);
			else
				plugin.getLogger().info("Could not find achievements.yml - Using default (en-US)");
		}

		load_achievements();
	}

	public void load_achievements() {
		achievementData = YamlConfiguration.loadConfiguration(achievementDataFile);
		achievements.clear();

		String[] configNames = new String[] { "amountOfGamesPlayed", "amountOfGamesWon", "amountOfGamesLost",
				"amountOfDaCsDone" };
		for (int i = 0; i < 4; i++) {
			achievements.add(new ArrayList<AchievementsObject>());

			List<String> readData = achievementData.getStringList(configNames[i]);

			for (int j = 0; j < readData.size(); j++) {
				String[] individualData = readData.get(j).split(";");

				if (individualData.length < 2) {
					plugin.getLogger()
							.info("Could not load the " + j + "'th data from the " + i + "'th achievement type.1");
					continue;
				}

				try {
					int level = Integer.parseInt(individualData[0]);
					double reward = Double.parseDouble(individualData[1]);
					achievements.get(i).add(new AchievementsObject(level, reward));
				} catch (NumberFormatException e) {
					plugin.getLogger()
							.info("Could not load the " + j + "'th data from the " + i + "'th achievement type.2");
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
		External: switch (toCheck) {
		case gamesPlayed:
		case gamesWon:
		case gamesLost:
		case dacDone:
			int listNo = 0;
			switch (toCheck) {
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

			for (AchievementsObject ao : achievement.get_achievements().get(listNo)) {

				int level = 0;
				if (mysql.hasConnection()) {
					ResultSet query = mysql.query("SELECT " + toCheck.substring(1) + " FROM " + config.tablePrefix
							+ "PLAYERS WHERE UUID='" + player.getUniqueId().toString() + "';");
					try {
						if (query.next()) {
							level = query.getInt(toCheck.substring(1));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					level = playerData.getData().getInt("players." + player.getUniqueId().toString() + toCheck, 0);
				}

				if (level != ao.get_level())
					continue;

				sendCongratulationMessage(player, toCheck, listNo, ao);
				if (DeACoudre.isEconomyEnabled()) {
					rewardAmount = ao.get_reward();
				}

				break External;
			}
			break;
		default:
			boolean completed = true;
			if (mysql.hasConnection()) {
				ResultSet query = mysql.query("SELECT " + toCheck.substring(12) + " FROM " + config.tablePrefix
						+ "PLAYERS WHERE UUID='" + player.getUniqueId().toString() + "';");
				try {
					if (query.next()) {
						completed = query.getBoolean(toCheck.substring(12));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				completed = playerData.getData().getBoolean("players." + player.getUniqueId().toString() + toCheck,
						false);
			}

			if (!completed) {

				if (mysql.hasConnection()) {
					mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET " + toCheck.substring(12)
							+ "='1' WHERE UUID='" + player.getUniqueId().toString() + "';");
				} else {
					playerData.getData().set("players." + player.getUniqueId().toString() + toCheck, true);
					playerData.savePlayerData();
				}

				sendCongratulationMessage(plugin, player, toCheck);

				if (DeACoudre.isEconomyEnabled()) {
					switch (toCheck) {
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

		if (rewardAmount != 0 && config.challengeReward && DeACoudre.isEconomyEnabled()) {
			DeACoudre.getEconomy().depositPlayer(player, rewardAmount);
			Language local = playerData.getLanguageOfPlayer(player);
			local.sendMsg(player,
					local.challengeRewardMoney.replace("%currency%", DeACoudre.getEconomy().currencyNamePlural())
							.replace("%amount%", String.valueOf(rewardAmount)));

			double previousAmount = 0;
			if (mysql.hasConnection()) {
				ResultSet query = mysql.query("SELECT money FROM " + config.tablePrefix + "PLAYERS WHERE UUID='"
						+ player.getUniqueId().toString() + "';");
				try {
					if (query.next())
						previousAmount = query.getDouble("money");
				} catch (SQLException e) {
					e.printStackTrace();
				}

				mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET money='" + (previousAmount + rewardAmount)
						+ "' WHERE UUID='" + player.getUniqueId().toString() + "';");
			} else {
				previousAmount = playerData.getData()
						.getDouble("players." + player.getUniqueId().toString() + ".stats.moneyGains", 0);
				playerData.getData().set("players." + player.getUniqueId().toString() + ".stats.moneyGains",
						(previousAmount + rewardAmount));
				playerData.savePlayerData();

			}
		}
	}

	private void sendCongratulationMessage(Player player, String toCheck, int listNo, AchievementsObject ao) {
		if (plugin.getConfiguration().broadcastAchievements)
			for (Player p : getBroadcastList(player)) {
				Language local = playerData.getLanguageOfPlayer(p);

				switch (toCheck) {
				case gamesPlayed:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName()).replace(
							"%challenge%", local.challengeDisplayPlayed.replace("%amount%", String.valueOf(ao.get_level()))));
					break;
				case gamesWon:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName()).replace(
							"%challenge%", local.challengeDisplayWin.replace("%amount%", String.valueOf(ao.get_level()))));
					break;
				case gamesLost:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName()).replace(
							"%challenge%", local.challengeDisplayLost.replace("%amount%", String.valueOf(ao.get_level()))));
					break;
				case dacDone:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName()).replace(
							"%challenge%", local.challengeDisplayDaC.replace("%amount%", String.valueOf(ao.get_level()))));
					break;

				}
			}
	}
	
	private List<Player> getBroadcastList(Player player) {
		List<Player> players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		if (!config.broadcastCongradulations) {
			players = new ArrayList<Player>();
			players.add(player);
		}
		return players;
		
	}

	private void sendCongratulationMessage(DeACoudre plugin, Player player, String toCheck) {

		if (plugin.getConfiguration().broadcastAchievements)
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);

				switch (toCheck) {
				case completedArena:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayCompleteArena).toString());
					break;
				case eightPlayersGame:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplay8PlayersGame).toString());
					break;
				case reachRoundHundred:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayReachRound100).toString());
					break;
				case dacOnFortyTwo:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayAnswerToLife).toString());
					break;
				case colorRivalery:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayFight).toString());
					break;
				case longTime:
					local.sendMsg(p, local.challengeBroadcast.replace("%player%", player.getDisplayName())
							.replace("%challenge%", local.challengeDisplayMinecraftSnail).toString());
					break;
				default:
					break;
				}

			}
	}

}
