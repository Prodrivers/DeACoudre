package me.poutineqc.deacoudre.events;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.MySQL;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.achievements.Achievement;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.instances.User;

public class PlayerMove implements Listener {

	private DeACoudre plugin;
	private PlayerData playerData;
	private Achievement achievements;
	private MySQL mysql;
	private Configuration config;

	public PlayerMove(DeACoudre plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfiguration();
		this.mysql = plugin.getMySQL();
		this.achievements = plugin.getAchievement();
		this.playerData = plugin.getPlayerData();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {

		final Player player = (Player) event.getPlayer();

		final Arena arena = Arena.getArenaFromPlayer(player);
		if (arena == null)
			return;

		if (arena.getGameState() != GameState.ACTIVE)
			return;

		final User user = arena.getUser(player);

		if (user != arena.getActivePlayer())
			return;

		final Location getTo = new Location(event.getTo().getWorld(), event.getTo().getBlockX(),
				event.getTo().getBlockY(), event.getTo().getBlockZ());

		if (!getTo.getBlock().isLiquid())
			return;

		while (getTo.add(new Vector(0, 1, 0)).getBlock().isLiquid()) {
		}
		getTo.add(new Vector(0, -1, 0));

		Location north = new Location(getTo.getWorld(), getTo.getBlockX(), getTo.getBlockY(), getTo.getBlockZ() - 1);
		Location south = new Location(getTo.getWorld(), getTo.getBlockX(), getTo.getBlockY(), getTo.getBlockZ() + 1);
		Location east = new Location(getTo.getWorld(), getTo.getBlockX() + 1, getTo.getBlockY(), getTo.getBlockZ());
		Location west = new Location(getTo.getWorld(), getTo.getBlockX() - 1, getTo.getBlockY(), getTo.getBlockZ());

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				player.teleport(arena.getLobby());
				
				Arena arena = Arena.getArenaFromPlayer(player);
				if (arena == null)
					return;

				user.maxStats(true);
				if (arena.getGameState() == GameState.ACTIVE) {
					arena.getScoreboard().resetScores(ChatColor.AQUA + user.getName());
					arena.getObjective().getScore(user.getName()).setScore(user.getPoint());
				}
			}
		}, 5L);

		Language local = playerData.getLanguageOfPlayer(player);

		arena.resetStallingAmount();
		arena.bumpCurrentTile();

		if (!north.getBlock().isLiquid() && !south.getBlock().isLiquid() && !west.getBlock().isLiquid()
				&& !east.getBlock().isLiquid()) {

			user.addPoint();

			int DaCdone = 0;
			if (mysql.hasConnection()) {
				ResultSet query = mysql.query(
						"SELECT DaCdone FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + user.getUUID() + "';");
				try {
					if (query.next())
						DaCdone = query.getInt("DaCdone");
				} catch (SQLException e) {
					e.printStackTrace();
				}

				mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET DaCdone='" + ++DaCdone + "' WHERE UUID='"
						+ user.getUUID() + "';");
			} else {
				DaCdone = playerData.getData().getInt("players." + player.getUniqueId() + ".DaCdone") + 1;
				playerData.getData().set("players." + player.getUniqueId() + ".DaCdone", DaCdone);
				playerData.savePlayerData();
			}

			if (!arena.isForceStart()) {
				achievements.testAchievement(Achievement.dacDone, player);

				if (arena.getRoundNo() == 42)
					achievements.testAchievement(Achievement.dacOnFortyTwo, player);
			}

			local.sendMsg(user.getPlayer(),
					local.gamePointsUpPlayer.replace("%points%", String.valueOf(user.getPoint())));

			for (User u : arena.getUsers()) {
				if (u != user) {
					Language localInstance = playerData.getLanguageOfPlayer(u.getPlayer());
					localInstance.sendMsg(u.getPlayer(),
							localInstance.gamePointsUpOthers.replace("%points%", String.valueOf(user.getPoint()))
									.replace("%player%", player.getDisplayName()));
				}

			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					do {
						getTo.getBlock().setType(Material.STAINED_GLASS);
						getTo.getBlock().setData((byte) user.getItemStack().getDurability());

						getTo.add(0, -1, 0);
					} while (getTo.getBlock().getType() == Material.WATER);
				}
			}, 5L);
		} else {

			if (config.verbose) {
				local.sendMsg(user.getPlayer(), local.gameSuccessPlayer);

				for (User op : arena.getUsers()) {
					if (op != user) {
						Language localInstance = playerData.getLanguageOfPlayer(op.getPlayer());
						localInstance.sendMsg(op.getPlayer(),
								localInstance.gameSuccessOthers.replace("%player%", player.getDisplayName()));
					}

				}
			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					do {
						getTo.getBlock().setType(user.getItemStack().getType());
						getTo.getBlock().setData((byte) user.getItemStack().getDurability());

						getTo.add(0, -1, 0);
					} while (getTo.getBlock().getType() == Material.WATER);
				}
			}, 5L);

		}

		user.setRoundSuccess(true);
		arena.flushConfirmationQueue(user);

		if (!arena.isOver() || arena.isForceStart()) {
			arena.nextPlayer();
		} else {
			user.getPlayer().setVelocity(new Vector());
			user.getPlayer().setFallDistance(0);
			arena.finishGame(false);
		}

	}
}
