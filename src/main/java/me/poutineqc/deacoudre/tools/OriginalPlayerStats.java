package me.poutineqc.deacoudre.tools;

import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.Log;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;

public class OriginalPlayerStats {
	private final PlayerData playerData;

	private final boolean teleportAfterEnding;
	private final boolean invisibleFlyingSpectators;

	private final Location location;
	private int level;
	private float experience;
	private GameMode gameMode;
	private double health;
	private int foodLevel;
	private float saturation;
	private Collection<PotionEffect> effects;
	private boolean flying;
	private boolean allowFlight;

	public OriginalPlayerStats(Configuration config, PlayerData playerData, Player player) {
		this.teleportAfterEnding = config.teleportAfterEnding;
		this.invisibleFlyingSpectators = config.invisibleFlyingSpectators;
		this.playerData = playerData;
		this.location = player.getLocation();
	}

	public void returnStats(Player player) {
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}

		player.setAllowFlight(allowFlight);
		if(flying) {
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

		ItemStack[] inventoryContents = playerData.getSavedInventoryContents(player);

		if (inventoryContents != null) {
			player.getInventory().clear();
			player.getInventory().setContents(inventoryContents);

			ItemStack[] armorContents = playerData.getSavedArmorContents(player);

			if (armorContents != null) {
				player.getInventory().setArmorContents(armorContents);
			} else {
				Log.warning("No armor to restore for player " + player.getName());
			}
		} else {
			Log.warning("No inventory to restore for player " + player.getName());
		}
		playerData.resetSavedInventoryArmor(player);

		if(teleportAfterEnding) {
			player.teleport(location);
		} else if(invisibleFlyingSpectators) {
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
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}

		if(invisibleFlyingSpectators) {
			spectatorStats(player, arena, spectator);
		}
	}

	public void spectatorStats(Player player, Arena arena, boolean spectator) {
		player.setAllowFlight(spectator);

		if(spectator) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, false, false));

			if(!arena.getSpectator().hasEntry(player.getName())) {
				arena.getSpectator().addEntry(player.getName());
			}

		} else {
			if(player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				player.removePotionEffect(PotionEffectType.INVISIBILITY);
			}

			if(arena.getSpectator().hasEntry(player.getName())) {
				arena.getSpectator().removeEntry(player.getName());
			}
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

		playerData.saveInventoryArmor(player);

		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
		player.updateInventory();
	}
}
