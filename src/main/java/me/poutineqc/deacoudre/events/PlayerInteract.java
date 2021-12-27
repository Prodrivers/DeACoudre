package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.achievements.AchievementsGUI;
import me.poutineqc.deacoudre.commands.DaC;
import me.poutineqc.deacoudre.commands.DacCommand;
import me.poutineqc.deacoudre.commands.DacSign;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

	private final AchievementsGUI achievementsGUI;
	private final DaC dac;

	public PlayerInteract(DeACoudre plugin, Language local) {
		this.achievementsGUI = plugin.getAchievementsGUI();
		this.dac = plugin.getDAC();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}

		if(!(e.getClickedBlock().getState() instanceof Sign s)) {
			return;
		}

		DacSign dacsign = DacSign.getDacSign(s.getLocation());
		if(dacsign == null) {
			return;
		}

		e.setCancelled(true);
		Player player = e.getPlayer();

		switch(dacsign.getSignType()) {
			case COLOR -> dac.openColorGUI(DacCommand.getCommand("color"), player);
			case JOIN -> dac.commandJoin(DacCommand.getCommand("join"), player, 2, s.getLine(2), true);
			case PLAY -> dac.commandJoin(DacCommand.getCommand("join"), player, 2, s.getLine(2), false);
			case QUIT -> dac.quitGame(DacCommand.getCommand("quit"), player);
			case START -> dac.startGame(DacCommand.getCommand("start"), player);
			case STATS -> achievementsGUI.openStats(player);
		}
	}
}
