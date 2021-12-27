package me.poutineqc.deacoudre.instances;

import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.tools.ItemStackManager;
import me.poutineqc.deacoudre.tools.OriginalPlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

import java.util.UUID;

public class User {

	private static Configuration config;

	private Player player;
	private String displayName;
	private String name;
	private OriginalPlayerStats originalStats;
	private ItemStackManager color;
	private int points = 0;
	private int place;
	private boolean roundSuccess = false;
	private boolean eliminated = false;
	private boolean waitingForConfirmation = false;
	private Score score;
	private Arena arena;

	public User(DeACoudre plugin) {
		User.config = plugin.getConfiguration();
	}

	public User(Player player, Arena arena, boolean tpAuto, boolean eliminated) {
		this.player = player;
		this.arena = arena;
		this.name = ChatColor.stripColor(player.getName());
		this.displayName = player.getDisplayName();

		this.eliminated = eliminated;
		if(eliminated) {
			this.points = -2;
		} else {
			this.score = arena.getObjective().getScore(name);
			score.setScore(points);
		}

		player.setScoreboard(arena.getObjective().getScoreboard());

		originalStats = new OriginalPlayerStats(config, player);

		if(tpAuto) {
			player.teleport(arena.getLobby());
		}

		originalStats.fillOtherStats(player);
		maxStats(false);

	}

	public User(int place) {
		this.place = place;
	}

	public User(String name, int place) {
		this.place = place;
		this.name = name;
	}

	public void unEliminate(Arena arena) {
		this.eliminated = false;
		points = 0;
		this.score = arena.getObjective().getScore(name);
		score.setScore(points);
	}

	public String getName() {
		return name;
	}

	public int getPlace() {
		return place;
	}

	public void setPlace(int place) {
		this.place = place;
	}

	public void removeColor() {
		setColor(null);
	}

	public ItemStackManager getColor() {
		return color;
	}

	public void setColor(ItemStackManager item) {
		if(this.color != null) {
			this.color.setAvailable(true);
		}
		this.color = item;
		if(this.color != null) {
			this.color.setAvailable(false);
		}
	}

	public UUID getUUID() {
		return player.getUniqueId();
	}

	public Player getPlayer() {
		return player;
	}

	public void addPoint() {
		score.setScore(++points);
	}

	public void removePoint() {
		score.setScore(--points);
	}

	public int getPoint() {
		return points;
	}

	public boolean isRoundSuccess() {
		return roundSuccess;
	}

	public void setRoundSuccess(boolean roundSuccess) {
		this.roundSuccess = roundSuccess;
	}

	public void returnOriginalPlayerStats() {
		originalStats.returnStats(player);
	}

	public boolean isEliminated() {
		return eliminated;
	}

	public void eliminate() {
		eliminated = true;
		score.getObjective().getScoreboard().resetScores(name);
		score.getObjective().getScoreboard().resetScores(ChatColor.AQUA + name);
		points = -2;
	}

	public boolean isWaitingForConfirmation() {
		if(waitingForConfirmation) {
			waitingForConfirmation = false;
			return true;
		}

		return false;
	}

	public void addWaitingForConfirmation() {
		waitingForConfirmation = true;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void maxStats(boolean spectator) {
		originalStats.maxStats(player, arena, spectator);
	}
}
