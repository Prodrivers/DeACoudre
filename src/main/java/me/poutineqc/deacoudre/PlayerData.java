package me.poutineqc.deacoudre;

import me.poutineqc.deacoudre.instances.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

@Singleton
public class PlayerData implements Listener {
	private final HashMap<String, String> originalPlayerName = new HashMap<>();
	private final File playerFile;
	private final MySQL mysql;
	private final Configuration config;
	private FileConfiguration playerData;

	@Inject
	public PlayerData(final DeACoudre plugin, final MySQL mySQL) {
		this.config = plugin.getConfiguration();
		this.mysql = mySQL;

		playerFile = new File(plugin.getDataFolder(), "playerData.yml");
		if(!playerFile.exists()) {
			try {
				playerFile.createNewFile();
			} catch(IOException e) {
				Log.severe(ChatColor.RED + "Could not create playerData.ylm.");
			}
		}

		loadPlayerData();
	}

	public FileConfiguration getData() {
		return playerData;
	}

	public void savePlayerData() {
		try {
			playerData.save(playerFile);
		} catch(IOException e) {
			Log.severe(ChatColor.RED + "Could not save arenaData.yml!");
		}
	}

	public void loadPlayerData() {
		playerData = YamlConfiguration.loadConfiguration(playerFile);

		generateOriginalPlayerNames();

		for(Player player : Bukkit.getOnlinePlayers()) {
			addOnFileIfNotExist(player);
		}
	}

	private void generateOriginalPlayerNames() {
		originalPlayerName.clear();

		if(mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT UUID, name FROM " + config.tablePrefix + "PLAYERS");
			try {
				while(query.next()) {
					originalPlayerName.put(query.getString("UUID"), query.getString("name"));
				}
			} catch(SQLException e) {
				Log.severe("Could not get player names from database.", e);
			}
		} else {
			if(playerData.contains("players")) {
				for(String uuid : playerData.getConfigurationSection("players").getKeys(false)) {
					originalPlayerName.put(uuid,
							playerData.getString("players." + uuid + ".name", UUID.randomUUID().toString()));
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		addOnFileIfNotExist(player);
	}

	public void addOnFileIfNotExist(Player player) {
		String uuid = player.getUniqueId().toString();

		if(originalPlayerName.containsKey(uuid)) {
			if(player.getName().equalsIgnoreCase(originalPlayerName.get(uuid))) {
				return;
			}

			if(mysql.hasConnection()) {
				mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET name='" + player.getName() + "' WHERE UUID='"
						+ uuid + "';");
			} else {
				playerData.set("players." + uuid + ".name", player.getName());
				savePlayerData();
			}
		} else {
			originalPlayerName.put(uuid, player.getName());

			if(mysql.hasConnection()) {
				mysql.update("INSERT INTO " + config.tablePrefix + "PLAYERS (UUID, name) VALUES ('" + uuid + "','"
						+ player.getName() + "');");
			} else {
				playerData.set("players." + uuid + ".name", player.getName());
				playerData.set("players." + uuid + ".language", "default");
				playerData.set("players." + uuid + ".gamesPlayed", 0);
				playerData.set("players." + uuid + ".gamesWon", 0);
				playerData.set("players." + uuid + ".gamesLost", 0);
				playerData.set("players." + uuid + ".DaCdone", 0);
				playerData.set("players." + uuid + ".stats.timePlayed", 0);
				playerData.set("players." + uuid + ".stats.moneyGains", 0);
				playerData.set("players." + uuid + ".challenges.completedArena", false);
				playerData.set("players." + uuid + ".challenges.8playersGame", false);
				playerData.set("players." + uuid + ".challenges.reachRound100", false);
				playerData.set("players." + uuid + ".challenges.DaCon42", false);
				playerData.set("players." + uuid + ".challenges.colorRivalery", false);
				playerData.set("players." + uuid + ".challenges.longTime", false);
				savePlayerData();
			}
		}
	}

	public Language getLanguageOfPlayer(User user) {
		return getLanguageOfPlayer(user.getPlayer());
	}

	public Language getLanguageOfPlayer(Player player) {
		String fileName = null;
		if(mysql.hasConnection()) {
			try {
				ResultSet query = mysql.query("SELECT language FROM " + config.tablePrefix + "PLAYERS WHERE UUID='"
						+ player.getUniqueId() + "';");

				if(query.next()) {
					fileName = query.getString("language");
				}

			} catch(SQLException e) {
				Log.severe("Error on player locale retrieval.", e);
			}
		} else {
			fileName = playerData.getString("players." + player.getUniqueId() + ".language", null);
		}

		if(fileName == null) {
			return Language.getLanguages().get(config.language);
		}

		return getLanguage(fileName);
	}

	public Language getLanguage(String fileName) {
		for(Entry<String, Language> local : Language.getLanguages().entrySet()) {
			if(local.getKey().equalsIgnoreCase(fileName)) {
				return local.getValue();
			}
		}

		if(Language.getLanguages().containsKey(config.language)) {
			return Language.getLanguages().get(config.language);
		}

		return Language.getLanguages().get("en-US");

	}

	public void setLanguage(Player player, String key) {
		if(mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET language='" + key + "' WHERE UUID='"
					+ player.getUniqueId() + "';");
		} else {
			playerData.set("players." + player.getUniqueId() + ".language", key);
			savePlayerData();
		}
	}

	/**
	 * Save the Player's Inventory and Armor contents.
	 * @param player player
	 */
	public void saveInventoryArmor(Player player) {
		playerData.set("players." + player.getUniqueId() + ".inventory", Arrays.asList(player.getInventory().getContents()));
		playerData.set("players." + player.getUniqueId() + ".armor", Arrays.asList(player.getInventory().getArmorContents()));
		savePlayerData();
	}

	/**
	 * Retrieve the Player's saved Inventory contents.
	 * @param player player
	 * @return player's inventory contents
	 */
	public ItemStack[] getSavedInventoryContents(Player player) {
		try {
			@SuppressWarnings("unchecked") List<ItemStack> contents = (List<ItemStack>) playerData.getList("players." + player.getUniqueId() + ".inventory");
			return contents != null ? contents.toArray(new ItemStack[0]) : null;
		} catch(ClassCastException e) {
			Log.severe("Could not read player's " + player.getName() + " inventory.", e);
		}
		return null;
	}

	/**
	 * Retrieve the Player's saved Armor contents.
	 * @param player player
	 * @return player's armor contents
	 */
	public ItemStack[] getSavedArmorContents(Player player) {
		try {
			@SuppressWarnings("unchecked") List<ItemStack> contents = (List<ItemStack>) playerData.getList("players." + player.getUniqueId() + ".armor");
			return contents != null ? contents.toArray(new ItemStack[0]) : null;
		} catch(ClassCastException e) {
			Log.severe("Could not read player's " + player.getName() + " inventory.", e);
		}
		return null;
	}

	/**
	 * Reset a Player's saved Inventory and Armor contents.
	 * @param player player
	 */
	public void resetSavedInventoryArmor(Player player) {
		playerData.set("players." + player.getUniqueId() + ".inventory", null);
		playerData.set("players." + player.getUniqueId() + ".armor", null);
		savePlayerData();
	}
}
