package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleport implements Listener {

	private final PlayerData playerData;

	public PlayerTeleport(DeACoudre plugin) {
		this.playerData = plugin.getPlayerData();
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {

		Player player = event.getPlayer();

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			return;
		}

		if(arena.getGameState() == GameState.ENDING) {
			return;
		}

		if(event.getTo().getWorld() == arena.getWorld()) {
			if((event.getTo().distance(arena.getPlateform())) < 1 || (event.getTo().distance(arena.getLobby())) < 1) {
				return;
			}
		}

		event.setCancelled(true);

		Language local = playerData.getLanguageOfPlayer(player);
		local.sendMsg(player, local.errorTeleport);

	}

}
