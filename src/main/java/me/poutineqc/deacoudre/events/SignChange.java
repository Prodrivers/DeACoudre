package me.poutineqc.deacoudre.events;

import me.poutineqc.deacoudre.*;
import me.poutineqc.deacoudre.commands.DacSign;
import me.poutineqc.deacoudre.commands.SignType;
import me.poutineqc.deacoudre.instances.Arena;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignChange implements Listener {

	private final Configuration config;
	private final PlayerData playerData;

	public SignChange(DeACoudre plugin, Language local) {
		this.config = plugin.getConfiguration();
		this.playerData = plugin.getPlayerData();
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		Language local = playerData.getLanguage(config.language);

		if(isPrefixInLine(
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getLine(0))).toLowerCase(), local)) {
			if(!Permissions.hasPermission(e.getPlayer(), Permissions.permissionMakeSigns, false)) {
				setSignNoPermissions(e, local);
				return;
			}

			if(e.getLine(1).equalsIgnoreCase("join")) {
				Arena arena = Arena.getArenaFromName(e.getLine(2));
				if(arena != null) {
					new DacSign(e, SignType.JOIN);
				} else {
					setNoValidSign(e, local);
				}
			} else if(e.getLine(1).equalsIgnoreCase("play")) {
				Arena arena = Arena.getArenaFromName(e.getLine(2));
				if(arena != null) {
					if(arena.getWorld() == null) {
						setNoValidSign(e, local);
					} else if(arena.getWorld() != e.getBlock().getWorld()) {
						setNoValidSign(e, local);
					} else {
						new DacSign(e, SignType.PLAY);
					}
				} else {
					setNoValidSign(e, local);
				}
			} else if(e.getLine(1).equalsIgnoreCase("quit")) {
				new DacSign(e, SignType.QUIT);

			} else if(e.getLine(1).equalsIgnoreCase("color")) {
				new DacSign(e, SignType.COLOR);

			} else if(e.getLine(1).equalsIgnoreCase("start")) {
				new DacSign(e, SignType.START);

			} else if(e.getLine(1).equalsIgnoreCase("stats")) {
				new DacSign(e, SignType.STATS);

			} else {
				setNoValidSign(e, local);
			}
		} else if(isPrefixInLine(
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getLine(1))).toLowerCase(), local)
				|| isPrefixInLine(
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getLine(2))).toLowerCase(),
				local)
				|| isPrefixInLine(
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getLine(3))).toLowerCase(),
				local)) {

			if(Permissions.hasPermission(e.getPlayer(), Permissions.permissionMakeSigns, false)) {
				if(!Permissions.hasPermission(e.getPlayer(), Permissions.permissionMakeSigns, false)) {
					setSignNoPermissions(e, local);
					return;
				}

				setNoValidSign(e, local);
			}
		}

	}

	private boolean isPrefixInLine(String line, Language local) {
		return line.contains("[dac]")
				|| line.contains(ChatColor
				.stripColor(ChatColor.translateAlternateColorCodes('&', local.prefixLong.toLowerCase())))
				|| line.contains(ChatColor
				.stripColor(ChatColor.translateAlternateColorCodes('&', local.prefixShort.toLowerCase())));
	}

	private void setSignNoPermissions(SignChangeEvent e, Language local) {
		e.setLine(0, ChatColor.translateAlternateColorCodes('&', local.signNoPermission0));
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signNoPermission1));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signNoPermission2));
		e.setLine(3, ChatColor.translateAlternateColorCodes('&', local.signNoPermission3));
	}

	private void setNoValidSign(SignChangeEvent e, Language local) {
		e.setLine(0, ChatColor.translateAlternateColorCodes('&', local.prefixLong.trim()));
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signNotValid1));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signNotValid2));
		e.setLine(3, ChatColor.translateAlternateColorCodes('&', local.signNotValid3));
	}
}
