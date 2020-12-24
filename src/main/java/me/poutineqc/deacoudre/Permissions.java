package me.poutineqc.deacoudre;

import org.bukkit.entity.Player;

import me.poutineqc.deacoudre.commands.DacCommand;

public class Permissions {

	private static DeACoudre plugin;

	public Permissions(DeACoudre plugin) {
		Permissions.plugin = plugin;
	}

	public static final String PermissionMultiplier = "dacreward.multiplier.x";
	
	public static final String permissionMakeSigns = "dac.admin.makesigns";
	public static final String permissionAdvancedInfo = "dac.admin.info";

	public static boolean hasPermission(Player player, DacCommand command, boolean warning) {
		return hasPermission(player, command.getPermission(), warning);
	}
	
	public static boolean hasPermission(Player player, String permission, boolean warning) {
		if (player.hasPermission(permission)) {
			return true;
		} else {
			if (warning) {
				Language local = plugin.getPlayerData().getLanguageOfPlayer(player);
				local.sendMsg(player, local.errorNoPermission);
			}
			return false;
		}
	}

}
