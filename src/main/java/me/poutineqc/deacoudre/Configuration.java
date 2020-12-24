package me.poutineqc.deacoudre;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class Configuration {

	private FileConfiguration config;
	private File configFile;

	public List<ItemStack> rewardItems = new ArrayList<ItemStack>();
	public List<String> dispatchCommands = new ArrayList<String>();

	public boolean introInFrontOfEveryMessage;
	public String language;

	public boolean verbose;
	
	public boolean autostart;
	public int countdownTime;
	public int timeBeforePlayerTimeOut;
	public boolean timeOutKick;
	public int maxFailBeforeEnding;
	public boolean resetPoolBeforeGame;
	public boolean invisibleFlyingSpectators;
	
	public boolean broadcastStart;
	public boolean broadcastAchievements;
	public boolean broadcastCongradulations;

	public boolean economyReward;
	public boolean challengeReward;

	public double minAmountReward;
	public double maxAmountReward;
	public double bonusCompletingArena;

	public double challengeRewardFinishArenaFirstTime;
	public double challengeReward8PlayersGame;
	public double challengeRewardReachRound100;

	public double hiddenChallengeReward;
	public String itemReward;
	public boolean chatRooms;
	public boolean teleportAfterEnding;
	
	public boolean mysql;
	public String host;
	public int port;
	public String database;
	public String user;
	public String password;
	public String tablePrefix;

	public Configuration(DeACoudre plugin) {

		configFile = new File(plugin.getDataFolder(), "config.yml");
		if (!configFile.exists())
			plugin.saveDefaultConfig();

		loadConfig(plugin);
	}

	public void loadConfig(Plugin plugin) {
		config = YamlConfiguration.loadConfiguration(configFile);

		introInFrontOfEveryMessage = config.getBoolean("introInFrontOfEveryMessage", true);

		language = config.getString("language", "en-US");
		verbose = config.getBoolean("verbose", true);
		
		autostart = config.getBoolean("autostart", true);
		countdownTime = config.getInt("countdownTime", 60);
		timeBeforePlayerTimeOut = config.getInt("timeBeforePlayerTimeOut", 60);
		timeOutKick = config.getBoolean("timeOutKick", true);
		maxFailBeforeEnding = config.getInt("maxFailBeforeEnding", 10);
		chatRooms = config.getBoolean("chatRooms", false);

		invisibleFlyingSpectators = config.getBoolean("invisibleFlyingSpectators", true);
		teleportAfterEnding = config.getBoolean("teleportAfterEnding", true);
		resetPoolBeforeGame = config.getBoolean("resetPoolBeforeGame", true);
		
		mysql = config.getBoolean("mysql", false);
		host = config.getString("host", "localhost");
		port = config.getInt("port", 3306);
		database = config.getString("database", "minecraft");
		user = config.getString("user", "root");
		password = config.getString("password");
		tablePrefix = config.getString("tablePrefix", "deacoudre_");
		
		broadcastStart = config.getBoolean("enabledBroadcasts.broadcastStart", true);
		broadcastAchievements = config.getBoolean("enabledBroadcasts.broadcastAchievements", true);
		broadcastCongradulations = config.getBoolean("enabledBroadcasts.broadcastCongradulations", true);
		
		economyReward = config.getBoolean("economyReward", true);
		challengeReward = config.getBoolean("challengeReward", true);

		minAmountReward = config.getDouble("minAmountReward", 25);
		maxAmountReward = config.getDouble("maxAmountReward", 150);
		bonusCompletingArena = config.getDouble("bonusCompletingArena", 50);

		challengeRewardFinishArenaFirstTime = config.getDouble("challengeRewardFinishArenaFirstTime", 100);
		challengeReward8PlayersGame = config.getDouble("challengeReward8PlayersGame", 50);
		challengeRewardReachRound100 = config.getDouble("challengeRewardReachRound100", 75);

		hiddenChallengeReward = config.getDouble("hiddenChallengeReward", 100);
		itemReward = config.getString("itemReward", "none");

		dispatchCommands = config.getStringList("commands");

		if (!itemReward.equalsIgnoreCase("random") && !itemReward.equalsIgnoreCase("all"))
			itemReward = "none";

		if (!itemReward.equalsIgnoreCase("none"))
			loadItemRewards(plugin);
	}

	@SuppressWarnings("deprecation")
	private Material getMaterial(String type) {
		Material material;
		try {
			int id = Integer.parseInt(type);
			material = Material.getMaterial(id);
		} catch (NumberFormatException e) {
			material = Material.getMaterial(type);
		}
		return material;
	}

	private void loadItemRewards(Plugin plugin) {
		rewardItems.clear();

		for (String items : config.getStringList("itemRewards")) {
			String[] item = items.split(":");

			Material material = getMaterial(item[0]);
			int amount = 1;
			int data = 0;
			String name = "-1";

			if (material == null) {
				plugin.getLogger().info("Error while trying to load the Item: " + items);
				plugin.getLogger().info("Item not found. Ignoring...");
				continue;
			}

			try {
				if (item.length > 1)
					amount = Integer.parseInt(item[1]);

				if (amount > 64) {
					plugin.getLogger().info("Error while trying to load the Item: " + items);
					plugin.getLogger().info("Too much items. Ignoring..");
					continue;
				}

				if (item.length > 2)
					data = Integer.parseInt(item[2]);
				if (item.length > 3)
					name = item[3];

			} catch (NumberFormatException e) {
				plugin.getLogger().info("Error while trying to load the Item: " + items);
				plugin.getLogger().info("Value not a number. Ignoring..");
				continue;
			}

			ItemStack tempReward = new ItemStack(material, amount, (short) data);

			if (name != "-1") {
				ItemMeta tempMeta = tempReward.getItemMeta();
				tempMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
				tempReward.setItemMeta(tempMeta);
			}

			rewardItems.add(tempReward);
		}
	}
}
