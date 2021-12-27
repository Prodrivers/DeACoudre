package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.instances.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PlayerDamage implements Listener {

	private final PlayerData playerData;

	public PlayerDamage(DeACoudre plugin) {
		this.playerData = plugin.getPlayerData();
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player player)) {
			return;
		}

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			return;
		}

		if(arena.getGameState() != GameState.ACTIVE) {
			return;
		}

		event.setCancelled(true);

		if(!event.getCause().equals(DamageCause.FALL)) {
			return;
		}

		User players = arena.getUser(player);

		if(players == arena.getActivePlayer()) {
			losingAlgorithm(player, arena, players);
		}

	}

	public void losingAlgorithm(Player player, Arena arena, User user) {

		Language local = playerData.getLanguageOfPlayer(player);
		user.removePoint();
		player.teleport(arena.getLobby());
		user.maxStats(true);
		arena.getScoreboard().resetScores(ChatColor.AQUA + user.getName());
		arena.getObjective().getScore(user.getName()).setScore(user.getPoint());

		arena.bumpStallingAmount();

		if(arena.isForceStart()) {
			if(user.getPoint() == -1) {
				user.eliminate();

				local.sendMsg(player, local.gamePointsEliminatePlayer);

				for(User u : arena.getUsers()) {
					if(user != u) {
						Language localInstance = playerData.getLanguageOfPlayer(u.getPlayer());
						localInstance.sendMsg(u.getPlayer(),
								localInstance.gamePointsEliminateOthers.replace("%player%", player.getDisplayName()));
					}
				}

				arena.finishGame(false);
			} else {
				arena.nextPlayer();
				local.sendMsg(player, local.gamePointsDownPlayer.replace("%points%", String.valueOf(user.getPoint())));

				for(User u : arena.getUsers()) {
					if(user != u) {
						Language localInstance = playerData.getLanguageOfPlayer(u.getPlayer());
						localInstance.sendMsg(u.getPlayer(),
								localInstance.gamePointsDownOthers.replace("%player%", player.getDisplayName())
										.replace("%points%", String.valueOf(user.getPoint())));
					}
				}
			}
			return;
		}

		if(arena.isSomeoneSurvived()) {

			if(user.getPoint() == -1) {
				// IF someone already succeeded
				// AND damaged player lost his LAST life

				user.eliminate();
				local.sendMsg(player, local.gamePointsEliminatePlayer);

				for(User u : arena.getUsers()) {
					if(user != u) {
						Language localInstance = playerData.getLanguageOfPlayer(u.getPlayer());
						localInstance.sendMsg(u.getPlayer(),
								localInstance.gamePointsEliminateOthers.replace("%player%", player.getDisplayName()));
					}
				}
			} else {
				// IF someone already succeeded
				// AND damaged player lost normal life

				local.sendMsg(player, local.gamePointsDownPlayer.replace("%points%", String.valueOf(user.getPoint())));

				for(User u : arena.getUsers()) {
					if(user != u) {
						Language localInstance = playerData.getLanguageOfPlayer(u.getPlayer());
						localInstance.sendMsg(u.getPlayer(),
								localInstance.gamePointsDownOthers.replace("%player%", player.getDisplayName())
										.replace("%points%", String.valueOf(user.getPoint())));
					}
				}
			}
		} else {
			if(arena.isLastPlayer(user)) {
				if(user.getPoint() == -1) {
					// IF everybody failed this round
					// AND damaged player is last
					// AND damaged player lost his LAST life

					user.addPoint();
					arena.setSomeoneLostFinal(true);

					local.sendMsg(player, local.gamePointsReviveLastLastPlayer);

					for(User p : arena.getUsers()) {
						if(p != user) {
							Language localInstance = playerData.getLanguageOfPlayer(p.getPlayer());
							localInstance.sendMsg(p.getPlayer(), localInstance.gamePointsReviveLastLastOthers
									.replace("%player%", player.getDisplayName()));
						}
					}

				} else if(arena.isSomeoneLostFinal()) {
					// IF everybody failed this round
					// AND damaged player is last
					// AND damaged player lost normal life
					// AND other player lost his LAST life

					user.addWaitingForConfirmation();

					local.sendMsg(player, local.gamePointsReviveLastMultiplePlayer);

					for(User p : arena.getUsers()) {
						if(p != user) {
							Language localInstance = playerData.getLanguageOfPlayer(p.getPlayer());
							localInstance.sendMsg(p.getPlayer(), localInstance.gamePointsReviveLastMultipleOthers
									.replace("%player%", player.getDisplayName()));
						}
					}
				} else {
					// IF everybody failed this round
					// AND damaged player is last
					// AND damaged player lost normal life
					// AND nobody lost his LAST life

					local.sendMsg(player,
							local.gamePointsDownPlayer.replace("%points%", String.valueOf(user.getPoint())));

					for(User p : arena.getUsers()) {
						if(user != p) {
							Language localInstance = playerData.getLanguageOfPlayer(p.getPlayer());
							localInstance.sendMsg(p.getPlayer(),
									localInstance.gamePointsDownOthers.replace("%player%", player.getDisplayName())
											.replace("%points%", String.valueOf(user.getPoint())));
						}
					}

				}
			} else {

				if(user.getPoint() == -1) {
					// IF everybody failed this round
					// AND damaged player NOT last
					// AND damaged player lost his LAST life

					user.addWaitingForConfirmation();
					arena.setSomeoneLostFinal(true);

					local.sendMsg(player, local.gamePointsConfirmationPlayer);

					for(User p : arena.getUsers()) {
						if(p != user) {
							Language localInstance = playerData.getLanguageOfPlayer(p.getPlayer());
							localInstance.sendMsg(p.getPlayer(), localInstance.gamePointsConfirmationOthers
									.replace("%player%", player.getDisplayName()));
						}
					}

				} else {
					// IF everybody failed this round
					// AND damaged player NOT last
					// AND damaged player lost normal life

					user.addWaitingForConfirmation();

					local.sendMsg(player,
							local.gamePointsDownPlayer.replace("%points%", String.valueOf(user.getPoint())));
					local.sendMsg(player, local.gamePointsReviveHint);

					for(User p : arena.getUsers()) {
						if(user != p) {
							Language localInstance = playerData.getLanguageOfPlayer(p.getPlayer());
							localInstance.sendMsg(p.getPlayer(),
									localInstance.gamePointsDownOthers.replace("%player%", player.getDisplayName())
											.replace("%points%", String.valueOf(user.getPoint())));
						}
					}
				}
			}
		}

		if(arena.isLastPlayer(user) && arena.isSomeoneLostFinal()) {
			arena.reviveConfirmationQueue();
		}

		if(arena.isOver()) {
			arena.finishGame(false);
		} else {
			arena.nextPlayer();
		}

	}
}
