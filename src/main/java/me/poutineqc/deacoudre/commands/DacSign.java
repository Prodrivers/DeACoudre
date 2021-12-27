package me.poutineqc.deacoudre.commands;

import me.poutineqc.deacoudre.*;
import me.poutineqc.deacoudre.instances.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.SignChangeEvent;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DacSign {

	private static final List<DacSign> signs = new ArrayList<>();
	private static Configuration config;
	private static MySQL mysql;
	private static PlayerData playerData;
	private static File signFile;
	private static YamlConfiguration signData;
	private UUID uuid;
	private SignType type;
	private Location location;

	public DacSign(DeACoudre plugin) {
		DacSign.config = plugin.getConfiguration();
		DacSign.mysql = plugin.getMySQL();
		DacSign.playerData = plugin.getPlayerData();

		signFile = new File(plugin.getDataFolder(), "signData.yml");
		if(!signFile.exists()) {
			try {
				signFile.createNewFile();
			} catch(IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create signData.ylm.");
			}
		}

		loadSignData();
	}

	public DacSign(UUID uuid, Location location, SignType type) {
		this.uuid = uuid;
		this.type = type;
		this.location = location;

		boolean delete = type == null;

		Arena arena = null;
		BlockState block = null;
		try {
			block = location.getBlock().getState();
			if(!(block instanceof Sign sign)) {
				delete = true;
			} else {
				arena = Arena.getArenaFromName(sign.getLine(2));
				if(arena == null) {
					if(type == SignType.JOIN || type == SignType.PLAY) {
						delete = true;
					}
				} else if(type == SignType.PLAY && arena.getWorld() != location.getWorld()) {
					delete = true;
				}
			}
		} catch(NullPointerException e) {
			delete = true;
		}

		if(delete) {
			removeSign();
			return;
		}

		signs.add(this);
		if(type == SignType.JOIN || type == SignType.PLAY) {
			updateSigns(arena);
		}
	}

	public DacSign(SignChangeEvent event, SignType type) {
		Language local = playerData.getLanguage(config.language);

		Arena arena = Arena.getArenaFromName(event.getLine(2));
		this.uuid = UUID.randomUUID();
		this.type = type;
		this.location = event.getBlock().getLocation();

		switch(type) {
			case COLOR -> {
				event.setLine(0, "");
				event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
				event.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signColor));
				event.setLine(3, "");
			}
			case JOIN -> {
				event.setLine(0, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
				event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signJoin));
				switch(arena.getGameState()) {
					case ACTIVE, ENDING -> event.setLine(3, ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateActive));
					case READY, STARTUP -> event.setLine(3,
							ChatColor.stripColor(
									ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers)) + " : "
									+ arena.getNonEliminated().size() + "/" + arena.getMaxPlayer());
					case UNREADY -> event.setLine(3, ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateUnset));
				}
			}
			case PLAY -> {
				event.setLine(0, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
				event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signPlay));
				switch(arena.getGameState()) {
					case ACTIVE, ENDING -> event.setLine(3, ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateActive));
					case READY, STARTUP -> event.setLine(3,
							ChatColor.stripColor(
									ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers)) + " : "
									+ arena.getNonEliminated().size() + "/" + arena.getMaxPlayer());
					case UNREADY -> event.setLine(3, ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateUnset));
				}
			}
			case QUIT -> {
				event.setLine(0, "");
				event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
				event.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signQuit));
				event.setLine(3, "");
			}
			case START -> {
				event.setLine(0, "");
				event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
				event.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signStart));
				event.setLine(3, "");
			}
			case STATS -> {
				event.setLine(0, "");
				event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
				event.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signStats));
				event.setLine(3, "");
			}
			default -> {
				event.setLine(0, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
				event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signNotValid1));
				event.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signNotValid2));
				event.setLine(3, ChatColor.translateAlternateColorCodes('&', local.signNotValid3));
			}
		}

		signs.add(this);
		if(type == SignType.JOIN || type == SignType.PLAY) {
			updateSigns(arena);
		}

		if(mysql.hasConnection()) {
			mysql.update("INSERT INTO " + config.tablePrefix
					+ "SIGNS (uuid, type ,locationWorld, locationX, locationY, locationZ) " + "VALUES ('" + uuid + "','"
					+ type + "','" + location.getWorld().getName() + "','" + location.getBlockX() + "','"
					+ location.getBlockY() + "','" + location.getBlockZ() + "');");

		} else {
			signData.set("signs." + uuid.toString() + ".type", type.toString());
			signData.set("signs." + uuid.toString() + ".location.world", location.getWorld().getName());
			signData.set("signs." + uuid.toString() + ".location.X", location.getBlockX());
			signData.set("signs." + uuid.toString() + ".location.Y", location.getBlockY());
			signData.set("signs." + uuid.toString() + ".location.Z", location.getBlockZ());
			saveSignData();
		}

	}

	private static void loadSignData() {
		signData = YamlConfiguration.loadConfiguration(signFile);
	}

	public static void loadAllSigns() {
		signs.clear();

		if(mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "SIGNS;");
			try {
				while(query.next()) {
					UUID uuid = UUID.fromString(query.getString("uuid"));
					SignType type = getSignType(query.getString("type"));
					Location location = new Location(Bukkit.getWorld(query.getString("locationWorld")),
							query.getInt("locationX"), query.getInt("locationY"), query.getInt("locationZ"));

					new DacSign(uuid, location, type);
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		} else {
			if(!signData.contains("signs")) {
				return;
			}

			for(String uuid : signData.getConfigurationSection("signs").getKeys(false)) {
				ConfigurationSection cs = signData.getConfigurationSection("signs." + uuid);
				SignType type = getSignType(cs.getString("type", UUID.randomUUID().toString()));
				Location location = new Location(Bukkit.getWorld(cs.getString("location.world")),
						cs.getInt("location.X", 0), cs.getInt("location.Y", 0), cs.getInt("location.Z"));

				new DacSign(UUID.fromString(uuid), location, type);
			}

		}

		updateSigns();
	}

	private static SignType getSignType(String string) {
		switch(string) {
			case "JOIN":
				return SignType.JOIN;
			case "QUIT":
				return SignType.QUIT;
			case "PLAY":
				return SignType.PLAY;
			case "START":
				return SignType.START;
			case "STATS":
				return SignType.STATS;
			case "COLOR":
				return SignType.COLOR;
			default:
				return null;
		}
	}

	public static void arenaDelete(Arena arena) {
		for(DacSign dacsign : signs) {

			if(!arena.getName().equalsIgnoreCase(((Sign) dacsign.location.getBlock().getState()).getLine(2))) {
				continue;
			}

			removeSign(dacsign);
			arenaDelete(arena);
			return;
		}
	}

	public static void removeSign(DacSign dacsign) {
		dacsign.removeSign();
	}

	public static void updateSigns() {

		Language local = playerData.getLanguage(config.language);
		for(DacSign dacsign : signs) {

			Sign sign = (Sign) dacsign.location.getBlock().getState();

			switch(dacsign.type) {
				case COLOR -> {
					sign.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
					sign.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signColor));
				}
				case JOIN -> {
					sign.setLine(0, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
					sign.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signJoin));
				}
				case PLAY -> {
					sign.setLine(0, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
					sign.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signPlay));
				}
				case QUIT -> {
					sign.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
					sign.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signQuit));
				}
				case START -> {
					sign.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
					sign.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signStart));
				}
				case STATS -> {
					sign.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
					sign.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signStats));
				}
			}

			sign.update();

			if(dacsign.type != SignType.JOIN && dacsign.type != SignType.PLAY) {
				continue;
			}

			Arena arena = Arena.getArenaFromName(sign.getLine(2));
			if(arena == null) {
				continue;
			}

			dacsign.updateGameState(sign, arena);
		}
	}

	public static void updateSigns(Arena arena) {
		for(DacSign dacsign : signs) {
			if(arena == null) {
				continue;
			}

			Sign sign = (Sign) dacsign.location.getBlock().getState();

			if(!sign.getLine(2).equalsIgnoreCase(arena.getName())) {
				continue;
			}

			dacsign.updateGameState(sign, arena);
		}
	}

	public static void removePlaySigns(Arena arena) {
		for(DacSign dacsign : signs) {

			if(!arena.getName().equalsIgnoreCase(((Sign) dacsign.location.getBlock().getState()).getLine(2))
					|| dacsign.type != SignType.PLAY) {
				continue;
			}

			removeSign(dacsign);
			signs.remove(dacsign);
			removePlaySigns(arena);
			return;
		}
	}

	public static DacSign getDacSign(Location location) {
		for(DacSign dacsign : signs) {
			if(dacsign.location.getWorld() == location.getWorld()) {
				if(dacsign.location.distance(location) == 0) {
					return dacsign;
				}
			}
		}

		return null;
	}

	private static void saveSignData() {
		try {
			signData.save(signFile);
		} catch(IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save signData.yml!");
		}
	}

	public void removeSign() {
		if(location.getBlock().getState() instanceof Sign sign) {
			sign.setLine(0, " ");
			sign.setLine(1, " ");
			sign.setLine(2, " ");
			sign.setLine(3, " ");
			sign.getLocation().getChunk().load();
			sign.update();
		}

		signs.remove(this);

		if(mysql.hasConnection()) {
			mysql.update("DELETE FROM " + config.tablePrefix + "SIGNS WHERE uuid='" + uuid.toString() + "';");
		} else {
			signData.set("signs." + uuid.toString(), null);
			saveSignData();
		}
	}

	private void updateGameState(Sign sign, Arena arena) {
		Language local = playerData.getLanguage(config.language);
		switch(arena.getGameState()) {
			case ACTIVE, ENDING -> sign.setLine(3, ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateActive));
			case READY, STARTUP -> sign.setLine(3,
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers))
							+ " : " + arena.getNonEliminated().size() + "/"
							+ arena.getMaxPlayer());
			case UNREADY -> sign.setLine(3, ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateUnset));
		}

		sign.update();
	}

	public SignType getSignType() {
		return type;
	}

	public YamlConfiguration getData() {
		return signData;
	}

}
