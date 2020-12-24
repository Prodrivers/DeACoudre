package me.poutineqc.deacoudre.tools;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.instances.Arena;

public class OriginalPlayerStats {

	private Configuration config;

	private int level;
	private float experience;
	private GameMode gameMode;
	private double health;
	private int foodLevel;
	private float saturation;
	private Collection<PotionEffect> effects;
	private Location location;
	private boolean flying;
	private boolean allowFlight;

	public OriginalPlayerStats(Configuration config, Player player) {
		this.config = config;
		this.location = player.getLocation();
	}

	public void returnStats(Player player) {
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

		player.setAllowFlight(allowFlight);
		if (flying) {
			player.setAllowFlight(true);
			player.setFlying(flying);
		}
		player.setFallDistance(0);
		player.setVelocity(new Vector());
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		player.setLevel(level);
		player.setExp(experience);
		player.setGameMode(gameMode);
		player.setHealth(health);
		player.setFoodLevel(foodLevel);
		player.setSaturation(saturation);
		player.addPotionEffects(effects);

		if (config.teleportAfterEnding)
			player.teleport(location);
		else if (config.invisibleFlyingSpectators) {
			player.setFallDistance(-255);
		}
	}

	public void maxStats(Player player, Arena arena, boolean spectator) {
		player.setFallDistance(0);
		player.setVelocity(new Vector());
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setLevel(0);
		player.setExp(0);
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setSaturation(20);
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

		if (config.invisibleFlyingSpectators)
			spectatorStats(player, arena, spectator);
	}

	public void spectatorStats(Player player, Arena arena, boolean spectator) {
		player.setAllowFlight(spectator);

		if (spectator) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, false, false));

			if (!arena.getSpectator().hasEntry(player.getName()))
				arena.getSpectator().addEntry(player.getName());

		} else {
			if (player.hasPotionEffect(PotionEffectType.INVISIBILITY))
				player.removePotionEffect(PotionEffectType.INVISIBILITY);

			if (arena.getSpectator().hasEntry(player.getName()))
				arena.getSpectator().removeEntry(player.getName());
		}
	}

	public void fillOtherStats(Player player) {
		this.flying = player.isFlying();
		this.allowFlight = player.getAllowFlight();
		this.level = player.getLevel();
		this.experience = player.getExp();
		this.gameMode = player.getGameMode();
		this.health = player.getHealth();
		this.foodLevel = player.getFoodLevel();
		this.saturation = player.getSaturation();
		this.effects = player.getActivePotionEffects();

	}
}
