package me.poutineqc.deacoudre.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;

public class AsyncPlayerChat implements Listener {

	private DeACoudre plugin;

	public AsyncPlayerChat(DeACoudre plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		if (!plugin.getConfiguration().chatRooms)
			return;

		Player player = e.getPlayer();

		Arena arena = Arena.getArenaFromPlayer(player);
		if (arena == null) {
			for (Player p : Arena.getAllPlayersInStartedGame())
				e.getRecipients().remove(p);
			return;
		}

		if (arena.getGameState() != GameState.ACTIVE) {
			for (Player p : Arena.getAllPlayersInStartedGame())
				e.getRecipients().remove(p);
			return;
		}

		for (Player p : Arena.getAllOutsideGame(arena))
			e.getRecipients().remove(p);

	}

}
