package me.poutineqc.deacoudre;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.poutineqc.deacoudre.achievements.Achievement;
import me.poutineqc.deacoudre.achievements.AchievementsGUI;
import me.poutineqc.deacoudre.achievements.TopManager;
import me.poutineqc.deacoudre.commands.DaC;
import me.poutineqc.deacoudre.commands.DacCommand;
import me.poutineqc.deacoudre.commands.DacSign;
import me.poutineqc.deacoudre.events.AsyncPlayerChat;
import me.poutineqc.deacoudre.events.BlockBreak;
import me.poutineqc.deacoudre.events.ElytraToggle;
import me.poutineqc.deacoudre.events.PlayerDamage;
import me.poutineqc.deacoudre.events.PlayerDisconnect;
import me.poutineqc.deacoudre.events.PlayerInteract;
import me.poutineqc.deacoudre.events.PlayerMove;
import me.poutineqc.deacoudre.events.PlayerTeleport;
import me.poutineqc.deacoudre.events.SignChange;
import me.poutineqc.deacoudre.guis.ChooseColorGUI;
import me.poutineqc.deacoudre.guis.ColorsGUI;
import me.poutineqc.deacoudre.guis.JoinGUI;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.User;

import net.milkbowl.vault.economy.Economy;

public class DeACoudre extends JavaPlugin {

	private Configuration config;
	private MySQL mysql;
	private Language mainLanguage;
	private PlayerData playerData;
	private ArenaData arenaData;
	private Achievement achievement;
	private ChooseColorGUI chooseColorGUI;

	private AchievementsGUI achievementsGUI;
	private PlayerDamage playerDamage;
	private JoinGUI joinGUI;
	private DaC dac;
	private DacSign signData;
	private Updater updater;

	private static Economy econ;
	public static String NMS_VERSION;
	public static boolean aboveOneNine;

