package me.poutineqc.deacoudre.commands;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Log;
import me.poutineqc.deacoudre.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class DaCCommandDescription {
	private static Map<String, DaCCommandDescription> commands;
	private static DeACoudre plugin;
	private static File commandFile;
	private static FileConfiguration commandData;
	private final String commandName;
	private final String description;
	private final String permission;
	private final String usage;
	private final List<String> arguments;
	private final CommandType type;
	private final boolean isPlayerOnly;

	public DaCCommandDescription(String commandName, String description, String permission, String usage, CommandType type, List<String> arguments, boolean isPlayerOnly) {
		this.commandName = commandName;
		this.description = description;
		this.permission = permission;
		this.usage = usage;
		this.type = type;
		this.arguments = arguments;
		this.isPlayerOnly = isPlayerOnly;
	}

	public static void init(DeACoudre plugin) {
		DaCCommandDescription.plugin = plugin;

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
		commands = new HashMap<>();

		readingProcess();

		commandFile.delete();
	}

	private static void readingProcess() {
		for(String commandType : commandData.getConfigurationSection("commands").getKeys(false)) {
			CommandType type = switch(commandType) {
				case "game" -> CommandType.GAME;
				case "arena" -> CommandType.ARENA;
				case "general" -> CommandType.GENERAL;
				case "admin" -> CommandType.ADMIN;
				default -> CommandType.ALL;
			};

			for(String commandName : commandData.getConfigurationSection("commands." + commandType).getKeys(false)) {
				String description = commandData.getString("commands." + commandType + "." + commandName + ".description");
				String permission = commandData.getString("commands." + commandType + "." + commandName + ".permission");
				String usage = commandData.getString("commands." + commandType + "." + commandName + ".usage");
				List<String> arguments = commandData.getStringList("commands." + commandType + "." + commandName + ".arguments");
				boolean isPlayerOnly = commandData.getBoolean("commands." + commandType + "." + commandName + ".playeronly");
				commands.put(commandName, new DaCCommandDescription(commandName, description, permission, usage, type, arguments, isPlayerOnly));
			}
		}
	}

	public static Collection<DaCCommandDescription> getCommands() {
		return commands.values();
	}

	public static List<DaCCommandDescription> getRequiredCommands(CommandSender sender, CommandType commandType) {
		return commands.values().stream()
				.filter(cmd -> cmd.type == commandType || commandType == CommandType.ALL)
				.filter(cmd -> Permissions.hasPermission(sender, cmd.permission, false))
				.filter(cmd -> cmd.canExecute(sender))
				.collect(Collectors.toList());
	}

	public static DaCCommandDescription getCommand(String argument) {
		return commands.get(argument);
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

	public CommandType getType() {
		return type;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public boolean canExecute(CommandSender sender) {
		return !isPlayerOnly || sender instanceof Player;
	}
}
