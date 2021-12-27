package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.commands.DacSign;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(!(event.getBlock().getState() instanceof Sign sign)) {
			return;
		}

		DacSign dacSign = DacSign.getDacSign(sign.getLocation());
		if(dacSign != null) {
			DacSign.removeSign(dacSign);
		}
	}

}
