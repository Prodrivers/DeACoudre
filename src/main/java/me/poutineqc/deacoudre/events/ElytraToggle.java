package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.instances.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class ElytraToggle implements Listener {

	@EventHandler
	public void onElytraToggle(EntityToggleGlideEvent event) {
		if(!event.isGliding()) {
			return;
		}

		if(!(event.getEntity() instanceof Player player)) {
			return;
		}

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			return;
		}

		event.setCancelled(true);
	}

}
