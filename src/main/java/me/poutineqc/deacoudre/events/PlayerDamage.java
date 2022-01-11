package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.instances.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import javax.inject.Singleton;

@Singleton
public class PlayerDamage implements Listener {
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player player)) {
			return;
		}

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			return;
		}

		event.setCancelled(true);

		if(arena.getGameState() != GameState.ACTIVE) {
			return;
		}

		if(!event.getCause().equals(DamageCause.FALL)) {
			return;
		}

		User user = arena.getUser(player);

		if(user == arena.getActivePlayer()) {
			arena.onJumpFailed( user);
		}
	}
}
