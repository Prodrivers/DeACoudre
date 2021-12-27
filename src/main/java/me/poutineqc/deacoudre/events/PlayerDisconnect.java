package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDisconnect implements Listener {

	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent event) {

		Player player = event.getPlayer();

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			return;
		}

		User user = arena.getUser(player);
		arena.removeUserFromGame(user, true);
	}
}
