package me.poutineqc.deacoudre.sections;

import fr.prodrivers.bukkit.commons.sections.Section;
import fr.prodrivers.bukkit.commons.sections.SectionCapabilities;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.Log;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Set;

public class ArenaSection extends Section {
	private static final String DAC_ARENA_SECTION_PREFIX = "dac.";

	private final PlayerData playerData;
	private final Arena arena;

	public ArenaSection(@NonNull PlayerData playerData, @NonNull Arena arena) {
		super(DAC_ARENA_SECTION_PREFIX + arena.getName());

		this.playerData = playerData;
		this.arena = arena;
	}

	@Override
	public @NonNull Set<SectionCapabilities> getCapabilities() {
		return Collections.emptySet();
	}

	@Override
	public boolean preJoin(@NonNull Player player, Section section, boolean fromParty) {
		Log.finest("Player wants to join arena " + arena.getName());

		Language local = playerData.getLanguageOfPlayer(player);

		if(arena.getGameState() == GameState.UNREADY) {
			local.sendMsg(player, local.joinStateUnset);
			Log.warning("Player wanted to join arena " + arena.getName()+ ", but is not ready.");
			return false;
		}

		return true;
	}

	@Override
	public boolean join(@NonNull Player player) {
		return arena.addPlayerToTeam(player);
	}

	@Override
	public boolean preLeave(@NonNull OfflinePlayer offlinePlayer, Section section, boolean fromParty) {
		Player player = offlinePlayer.getPlayer();

		if(player == null) {
			Log.warning("Player wanted to leave arena " + arena.getName()+ ", but is offline.");
			return true;
		}

		Arena playerArena = Arena.getArenaFromPlayer(player);
		if(playerArena != arena) {
			Language local = playerData.getLanguageOfPlayer(player);
			local.sendMsg(player, local.errorNotInGame);
			Log.warning("Player wanted to leave arena " + arena.getName()+ ", but is not in this arena.");
			return false;
		}

		return true;
	}

	@Override
	public boolean leave(@NonNull OfflinePlayer offlinePlayer) {
		Player player = offlinePlayer.getPlayer();

		if(player == null) {
			Log.warning("Player wanted to leave arena " + arena.getName()+ ", but is offline.");
			return true;
		}

		return arena.removePlayerFromGame(offlinePlayer.getPlayer());
	}
}
