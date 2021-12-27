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

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerData implements Listener {
	private final HashMap<String, String> originalPlayerName = new HashMap<>();
	private final File playerFile;
	private final MySQL mysql;
	private final Configuration config;
	private FileConfiguration playerData;
	private boolean lastVersion;
	private String latestVersion;

	public PlayerData(final DeACoudre plugin) {
		this.config = plugin.getConfiguration();
		this.mysql = plugin.getMySQL();

		playerFile = new File(plugin.getDataFolder(), "playerData.yml");
		if(!playerFile.exists()) {
			try {
				playerFile.createNewFile();
			} catch(IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create playerData.ylm.");
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
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save arenaData.yml!");
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
				e.printStackTrace();
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
				e.printStackTrace();
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

	public boolean isLatestVersion() {
		return lastVersion;
	}

	public String getLatestVersion() {
		return latestVersion;
	}
}