	public void onEnable() {
		final PluginDescriptionFile pdfFile = getDescription();
		final Logger logger = getLogger();

		NMS_VERSION = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		aboveOneNine = NMS_VERSION.startsWith("v1_9") || NMS_VERSION.startsWith("v1_1") || NMS_VERSION.startsWith("v2");

		config = new Configuration(this);
		if (!initialiseEconomy())
			return;

		new User(this);
		new Permissions(this);
		new DacCommand(this);
		loadLanguages();
		connectMySQL();
		playerData = new PlayerData(this);
		achievement = new Achievement(this);
		new TopManager(this);
		achievementsGUI = new AchievementsGUI(this);
		chooseColorGUI = new ChooseColorGUI(this);
		joinGUI = new JoinGUI(this);
		playerDamage = new PlayerDamage(this);
		arenaData = new ArenaData(this);
		signData = new DacSign(this);
		new Arena(this);
		
		updater = new Updater(this);
		registerEvents();

		getCommand("dac").setExecutor(dac);

		try {
			Metrics metrics;
			metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info(pdfFile.getName() + " has been enabled (v" + pdfFile.getVersion() + ")");

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Arena.loadArenas();
				DacSign.loadAllSigns();
			}
		}, 0L);
	}

	private void connectMySQL() {
		if (config.mysql) {
			mysql = new MySQL(this);
			if (mysql.hasConnection())
				createMySQLTables();
		} else {
			mysql = new MySQL();
		}

	}

	private void createMySQLTables() {
		mysql.update("CREATE TABLE IF NOT EXISTS " + config.tablePrefix + "SIGNS ("
				+ "uuid varchar(64), type varchar(32),"
				+ "locationWorld varchar(32), locationX INT DEFAULT 0, locationY INT DEFAULT 0, locationZ INT DEFAULT 0);");
		mysql.update("ALTER TABLE " + config.tablePrefix + "SIGNS CONVERT TO CHARACTER SET utf8;");

		mysql.update("CREATE TABLE IF NOT EXISTS " + config.tablePrefix + "ARENAS (name varchar(32),world varchar(32),"
				+ "minAmountPlayer INT DEFAULT 2, maxAmountPlayer INT DEFAULT 8, colorIndice LONG,"
				+ "minPointX INT DEFAULT 0,minPointY INT DEFAULT 0,minPointZ INT DEFAULT 0,"
				+ "maxPointX INT DEFAULT 0, maxPointY INT DEFAULT 0,maxPointZ INT DEFAULT 0,"
				+ "lobbyX DOUBLE DEFAULT 0,lobbyY DOUBLE DEFAULT 0,lobbyZ DOUBLE DEFAULT 0,"
				+ "lobbyPitch FLOAT DEFAULT 0,lobbyYaw FLOAT DEFAULT 0,"
				+ "plateformX DOUBLE DEFAULT 0,plateformY DOUBLE DEFAULT 0,plateformZ DOUBLE DEFAULT 0,"
				+ "plateformPitch FLOAT DEFAULT 0,plateformYaw FLOAT DEFAULT 0);");
		mysql.update("ALTER TABLE " + config.tablePrefix + "ARENAS CONVERT TO CHARACTER SET utf8;");

		mysql.update("CREATE TABLE IF NOT EXISTS " + config.tablePrefix
				+ "PLAYERS (UUID varchar(64), name varchar(64), language varchar(32), timePlayed INT DEFAULT 0,"
				+ "money DOUBLE DEFAULT 0,"
				+ "gamesPlayed INT DEFAULT 0, gamesWon INT DEFAULT 0, gamesLost INT DEFAULT 0, DaCdone INT DEFAULT 0,"
				+ "completedArena BOOLEAN DEFAULT FALSE, 8playersGame BOOLEAN DEFAULT FALSE,"
				+ "reachRound100 BOOLEAN DEFAULT FALSE, DaCon42 BOOLEAN DEFAULT FALSE,"
				+ "colorRivalery BOOLEAN DEFAULT FALSE, longTime BOOLEAN DEFAULT FALSE);");
		mysql.update("ALTER TABLE " + config.tablePrefix + "PLAYERS CONVERT TO CHARACTER SET utf8;");

		mysql.update("CREATE OR REPLACE VIEW " + config.tablePrefix + "GAMESPLAYED AS SELECT name, gamesPlayed FROM "
				+ config.tablePrefix + "PLAYERS ORDER BY gamesPlayed DESC LIMIT 10");
		mysql.update("CREATE OR REPLACE VIEW " + config.tablePrefix + "GAMESWON AS SELECT name, gamesWon FROM "
				+ config.tablePrefix + "PLAYERS ORDER BY gamesWon DESC LIMIT 10");
		mysql.update("CREATE OR REPLACE VIEW " + config.tablePrefix + "GAMESLOST AS SELECT name, gamesLost FROM "
				+ config.tablePrefix + "PLAYERS ORDER BY gamesLost DESC LIMIT 10");
		mysql.update("CREATE OR REPLACE VIEW " + config.tablePrefix + "DACDONE AS SELECT name, DaCdone FROM "
				+ config.tablePrefix + "PLAYERS ORDER BY DaCdone DESC LIMIT 10");
	}

	public void loadLanguages() {
		Language.clearLanguages();

		new Language(this);
		new Language("en-US", false);
		new Language("fr-FR", false);
		new Language(config.language, false);
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		updater.stop();
		Logger logger = getLogger();

		logger.info(pdfFile.getName() + " has been disabled.");
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(playerData, this);
		pm.registerEvents(playerDamage, this);
		pm.registerEvents(new PlayerDisconnect(), this);
		pm.registerEvents(new PlayerMove(this), this);
		pm.registerEvents(new PlayerTeleport(this), this);
		pm.registerEvents(new SignChange(this, mainLanguage), this);
		pm.registerEvents(new AsyncPlayerChat(this), this);
		pm.registerEvents(chooseColorGUI, this);
		pm.registerEvents(achievementsGUI, this);
		pm.registerEvents(new ColorsGUI(this), this);
		pm.registerEvents(joinGUI, this);
		dac = new DaC(this);
		pm.registerEvents(new BlockBreak(), this);
		pm.registerEvents(new PlayerInteract(this, mainLanguage), this);
		pm.registerEvents(updater, this);

		if (aboveOneNine)
			pm.registerEvents(new ElytraToggle(), this);

	}

	public boolean initialiseEconomy() {
		if (config.economyReward)
			if (!setupEconomy()) {
				getLogger().warning("Vault not found.");
				getLogger().warning("Add Vault to your plugins or disable monetary rewards in the config.");
				getLogger().info("Disabling DeACoudre...");
				getServer().getPluginManager().disablePlugin(this);
				return false;
			}
		return true;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static boolean isEconomyEnabled() {
		return econ != null;
	}

	public static Economy getEconomy() {
		return econ;
	}

	public Configuration getConfiguration() {
		return config;
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	public ArenaData getArenaData() {
		return arenaData;
	}

	public Achievement getAchievement() {
		return achievement;
	}

	public MySQL getMySQL() {
		return mysql;
	}

	public ChooseColorGUI getChooseColorGUI() {
		return chooseColorGUI;
	}

	public AchievementsGUI getAchievementsGUI() {
		return achievementsGUI;
	}

	public PlayerDamage getPlayerDamage() {
		return playerDamage;
	}

	public JoinGUI getJoinGUI() {
		return joinGUI;
	}

	public DaC getDAC() {
		return dac;
	}

	public DacSign getSignData() {
		return signData;
	}
}
