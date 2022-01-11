package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.instances.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import javax.inject.Singleton;

@Singleton
public class PlayerMove implements Listener {
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		final Player player = event.getPlayer();

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

		arena.onJumpInPool(user, getTo);
	}
}
