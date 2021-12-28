package me.poutineqc.deacoudre.commands;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Log;
import me.poutineqc.deacoudre.Permissions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DacCommand {

	private static ArrayList<DacCommand> commands;
	private static DeACoudre plugin;
	private static File commandFile;
	private static FileConfiguration commandData;
	private String commandName;
	private String description;
	private String permission;
	private String usage;
	private CommandType type;

	public DacCommand(String commandName, String description, String permission, String usage, CommandType type) {
		this.commandName = commandName;
		this.description = description;
		this.permission = permission;
		this.usage = usage;
		this.type = type;
	}

	public DacCommand(DeACoudre plugin) {
		DacCommand.plugin = plugin;

		commandFile = new File(plugin.getDataFolder(), "commands.yml");
		loadCommands();
	}

	private static void loadCommands() {
		InputStream local = plugin.getResource("commands.yml");
		if(local != null) {
			plugin.saveResource("commands.yml", false);
		} else {
			Log.severe("Could not find commands.yml inside the jar file.");
		}

		commandData = YamlConfiguration.loadConfiguration(commandFile);
		commands = new ArrayList<>();

		readingProcess();

		commandFile.delete();
	}

	private static void readingProcess() {
		for(String commandType : commandData.getConfigurationSection("commands").getKeys(false)) {

			CommandType type;
			switch(commandType) {
				case "game":
					type = CommandType.GAME_COMMANDS;
					break;
				case "arena":
					type = CommandType.ARENA_COMMANDS;
					break;
				case "general":
					type = CommandType.GENERAL;
					break;
				case "admin":
					type = CommandType.ADMIN_COMMANDS;
					break;
				default:
					type = CommandType.ALL;
			}

			for(String commandName : commandData.getConfigurationSection("commands." + commandType).getKeys(false)) {
				String description = commandData.getString("commands." + commandType + "." + commandName + ".description");
				String permission = commandData.getString("commands." + commandType + "." + commandName + ".permission");
				String usage = commandData.getString("commands." + commandType + "." + commandName + ".usage");
				commands.add(new DacCommand(commandName, description, permission, usage, type));
			}
		}
	}

	public static ArrayList<DacCommand> getCommands() {
		return commands;
	}

	public static List<DacCommand> getRequiredCommands(Player player, CommandType commandType) {
		List<DacCommand> requestedCommands = new ArrayList<>();

		for(DacCommand cmd : commands) {
			if(cmd.type == commandType || commandType == CommandType.ALL) {
				if(Permissions.hasPermission(player, cmd.permission, false)) {
					requestedCommands.add(cmd);
				}
			}
		}

		return requestedCommands;
	}

	public static DacCommand getCommand(String argument) {
		for(DacCommand command : commands) {
			if(command.commandName.equalsIgnoreCase(argument)) {
				return command;
			}
		}
		return null;
	}

	public String getCommandName() {
		return commandName;
	}

	public String getPermission() {
		return permission;
	}

	public String getDescription() {
		return description;
	}

	public String getUsage() {
		return usage;
	}
}
