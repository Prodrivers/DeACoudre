package me.poutineqc.deacoudre.ui;

import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.User;
import me.poutineqc.deacoudre.tools.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class ArenaUI {
	private final PlayerData playerData;
	private final Configuration configuration;

	@Inject
	public ArenaUI(PlayerData playerData, Configuration configuration) {
		this.playerData = playerData;
		this.configuration = configuration;
	}

	public void onPlayerJoined(User user, Arena arena) {
		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());
		String playerDisplayName = user.getDisplayName();
		String nonEliminated = String.valueOf(arena.getNonEliminated().size());

		locale.sendMsg(
				user.getPlayer(),
				locale.joinGamePlayer
					.replace("%arenaName%", arena.getDisplayName())
					.replace("%amountInGame%", nonEliminated)
		);

		for(User u : arena.getUsers()) {
			if(u != user) {
				Language localInstance = playerData.getLanguageOfPlayer(u);
				localInstance.sendMsg(
						u,
						localInstance.joinGameOthers
								.replace("%player%", playerDisplayName)
								.replace("%amountInGame%", nonEliminated)
				);
			}
		}
	}

	public void onPlayerJoinedUnsetArena(Player player) {
		Language locale = playerData.getLanguageOfPlayer(player);

		locale.sendMsg(player, locale.joinStateUnset);
	}

	public void onPlayerJoinedActiveOrEndingArena(Player player) {
		Language locale = playerData.getLanguageOfPlayer(player);

		locale.sendMsg(player, locale.joinStateStarted);
		locale.sendMsg(player, locale.joinAsSpectator);
	}

	public void onPlayerJoinedNonStartedFullGame(Player player) {
		Language locale = playerData.getLanguageOfPlayer(player);

		locale.sendMsg(player, locale.joinStateFull);
		locale.sendMsg(player, locale.joinAsSpectator);
	}

	public void onPlayerJoinedActiveGameBecauseOfLeaver(User leaver, User joiner, Arena arena) {
		String leaverDisplayName = leaver.getDisplayName();
		String joinerDisplayName = joiner.getDisplayName();

		Language local = playerData.getLanguageOfPlayer(joiner);

		local.sendMsg(joiner, local.joinNewPlacePlayer.replace("%leaver%", leaverDisplayName));

		for(User u : arena.getUsers()) {
			if(u != joiner) {
				Language localTemp = playerData.getLanguageOfPlayer(u);
				localTemp.sendMsg(
						u.getPlayer(),
						localTemp.joinNewPlaceOthers
								.replace("%player%", joinerDisplayName)
								.replace("%leaver%", leaverDisplayName)
				);
			}
		}
	}

	public void onPlayerQuitRunningGame(User user, Arena arena) {
		String playerDisplayName = user.getDisplayName();

		Language locale = playerData.getLanguageOfPlayer(user.getPlayer());

		locale.sendMsg(user.getPlayer(), locale.quitGamePlayer);

		for(User u : arena.getUsers()) {
			if(user != u) {
				Language localTemp = playerData.getLanguageOfPlayer(u);
				localTemp.sendMsg(
						u.getPlayer(),
						localTemp.quitGameOthers.replace("%player%", playerDisplayName)
				);
			}
		}
	}
	
	public void onAutoStartFailed(Player player) {
		Language locale = playerData.getLanguageOfPlayer(player);

		locale.sendMsg(player, locale.startAutoFail);
	}
	
	public void onLobbyTeleportFailed(Player player) {
		playerData.getLanguageOfPlayer(player).sendMsg(
				player,
				ChatColor.RED + "Error: Could not teleport you to the lobby. Failed to join the game."
		);
	}

	public void onCountdownStarted(Arena arena) {
		if(configuration.broadcastStart) {
			String arenaDisplayName = arena.getDisplayName();
			String countdownTime = String.valueOf(configuration.countdownTime);

			for(Player p : Bukkit.getOnlinePlayers()) {
				Language localInstance = playerData.getLanguageOfPlayer(p);
				localInstance.sendMsg(
						p,
						localInstance.startBroadcast
								.replaceAll("%arena%", arenaDisplayName)
								.replace("%time%", countdownTime)
				);
			}
		}
	}

	public void onCountdownStep(Arena arena, int timeInTick) {
		int level = (int) Math.floor(timeInTick / 20.0) + 1;

		for(User user : arena.getUsers()) {
			user.getPlayer().setLevel(level);
			user.getPlayer().setExp((float) ((timeInTick % 20) / 20.0));
		}

		switch(timeInTick / 20) {
			case 30:
			case 10:
			case 5:
			case 4:
			case 3:
			case 2:
			case 1:
				if(timeInTick % 20.0 == 0) {
					for(User user : arena.getUsers()) {
						user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

						Language locale = playerData.getLanguageOfPlayer(user);

						Utils.sendTitle(
								user.getPlayer(),
								Component.text(String.valueOf(timeInTick / 20), NamedTextColor.GOLD, TextDecoration.BOLD),
								Component.text(String.valueOf(locale.keyWordGeneralSeconds), NamedTextColor.DARK_GRAY, TextDecoration.ITALIC),
								5,
								10,
								5
						);
					}
				}
				break;
		}
	}

	public void onCountdownCancelled(Player player) {
		Language locale = playerData.getLanguageOfPlayer(player);
		locale.sendMsg(player, locale.startStopped);
	}

	public void onArenaStart(Arena arena) {
		Language locale = Language.getDefaultLanguage();
		arena.getObjective().setDisplayName(ChatColor.translateAlternateColorCodes('&',
				ChatColor.AQUA + arena.getDisplayName() + " &f: " + locale.keyWordScoreboardPoints));
		arena.getObjective().getScore(ChatColor.GOLD + "-------------------").setScore(98);
		arena.getObjective().getScoreboard().resetScores(ChatColor.GOLD + locale.keyWordGeneralMinimum + " = " + ChatColor.AQUA
				+ arena.getMinPlayer());
		arena.getObjective().getScoreboard().resetScores(ChatColor.GOLD + locale.keyWordGeneralMaximum + " = " + ChatColor.AQUA
				+ arena.getMaxPlayer());
	}

	public void onArenaNonForcedStart(Arena arena) {
		for(User user : arena.getUsers()) {
			Language locale = playerData.getLanguageOfPlayer(user);
			locale.sendMsg(user, locale.startRandomOrder);
		}
	}

	public void onNewRound(Arena arena, int previousRoundNumber) {
		Language l = Language.getDefaultLanguage();

		arena.getScoreboard().resetScores(
				ChatColor.GOLD + l.keyWordScoreboardRound + " = " + ChatColor.AQUA + previousRoundNumber);
		previousRoundNumber++;
		arena.getObjective().getScore(ChatColor.GOLD + l.keyWordScoreboardRound + " = " + ChatColor.AQUA + previousRoundNumber)
				.setScore(99);

		if(configuration.verbose) {
			String round = String.valueOf(previousRoundNumber);
			for(User p : arena.getUsers()) {
				Language locale = playerData.getLanguageOfPlayer(p);
				locale.sendMsg(p.getPlayer(), locale.gameNewRound.replace("%round%", round));
			}
		}
	}

	public void onPlayerChange(User activeUser, Arena arena) {
		Language locale;
		try {
			locale = playerData.getLanguageOfPlayer(activeUser);
		} catch(NullPointerException e) {
			return;
		}

		if(configuration.verbose) {
			String activeUserName = activeUser.getDisplayName();
			locale.sendMsg(activeUser, locale.gameTurnPlayer);

			for(User user : arena.getUsers()) {
				if(activeUser != user) {
					Language localInstance = playerData.getLanguageOfPlayer(user);
					localInstance.sendMsg(
							user,
							localInstance.gameTurnOthers.replace("%player%", activeUserName)
					);
				}
			}
		}

		arena.getScoreboard().resetScores(activeUser.getName());
		arena.getObjective().getScore(ChatColor.AQUA + activeUser.getName()).setScore(activeUser.getPoint());

		Utils.sendTitle(activeUser.getPlayer(), Component.text(locale.keyWordJumpFast, NamedTextColor.GOLD, TextDecoration.BOLD), null, 5, 10, 5);
	}

	public void onFinishSingleNonEliminated(Arena arena) {
		String arenaDisplayName = arena.getDisplayName();

		for(Player p : arena.getBroadcastCongratulationList()) {
			Language locale = playerData.getLanguageOfPlayer(p);
			locale.sendMsg(
					p,
					locale.endingBroadcastSingle
							.replace("%player%", arena.getNonEliminated().get(0).getPlayer().getDisplayName())
							.replace("%arenaName%", arenaDisplayName)
			);
		}
	}

	public void onFinishCompleted(Arena arena) {
		String arenaDisplayName = arena.getDisplayName();

		for(Player player : arena.getBroadcastCongratulationList()) {
			Language locale = playerData.getLanguageOfPlayer(player);
			locale.sendMsg(
					player,
					locale.endingBroadcastMultiple
							.replace("%players%", arena.getPlayerListToDisplay(locale))
							.replace("%arenaName%", arenaDisplayName)
			);
		}
	}

	public void onFinishNonCompleted(Arena arena) {
		String arenaDisplayName = arena.getDisplayName();

		for(User user : arena.getUsers()) {
			Language localInstance = playerData.getLanguageOfPlayer(user);
			localInstance.sendMsg(user.getPlayer(), localInstance.endingStall.replace("%time%",
					String.valueOf(configuration.maxFailBeforeEnding)));
		}

		for(Player player : arena.getBroadcastCongratulationList()) {
			Language localInstance = playerData.getLanguageOfPlayer(player);
			localInstance.sendMsg(
					player,
					localInstance.endingBroadcastSingle
							.replace("%player%", arena.getNonEliminated().get(0).getDisplayName())
							.replace("%arenaName%", arenaDisplayName)
			);
		}
	}

	public void onForceStartedArenaFinished(Arena arena) {
		for(User user : arena.getUsers()) {
			Language locale = playerData.getLanguageOfPlayer(user);
			locale.sendMsg(user, locale.endingSimulation);
		}
	}

	public void onArenaFinishedPlayerTeleported(Arena arena) {
		for(User u : arena.getUsers()) {
			Language locale = playerData.getLanguageOfPlayer(u);
			locale.sendMsg(u, locale.endingTeleport);
		}
	}
}
