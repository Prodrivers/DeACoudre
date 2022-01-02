package me.poutineqc.deacoudre;

import me.poutineqc.deacoudre.commands.DaCCommandDescription;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Permissions {

	public static final String PermissionMultiplier = "dacreward.multiplier.x";
	public static final String permissionMakeSigns = "dac.admin.makesigns";
	public static final String permissionAdvancedInfo = "dac.admin.info";
	private static DeACoudre plugin;

	public Permissions(DeACoudre plugin) {
		Permissions.plugin = plugin;
	}

	public static boolean hasPermission(CommandSender sender, DaCCommandDescription command, boolean warning) {
		return hasPermission(sender, command.getPermission(), warning);
	}

	public static boolean hasPermission(CommandSender sender, String permission, boolean warning) {
		if(sender.hasPermission(permission)) {
			return true;
		} else {
			if(warning) {
				Language local;
				if(sender instanceof Player player) {
					local = plugin.getPlayerData().getLanguageOfPlayer(player);
				} else {
					local = Language.getDefaultLanguage();
				}
				local.sendMsg(sender, local.errorNoPermission);
			}
			return false;
		}
	}
}
