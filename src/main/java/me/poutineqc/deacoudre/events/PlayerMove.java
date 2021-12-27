package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.*;
import me.poutineqc.deacoudre.achievements.Achievement;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.instances.User;
import me.poutineqc.deacoudre.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Colorable;
import org.bukkit.util.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerMove implements Listener {

	private final DeACoudre plugin;
	private final PlayerData playerData;
	private final Achievement achievements;
	private final MySQL mysql;
	private final Configuration config;

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
		if(arena == null) {
			return;
		}

		if(arena.getGameState() != GameState.ACTIVE) {
			return;
		}

		final User user = arena.getUser(player);

		if(user != arena.getActivePlayer()) {
			return;
		}

		final Location getTo = new Location(event.getTo().getWorld(), event.getTo().getBlockX(),
				event.getTo().getBlockY(), event.getTo().getBlockZ());

		if(!getTo.getBlock().isLiquid()) {
			return;
		}

		while(getTo.add(new Vector(0, 1, 0)).getBlock().isLiquid()) {
		}
		getTo.add(new Vector(0, -1, 0));

		Location north = new Location(getTo.getWorld(), getTo.getBlockX(), getTo.getBlockY(), getTo.getBlockZ() - 1);
		Location south = new Location(getTo.getWorld(), getTo.getBlockX(), getTo.getBlockY(), getTo.getBlockZ() + 1);
		Location east = new Location(getTo.getWorld(), getTo.getBlockX() + 1, getTo.getBlockY(), getTo.getBlockZ());
		Location west = new Location(getTo.getWorld(), getTo.getBlockX() - 1, getTo.getBlockY(), getTo.getBlockZ());

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			player.teleport(arena.getLobby());

			Arena arena1 = Arena.getArenaFromPlayer(player);
			if(arena1 == null) {
				return;
			}

			user.maxStats(true);
			if(arena1.getGameState() == GameState.ACTIVE) {
				arena1.getScoreboard().resetScores(ChatColor.AQUA + user.getName());
				arena1.getObjective().getScore(user.getName()).setScore(user.getPoint());
			}
		}, 5L);

		Language local = playerData.getLanguageOfPlayer(player);

		arena.resetStallingAmount();
		arena.bumpCurrentTile();

		if(!north.getBlock().isLiquid() && !south.getBlock().isLiquid() && !west.getBlock().isLiquid()
				&& !east.getBlock().isLiquid()) {

			user.addPoint();

			int DaCdone = 0;
			if(mysql.hasConnection()) {
				ResultSet query = mysql.query(
						"SELECT DaCdone FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + user.getUUID() + "';");
				try {
					if(query.next()) {
						DaCdone = query.getInt("DaCdone");
					}
				} catch(SQLException e) {
					e.printStackTrace();
				}

				mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET DaCdone='" + ++DaCdone + "' WHERE UUID='"
						+ user.getUUID() + "';");
			} else {
				DaCdone = playerData.getData().getInt("players." + player.getUniqueId() + ".DaCdone") + 1;
				playerData.getData().set("players." + player.getUniqueId() + ".DaCdone", DaCdone);
				playerData.savePlayerData();
			}

			if(!arena.isForceStart()) {
				achievements.testAchievement(Achievement.dacDone, player);

				if(arena.getRoundNo() == 42) {
					achievements.testAchievement(Achievement.dacOnFortyTwo, player);
				}
			}

			local.sendMsg(user.getPlayer(),
					local.gamePointsUpPlayer.replace("%points%", String.valueOf(user.getPoint())));

			for(User u : arena.getUsers()) {
				if(u != user) {
					Language localInstance = playerData.getLanguageOfPlayer(u.getPlayer());
					localInstance.sendMsg(u.getPlayer(),
							localInstance.gamePointsUpOthers.replace("%points%", String.valueOf(user.getPoint()))
									.replace("%player%", player.getDisplayName()));
				}

			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				do {
					if(user.getColor().getItem().getData() instanceof Colorable) {
						getTo.getBlock().setType(Utils.colorToStainedGlassBlock(((Colorable) user.getColor().getItem().getData()).getColor()));
					} else {
						getTo.getBlock().setType(Material.BLACK_STAINED_GLASS);
					}

					getTo.add(0, -1, 0);
				} while(getTo.getBlock().getType() == Material.WATER);
			}, 5L);
		} else {

			if(config.verbose) {
				local.sendMsg(user.getPlayer(), local.gameSuccessPlayer);

				for(User op : arena.getUsers()) {
					if(op != user) {
						Language localInstance = playerData.getLanguageOfPlayer(op.getPlayer());
						localInstance.sendMsg(op.getPlayer(),
								localInstance.gameSuccessOthers.replace("%player%", player.getDisplayName()));
					}

				}
			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				do {
					getTo.getBlock().setType(user.getColor().getItem().getType());

					getTo.add(0, -1, 0);
				} while(getTo.getBlock().getType() == Material.WATER);
			}, 5L);

		}

		user.setRoundSuccess(true);
		arena.flushConfirmationQueue(user);

		if(!arena.isOver() || arena.isForceStart()) {
			arena.nextPlayer();
		} else {
			user.getPlayer().setVelocity(new Vector());
			user.getPlayer().setFallDistance(0);
			arena.finishGame(false);
		}
	}
}
