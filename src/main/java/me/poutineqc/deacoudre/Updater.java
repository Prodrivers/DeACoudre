package me.poutineqc.deacoudre;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.poutineqc.deacoudre.commands.DacCommand;

public final class Updater implements Listener {

	private static final String spigotPage = "https://www.spigotmc.org/resources/de-a-coudre.14635/";

	private final DeACoudre plugin;
	private final int id;
	
	private boolean lastVersion;
	private String latestVersion;

	public Updater(final DeACoudre plugin) {
		this.plugin = plugin;

		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				checkForLastVersion(plugin);
				if (!lastVersion) {
					notifyConsole(plugin);
				}
			}
		}, 0, 72000L);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (Permissions.hasPermission(event.getPlayer(), DacCommand.getCommand("reload"), false) && !lastVersion)
			notifyPlayer(event.getPlayer());
	}

	private void checkForLastVersion(DeACoudre plugin) {
		try {
			lastVersion = getInfoFromServer();
		} catch (IOException e) {
			plugin.getLogger().warning("Could not find the latest version available.");
			stop();
			return;
		}
	}

	private boolean getInfoFromServer() throws IOException {
		URL oracle = new URL(spigotPage + "history");
		URLConnection urlConn = oracle.openConnection();
		urlConn.addRequestProperty("User-Agent", "Mozilla/4.76");
		InputStream is = urlConn.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is, 4 * 1024);
		BufferedReader in = new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8));
		
		latestVersion = null;
		String inputLine;
		while ((inputLine = in.readLine()) != null && latestVersion == null)
			if (inputLine.matches("^.*?([1-9][0-9]?\\.[1-9][0-9]?.*)$") && inputLine.contains(plugin.getName())) {
				latestVersion = inputLine.replaceAll(" ", "").replace(plugin.getName(), "").replaceAll("<[^>]*>", "");
			}

		in.close();
		if (latestVersion == null) {
			throw new IOException("Could not find the version on the page.");
		}
		
		return latestVersion.equalsIgnoreCase(plugin.getDescription().getVersion());
	}

	private void notifyConsole(DeACoudre plugin) {
		Logger logger = plugin.getLogger();
		logger.info("----------------------------");
		logger.info("DeACoudre Updater");
		logger.info("");
		logger.info("An update for DeACoudre has been found!");
		logger.info("DeACoudre " + latestVersion);
		logger.info("You are running " + plugin.getDescription().getVersion());
		logger.info("");
		logger.info("Download at " + spigotPage);
		logger.info("----------------------------");
	}

	private void notifyPlayer(Player player) {
		Language local = plugin.getPlayerData().getLanguageOfPlayer(player);
		local.sendMsg(player,
				String.format("&3A new DeACoudre version is available &b(v%1$s)&3.%n&3Get it now : &b%2$s",
						latestVersion, spigotPage));
	}

	public void stop() {
		Bukkit.getScheduler().cancelTask(id);
	}
}
