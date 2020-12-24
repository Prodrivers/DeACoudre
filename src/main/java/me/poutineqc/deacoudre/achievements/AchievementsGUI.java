package me.poutineqc.deacoudre.achievements;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.MySQL;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.tools.ItemStackManager;

public class AchievementsGUI implements Listener {

	private Configuration config;
	private PlayerData playerData;
	private Achievement achievement;
	private MySQL mysql;

	public AchievementsGUI(DeACoudre plugin) {
		this.config = plugin.getConfiguration();
		this.mysql = plugin.getMySQL();
		this.playerData = plugin.getPlayerData();
		this.achievement = plugin.getAchievement();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inv = event.getInventory();
		InventoryView view = event.getView();
		Player player = (Player) event.getWhoClicked();
		Language local = playerData.getLanguageOfPlayer(player);

		if (ChatColor.stripColor(view.getTitle())
				.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStats)))
				|| ChatColor.stripColor(view.getTitle()).equalsIgnoreCase(
						ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordChallenges)))) {
			if (event.getAction().equals(InventoryAction.NOTHING) || event.getAction().equals(InventoryAction.UNKNOWN))
				return;

			event.setCancelled(true);

			ItemStack item = event.getCurrentItem();
			if (item.getType() != Material.ARROW)
				return;

			String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

			if (ChatColor.stripColor(itemName).equalsIgnoreCase(
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordChallenges)))) {
				openChallenges(player);
				return;
			}

			if (ChatColor.stripColor(itemName).equalsIgnoreCase(
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStats)))) {
				openStats(player);
				return;
			}
		}

	}

	public void openStats(Player player) {

		Language local = playerData.getLanguageOfPlayer(player);
		String UUID = player.getUniqueId().toString();

		ItemStackManager icon;
		Inventory inv = Bukkit.createInventory(null, 54,
				ChatColor.translateAlternateColorCodes('&', local.keyWordStats));

		int gamesPlayed = 0;
		int gamesWon = 0;
		int gamesLost = 0;
		int DaCdone = 0;
		int timePlayed = 0;
		double moneyGains = 0;

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT gamesPlayed, gamesWon, gamesLost, DaCdone, timePlayed, money FROM "
					+ config.tablePrefix + "PLAYERS WHERE UUID='" + UUID + "';");
			try {
				if (query.next()) {
					gamesPlayed = query.getInt("gamesPlayed");
					gamesWon = query.getInt("gamesWon");
					gamesLost = query.getInt("gamesLost");
					DaCdone = query.getInt("DaCdone");
					timePlayed = query.getInt("timePlayed");
					moneyGains = query.getDouble("money");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			gamesPlayed = playerData.getData().getInt("players." + UUID + ".gamesPlayed", 0);
			gamesWon = playerData.getData().getInt("players." + UUID + ".gamesWon", 0);
			gamesLost = playerData.getData().getInt("players." + UUID + ".gamesPlayed", 0)
					- playerData.getData().getInt("players." + UUID + ".gamesWon", 0);
			DaCdone = playerData.getData().getInt("players." + UUID + ".DaCdone", 0);
			timePlayed = playerData.getData().getInt("players." + UUID + ".stats.timePlayed", 0);
			moneyGains = playerData.getData().getInt("players." + UUID + ".stats.moneyGains", 0);
		}

		moneyGains = Math.floor(moneyGains * 100) / 100;

		/***************************************************
		 * Stats
		 ***************************************************/

		icon = new ItemStackManager(Material.PAPER, 4);

		icon.setTitle(ChatColor.UNDERLINE + "" + ChatColor.GOLD
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStats + " : DaC")));

		icon.addToLore("&e---------------------------");
		icon.addToLore(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesPlayed)) + ": "
				+ ChatColor.YELLOW + gamesPlayed);
		icon.addToLore(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesWon)) + ": "
				+ ChatColor.YELLOW + gamesWon);
		icon.addToLore(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesLost)) + ": "
				+ ChatColor.YELLOW + gamesLost);
		icon.addToLore(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsDacsDone)) + ": "
				+ ChatColor.YELLOW + DaCdone);
		icon.addToLore(ChatColor.YELLOW + "---------------------------");
		icon.addToLore(ChatColor.LIGHT_PURPLE
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsTimePlayed)) + ": "
				+ getTimePLayed(local, timePlayed));

		if (config.economyReward)
			icon.addToLore(ChatColor.LIGHT_PURPLE
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsMoneyGot)) + ": "
					+ ChatColor.YELLOW + DeACoudre.getEconomy().currencyNamePlural() + moneyGains);

		inv = icon.addToInventory(inv);

		/***************************************************
		 * Arrow to Challenges
		 ***************************************************/

		icon = new ItemStackManager(Material.ARROW, 8);

		icon.setTitle(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordChallenges)));

		inv = icon.addToInventory(inv);

		/***************************************************
		 * Top Amount of Game
		 ***************************************************/
		icon = new ItemStackManager(Material.MAP, 18);
		icon.setTitle(ChatColor.GOLD
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsTop10)) + " : "
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesPlayed)));
		icon.addToLore(ChatColor.YELLOW + "---------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "GAMESPLAYED;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ query.getInt("gamesPlayed"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getGames().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getGames().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + TopManager.getGames().get(i).getScore());
			}
		}

		inv = icon.addToInventory(inv);

		/***************************************************
		 * Top Amount of Win
		 ***************************************************/
		icon = new ItemStackManager(Material.MAP, 27);
		icon.setTitle(ChatColor.GOLD
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsTop10)) + " : "
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesWon)));
		icon.addToLore(ChatColor.YELLOW + "---------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "GAMESWON;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ query.getInt("gamesWon"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getWon().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getWon().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + TopManager.getWon().get(i).getScore());
			}
		}

		inv = icon.addToInventory(inv);

		/***************************************************
		 * Top Amount of Losses
		 ***************************************************/
		icon = new ItemStackManager(Material.MAP, 36);
		icon.setTitle(ChatColor.GOLD
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsTop10)) + " : "
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesLost)));
		icon.addToLore(ChatColor.YELLOW + "---------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "GAMESLOST;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ query.getInt("gamesLost"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getLost().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getLost().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + TopManager.getLost().get(i).getScore());
			}
		}

		inv = icon.addToInventory(inv);

		/***************************************************
		 * Top Amount of DaC
		 ***************************************************/
		icon = new ItemStackManager(Material.MAP, 45);
		icon.setTitle(ChatColor.GOLD
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsTop10)) + " : "
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsDacsDone)));
		icon.addToLore(ChatColor.YELLOW + "---------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "DACDONE;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ query.getInt("DaCdone"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getDaCdone().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getDaCdone().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + TopManager.getDaCdone().get(i).getScore());
			}
		}

		icon.addToInventory(inv);

		/***************************************************
		 * Glass Separator
		 ***************************************************/

		icon = new ItemStackManager(Material.BLUE_STAINED_GLASS_PANE);
		icon.setTitle(" ");

		for (int i = 0; i < inv.getSize(); i++) {
			switch (i) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 19:
			case 28:
			case 37:
			case 46:
				icon.setPosition(i);
				inv = icon.addToInventory(inv);
				break;
			}
		}

		/***************************************************
		 * Challenges
		 ***************************************************/

		String[] challengeNames = new String[] { local.challengeDisplayPlayed, local.challengeDisplayWin,
				local.challengeDisplayLost, local.challengeDisplayDaC };
		String[] challengePath = new String[] { Achievement.gamesPlayed, Achievement.gamesWon, Achievement.gamesLost,
				Achievement.dacDone };
		ArrayList<ArrayList<AchievementsObject>> achievements = achievement.get_achievements();

		for (int i = 0; i < 4; i++) {
			int position = (i * 9) + 20;

			for (AchievementsObject ao : achievements.get(i)) {
				int amount = 0;
				if (mysql.hasConnection()) {
					ResultSet query = mysql.query("SELECT " + challengePath[i].substring(1) + " FROM "
							+ config.tablePrefix + "PLAYERS WHERE UUID='" + UUID + "';");
					try {
						if (query.next()) {
							amount = query.getInt(challengePath[i].substring(1));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					amount = playerData.getData().getInt("players." + UUID + challengePath[i], 0);
				}

				if (amount >= ao.get_level()) {
					icon = new ItemStackManager(Material.PURPLE_WOOL, position++);
					icon.setTitle((ChatColor.GREEN + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
							challengeNames[i].replace("%amount%", String.valueOf(ao.get_level()))))));
					icon.addToLore(ChatColor.YELLOW + "---------------------------");
					icon.addToLore(
							ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', local.keyWordStatsProgression)
									+ ": " + ChatColor.GREEN + local.keyWordStatsCompleted);
				} else {
					icon = new ItemStackManager(Material.GRAY_WOOL, position++);
					icon.setTitle((ChatColor.RED + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
							challengeNames[i].replace("%amount%", String.valueOf(ao.get_level()))))));
					icon.addToLore(ChatColor.YELLOW + "---------------------------");
					icon.addToLore(
							ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', local.keyWordStatsProgression)
									+ ": " + ChatColor.YELLOW + String.valueOf(amount) + "/" + ao.get_level());
				}

				if (config.challengeReward && config.economyReward) {
					icon.addToLore(ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', local.keyWordStatsReward)
							+ ": " + ChatColor.YELLOW + DeACoudre.getEconomy().currencyNamePlural() + ao.get_reward());
				}

				icon.addToInventory(inv);

			}
		}

		player.openInventory(inv);

	}

	public void openChallenges(Player player) {

		Language local = playerData.getLanguageOfPlayer(player);
		String UUID = player.getUniqueId().toString();

		ItemStackManager icon;
		Inventory inv = Bukkit.createInventory(null, 27,
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordChallenges)));

		int gamesPlayed = 0;
		int gamesWon = 0;
		int gamesLost = 0;
		int DaCdone = 0;
		int timePlayed = 0;
		double moneyGains = 0;

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT gamesPlayed, gamesWon, gamesLost, DaCdone, timePlayed, money FROM "
					+ config.tablePrefix + "PLAYERS WHERE UUID='" + UUID + "';");
			try {
				if (query.next()) {
					gamesPlayed = query.getInt("gamesPlayed");
					gamesWon = query.getInt("gamesWon");
					gamesLost = query.getInt("gamesLost");
					DaCdone = query.getInt("DaCdone");
					timePlayed = query.getInt("timePlayed");
					moneyGains = query.getDouble("money");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			gamesPlayed = playerData.getData().getInt("players." + UUID + ".gamesPlayed", 0);
			gamesWon = playerData.getData().getInt("players." + UUID + ".gamesWon", 0);
			gamesLost = playerData.getData().getInt("players." + UUID + ".gamesPlayed", 0)
					- playerData.getData().getInt("players." + UUID + ".gamesWon", 0);
			DaCdone = playerData.getData().getInt("players." + UUID + ".DaCdone", 0);
			timePlayed = playerData.getData().getInt("players." + UUID + ".stats.timePlayed", 0);
			moneyGains = playerData.getData().getInt("players." + UUID + ".stats.moneyGains", 0);
		}

		moneyGains = Math.floor(moneyGains * 100) / 100;

		/***************************************************
		 * Stats
		 ***************************************************/

		icon = new ItemStackManager(Material.PAPER, 4);

		icon.setTitle(ChatColor.UNDERLINE + "" + ChatColor.GOLD
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStats + " : DaC")));

		icon.addToLore("&e---------------------------");
		icon.addToLore(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesPlayed)) + ": "
				+ ChatColor.YELLOW + gamesPlayed);
		icon.addToLore(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesWon)) + ": "
				+ ChatColor.YELLOW + gamesWon);
		icon.addToLore(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsGamesLost)) + ": "
				+ ChatColor.YELLOW + gamesLost);
		icon.addToLore(ChatColor.AQUA
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsDacsDone)) + ": "
				+ ChatColor.YELLOW + DaCdone);
		icon.addToLore(ChatColor.YELLOW + "---------------------------");
		icon.addToLore(ChatColor.LIGHT_PURPLE
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsTimePlayed)) + ": "
				+ getTimePLayed(local, timePlayed));

		if (config.economyReward)
			icon.addToLore(ChatColor.LIGHT_PURPLE
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsMoneyGot)) + ": "
					+ ChatColor.YELLOW + DeACoudre.getEconomy().currencyNamePlural() + moneyGains);

		inv = icon.addToInventory(inv);

		/***************************************************
		 * Glass Separator
		 ***************************************************/

		icon = new ItemStackManager(Material.BLUE_STAINED_GLASS_PANE);
		icon.setTitle(" ");

		for (int i = 0; i < inv.getSize(); i++) {
			switch (i) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
				icon.setPosition(i);
				inv = icon.addToInventory(inv);
				break;
			}
		}

		/***************************************************
		 * Challenge Completed Arena
		 ***************************************************/

		String[] challengeNames = new String[] { local.challengeDisplayCompleteArena,
				local.challengeDisplay8PlayersGame, local.challengeDisplayReachRound100, local.challengeDisplayFight,
				local.challengeDisplayAnswerToLife, local.challengeDisplayMinecraftSnail };
		String[] challengePath = new String[] { Achievement.completedArena, Achievement.eightPlayersGame,
				Achievement.reachRoundHundred, Achievement.colorRivalery, Achievement.dacOnFortyTwo,
				Achievement.longTime };
		double[] challengeReward = new double[] { config.challengeRewardFinishArenaFirstTime,
				config.challengeReward8PlayersGame, config.challengeRewardReachRound100, config.hiddenChallengeReward,
				config.hiddenChallengeReward, config.hiddenChallengeReward };

		int position = 19;

		for (int i = 0; i < 6; i++) {

			boolean completed = false;
			if (mysql.hasConnection()) {
				ResultSet query = mysql.query("SELECT " + challengePath[i].substring(12) + " FROM " + config.tablePrefix
						+ "PLAYERS WHERE UUID='" + UUID + "';");
				try {
					if (query.next())
						completed = query.getBoolean(challengePath[i].substring(12));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				completed = playerData.getData().getBoolean("players." + UUID + challengePath[i]);
			}

			if (completed) {
				icon = new ItemStackManager(Material.GREEN_DYE, position++);
				icon.setTitle(ChatColor.GREEN
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', challengeNames[i])));

				icon.addToLore(ChatColor.YELLOW + "---------------------------");
				icon.addToLore(ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', local.keyWordStatsProgression)
						+ ": " + ChatColor.GREEN
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsCompleted)));
			} else {
				icon = new ItemStackManager(Material.GRAY_DYE, position++);
				icon.setTitle(ChatColor.RED
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', challengeNames[i])));

				icon.addToLore(ChatColor.YELLOW + "---------------------------");

				icon.addToLore(ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', local.keyWordStatsProgression)
						+ ": " + ChatColor.RED
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStatsNotCompleted)));
			}

			if (config.challengeReward && config.economyReward)
				icon.addToLore(ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', local.keyWordStatsReward) + ": "
						+ ChatColor.YELLOW + DeACoudre.getEconomy().currencyNamePlural() + challengeReward[i]);

			inv = icon.addToInventory(inv);

			if (position == 22)
				position++;
		}

		/***************************************************
		 * Arrow to Challenges
		 ***************************************************/

		icon = new ItemStackManager(Material.ARROW, 8);

		icon.setTitle(
				ChatColor.AQUA + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordStats)));

		inv = icon.addToInventory(inv);

		player.openInventory(inv);
	}

	private String getTimePLayed(Language local, int timePlayed) {
		timePlayed = (int) Math.floor(timePlayed / 60000.0);
		int minutes = timePlayed % 60;
		timePlayed = (int) Math.floor(timePlayed / 60.0);

		return ChatColor.YELLOW + String.valueOf(timePlayed) + ChatColor.GREEN + " "
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordGeneralHours))
				+ ChatColor.YELLOW + " " + String.valueOf(minutes) + ChatColor.GREEN + " "
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordGeneralMinutes));
	}
}
