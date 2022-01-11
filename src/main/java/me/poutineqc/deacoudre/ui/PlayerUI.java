package me.poutineqc.deacoudre.ui;

import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.instances.User;
import me.poutineqc.deacoudre.tools.ColorManager;
import me.poutineqc.deacoudre.tools.Utils;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlayerUI {
	private final Plugin plugin;
	private final Configuration configuration;
	private final PlayerData playerData;

	@Inject
	public PlayerUI(Plugin plugin, Configuration configuration, PlayerData playerData) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.playerData = playerData;
	}

	private void scheduleSound(final Player player, final long delay, final Sound sound, final float velocity) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			player.playSound(
					player.getLocation(),
					sound,
					SoundCategory.RECORDS,
					10,
					velocity
			);
		}, delay);
	}

	private void scheduleSound(final Arena arena, final long delay, final Sound sound, final float velocity) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			for(User user : arena.getUsers()) {
				user.getPlayer().playSound(
						user.getPlayer().getLocation(),
						sound,
						SoundCategory.RECORDS,
						10,
						velocity
				);
			}
		}, delay);
	}

	private void spawnFirework(Location loc, Color color, Color fadeColor, float yOffset) {
		FireworkEffect effect = FireworkEffect.builder().trail(false).flicker(false).withColor(color).withFade(fadeColor).with(FireworkEffect.Type.BALL).build();
		final World world = loc.getWorld();
		if(world != null) {
			final Firework fw = world.spawn(loc.clone().add(0, yOffset, 0), Firework.class);
			FireworkMeta meta = fw.getFireworkMeta();
			meta.addEffect(effect);
			meta.setPower(0);
			fw.setFireworkMeta(meta);

			Bukkit.getScheduler().runTaskLater(this.plugin, fw::detonate, 2L);
		}
	}

	public void onJumpFailed(final User user, final Arena arena) {
		arena.getScoreboard().resetScores(ChatColor.AQUA + user.getName());
		arena.getObjective().getScore(user.getName()).setScore(user.getPoint());
	}

	public void onJumpSucceeded(final User user, final Arena arena, final Location getTo) {
		final Player player = user.getPlayer();
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
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

			do {
				if(user.getColor() != null && user.getColor().getColor() != null) {
					getTo.getBlock().setType(Utils.colorToStainedGlassBlock(user.getColor().getColor()));
				} else {
					getTo.getBlock().setType(Material.BLACK_STAINED_GLASS);
				}

				getTo.add(0, -1, 0);
			} while(getTo.getBlock().getType() == Material.WATER);
		}, 4L);
	}

	public void onRegularJump(final Arena arena, final User user) {
		if(this.configuration.verbose) {
			String playerDisplayName = user.getDisplayName();

			Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

			locale.sendMsg(user.getPlayer(), locale.gameSuccessPlayer);

			for(User op : arena.getUsers()) {
				if(op != user) {
					Language loopPlayerLocale = playerData.getLanguageOfPlayer(op.getPlayer());
					loopPlayerLocale.sendMsg(
							op.getPlayer(),
							loopPlayerLocale.gameSuccessOthers.replace("%player%", playerDisplayName)
					);
				}

			}
		}

		scheduleSound(user.getPlayer(), 1L, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f);
	}

	public void onDaC(final Arena arena, final User user) {
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(),
				locale.gamePointsUpPlayer.replace("%points%", String.valueOf(user.getPoint())));

		for(User u : arena.getUsers()) {
			String playerDisplayName = user.getDisplayName();
			if(u != user) {
				Language loopPlayerLocale = playerData.getLanguageOfPlayer(u.getPlayer());
				loopPlayerLocale.sendMsg(
						u.getPlayer(),
						loopPlayerLocale.gamePointsUpOthers.replace("%points%", String.valueOf(user.getPoint()))
								.replace("%player%", playerDisplayName)
				);
			}
		}

		scheduleSound(arena, 1L, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f);
		spawnFirework(user.getLocation(), Color.RED, Color.GREEN, 0f);
		spawnFirework(user.getLocation(), Color.RED, Color.GREEN, 0.75f);
	}

	public void onPlayerEliminated(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(), locale.gamePointsEliminatePlayer);

		for(User u : arena.getUsers()) {
			if(user != u) {
				Language loopPlayerLocale = playerData.getLanguageOfPlayer(u.getPlayer());
				loopPlayerLocale.sendMsg(
						u.getPlayer(),
						loopPlayerLocale.gamePointsEliminateOthers.replace("%player%", playerDisplayName)
				);
			}
		}

		scheduleSound(user.getPlayer(), 1L, Sound.ENTITY_ITEM_BREAK, 0.5f);
		scheduleSound(user.getPlayer(), 1L, Sound.ENTITY_PLAYER_DEATH, 1);
	}

	public void onPlayerLostLife(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();
		String playerPoints = String.valueOf(user.getPoint());
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(), locale.gamePointsDownPlayer.replace("%points%", playerPoints));

		for(User u : arena.getUsers()) {
			if(user != u) {
				Language loopPlayerLocale = playerData.getLanguageOfPlayer(u.getPlayer());
				loopPlayerLocale.sendMsg(
						u.getPlayer(),
						loopPlayerLocale.gamePointsDownOthers.replace("%player%", playerDisplayName)
								.replace("%points%", playerPoints)
				);
			}
		}

		scheduleSound(user.getPlayer(), 1L, Sound.ENTITY_PLAYER_BIG_FALL, 1);
	}

	public void onLastRoundPlayerEliminatedAndRevivalOfEveryone(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(), locale.gamePointsReviveLastLastPlayer);

		for(User p : arena.getUsers()) {
			if(p != user) {
				Language loopPlayerLocale = playerData.getLanguageOfPlayer(p.getPlayer());
				loopPlayerLocale.sendMsg(
						p.getPlayer(),
						loopPlayerLocale.gamePointsReviveLastLastOthers
							.replace("%player%", playerDisplayName)
				);
			}
		}

		scheduleSound(arena, 1L, Sound.ITEM_TOTEM_USE, 1);
	}

	public void onLastRoundPlayerLostLifeRevivalOfEveryone(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(), locale.gamePointsReviveLastMultiplePlayer);

		for(User p : arena.getUsers()) {
			if(p != user) {
				Language loopPlayerLocale = playerData.getLanguageOfPlayer(p.getPlayer());
				loopPlayerLocale.sendMsg(
						p.getPlayer(),
						loopPlayerLocale.gamePointsReviveLastMultipleOthers
							.replace("%player%", playerDisplayName)
				);
			}
		}

		scheduleSound(arena, 1L, Sound.ITEM_TOTEM_USE, 1);
	}

	public void onNonLastRoundPlayerEliminated(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(), locale.gamePointsConfirmationPlayer);

		for(User p : arena.getUsers()) {
			if(p != user) {
				Language loopPlayerLocale = playerData.getLanguageOfPlayer(p.getPlayer());
				loopPlayerLocale.sendMsg(
						p.getPlayer(),
						loopPlayerLocale.gamePointsConfirmationOthers
							.replace("%player%", playerDisplayName)
				);
			}
		}

		scheduleSound(user.getPlayer(), 1L, Sound.ENTITY_ITEM_BREAK, 0.5f);
		scheduleSound(user.getPlayer(), 1L, Sound.ENTITY_PLAYER_DEATH, 1);
	}

	public void onNonLastRoundPlayerLostLife(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();
		String playerPoints = String.valueOf(user.getPoint());

		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(),
				locale.gamePointsDownPlayer.replace("%points%", playerPoints)
		);
		locale.sendMsg(user.getPlayer(), locale.gamePointsReviveHint);

		for(User p : arena.getUsers()) {
			if(user != p) {
				Language loopPlayerLocale = playerData.getLanguageOfPlayer(p.getPlayer());
				loopPlayerLocale.sendMsg(
						p.getPlayer(),
						loopPlayerLocale.gamePointsDownOthers
								.replace("%player%", playerDisplayName)
								.replace("%points%", playerPoints)
				);
			}
		}

		scheduleSound(user.getPlayer(), 1L, Sound.ENTITY_PLAYER_BIG_FALL, 1);
	}

	public void onNonLastPlayerEliminatedAfterOtherSuccess(User eliminated, User eliminator, Arena arena) {
		String eliminatedDisplayName = eliminated.getPlayer().getDisplayName();
		String eliminatorDisplayName = eliminator.getPlayer().getDisplayName();

		Language locale = playerData.getLanguageOfPlayer(eliminated);
		locale.sendMsg(eliminated, locale.gamePointsFlushPlayer.replace("%player%", eliminatorDisplayName));

		for(User op : arena.getUsers()) {
			if(op != eliminated) {
				Language localInstance = playerData.getLanguageOfPlayer(op);
				localInstance.sendMsg(op,
						localInstance.gamePointsFlushOthers
							.replace("%player%", eliminatorDisplayName)
							.replace("%looser%", eliminatedDisplayName)
				);
			}
		}

		scheduleSound(eliminated.getPlayer(), 1L, Sound.ENTITY_ITEM_BREAK, 0.5f);
		scheduleSound(eliminated.getPlayer(), 1L, Sound.ENTITY_PLAYER_DEATH, 1);
	}

	public void onPlayerTimeTick(Player player, int timeTick) {
		int level = (int) Math.floor(timeTick / 20.0) + 1;
		player.setLevel(level);
		player.setExp((float) ((timeTick % 20) / 20.0));

		switch(timeTick / 20) {
			case 10:
			case 9:
			case 8:
			case 7:
			case 6:
			case 5:
			case 4:
			case 3:
			case 2:
			case 1:
				if(timeTick % 20 == 0) {
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
				}
		}
	}

	public void onPlayerTimeOut(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(), locale.gameTimeOutPlayer);

		for(User p : arena.getUsers()) {
			if(user != p) {
				Language loopPlayerLocale = playerData.getLanguageOfPlayer(p);
				loopPlayerLocale.sendMsg(
						p.getPlayer(),
						loopPlayerLocale.gameTimeOutOthers.replace("%player%", playerDisplayName)
				);
			}
		}

		scheduleSound(user.getPlayer(), 1L, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f);
		scheduleSound(user.getPlayer(), 4L, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f);
	}

	public void onNonLastPlayerRevivedAfterNoSuccess(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();
		String playerPoints = String.valueOf(user.getPoint());

		Language locale = playerData.getLanguageOfPlayer(user);

		locale.sendMsg(user, locale.gamePointsRevivePlayer.replace("%points%", playerPoints));

		for(User u : arena.getUsers()) {
			if(u != user) {
				Language localInstance = playerData.getLanguageOfPlayer(u);
				localInstance.sendMsg(
						u,
						localInstance.gamePointsReviveOthers
								.replace("%player%", playerDisplayName)
								.replace("%points%", playerPoints)
				);
			}
		}
	}

	public void onRewarded(Player player, double reward) {
		Language locale = playerData.getLanguageOfPlayer(player);

		locale.sendMsg(
				player,
				locale.endingRewardMoney
						.replace("%amount%", String.valueOf(reward))
						.replace("%currency%", DeACoudre.getEconomy().currencyNamePlural())
		);

		scheduleSound(player, 1L, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f);
		scheduleSound(player, 4L, Sound.ENTITY_PLAYER_LEVELUP, 1f);
	}

	public void onRewarded(Player player, ItemStack reward) {
		Language locale = playerData.getLanguageOfPlayer(player);

		if(reward.getItemMeta().hasDisplayName()) {
			locale.sendMsg(
					player,
					locale.endingRewardItemsReceive
							.replace("%amount%", String.valueOf(reward.getAmount()))
							.replace("%item%", reward.getItemMeta().getDisplayName())
			);
		} else {
			locale.sendMsg(
					player,
					locale.endingRewardItemsReceive
							.replace("%amount%", String.valueOf(reward.getAmount()))
							.replace("%item%", reward.getType().name())
			);
		}

		scheduleSound(player, 1L, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f);
		scheduleSound(player, 4L, Sound.ENTITY_PLAYER_LEVELUP, 1f);
	}

	public void onRewardedSingleNoSpaceLeft(Player player) {
		Language locale = playerData.getLanguageOfPlayer(player);

		locale.sendMsg(player, locale.endingRewardItemsSpaceOne);
	}

	public void onRewardedMultipleNoSpaceLeft(Player player) {
		Language locale = playerData.getLanguageOfPlayer(player);

		locale.sendMsg(player, locale.endingRewardItemsSpaceMultiple);
	}

	public void onAssignedColor(User user) {
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(
				user.getPlayer(),
				Utils.replaceInComponent(
						locale.startRandomColor,
						"%material%", ColorManager.getTranslatedMaterialName(user.getColor().getItem(), locale)
				)
		);
	}

	public void onPlayerAssignedPosition(User user, Arena arena, int position) {
		String pos = String.valueOf(position);
		for(User u : arena.getUsers()) {
			Language local = playerData.getLanguageOfPlayer(u);
			local.sendMsg(
					u,
					local.startPosition
							.replace("%player%", user.getDisplayName())
							.replace("%posNo%", pos)
			);
		}
	}
}