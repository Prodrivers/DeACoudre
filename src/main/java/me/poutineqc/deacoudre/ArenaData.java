package me.poutineqc.deacoudre;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ArenaData {

	private final File arenaFile;
	private FileConfiguration arenaData;

	public ArenaData(DeACoudre plugin) {

		arenaFile = new File(plugin.getDataFolder(), "arenaData.yml");
		if(!arenaFile.exists()) {
			try {
				arenaFile.createNewFile();
			} catch(IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create arenaData.ylm.");
			}
		}
		loadArenaData();
	}

	public FileConfiguration getData() {
		return arenaData;
	}

	public void saveArenaData() {
		try {
			arenaData.save(arenaFile);
		} catch(IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save arenaData.yml!");
		}
	}

	public void loadArenaData() {
		arenaData = YamlConfiguration.loadConfiguration(arenaFile);
	}
}
