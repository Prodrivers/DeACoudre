package me.poutineqc.deacoudre.guis;

import java.util.Optional;

import me.poutineqc.deacoudre.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.tools.ColorManager;
import me.poutineqc.deacoudre.tools.ItemStackManager;

public class SetArenaBlocksGUI implements Listener {
	private PlayerData playerData;
	private Configuration config;

	public SetArenaBlocksGUI(DeACoudre plugin) {
		this.playerData = plugin.getPlayerData();
		this.config = plugin.getConfiguration();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) event.getWhoClicked();
		Language local = playerData.getLanguageOfPlayer(player);

		if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase(ChatColor
				.stripColor(ChatColor.translateAlternateColorCodes('&', local.editColorGuiTitle))))
			return;

		if (event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.UNKNOWN)
			return;

		event.setCancelled(true);

		ItemStack item = event.getCurrentItem();

		Arena arena = Arena
				.getArenaFromName(ChatColor.stripColor(event.getInventory().getItem(0).getItemMeta().getLore().get(0)));
		ColorManager colorManager = arena.getColorManager();

		boolean isChoosable = colorManager.isArenaBlockChoosableByPlayers(item);
		if (isChoosable && colorManager.getArenaBlocks().size() <= arena.getMaxPlayer()) {
			local.sendMsg(player, local.editColorColorLessPlayer);
			openColorGUI(player, arena);
			return;
		}

		if (arena.getGameState() == GameState.UNREADY) {
			if (arena.getMinPoolPoint() == null || arena.getMaxPoolPoint() == null) {
				player.closeInventory();
				local.sendMsg(player, local.editColorNoPool);
				return;
			}
		}

		if (arena.getGameState() != GameState.READY && arena.getGameState() != GameState.UNREADY) {
			player.closeInventory();
			local.sendMsg(player, local.editColorActive);
			return;
		}

		Optional<ItemStackManager> correspondingArenaItem = arena.getColorManager().getBlock(item);
		if(correspondingArenaItem.isEmpty()) {
			return;
		}

		if (!correspondingArenaItem.get().isAvailable()) {
			player.closeInventory();
			local.sendMsg(player, local.editColorChoosen);
			return;
		}


		int valueOfItem = config.usableBlocks.indexOf(item.getType());
		if(valueOfItem == -1)
			return;

		colorManager.setAsArenaBlock(item, !isChoosable);

		arena.resetArena(item);
		openColorGUI(player, arena);
	}

	public void openColorGUI(Player player, Arena arena) {
		Language local = playerData.getLanguageOfPlayer(player);

		Inventory inv = Bukkit.createInventory(null, 6*9,
				ChatColor.translateAlternateColorCodes('&', local.editColorGuiTitle));
		ItemStackManager icon;
		/***************************************************
		 * Instructions
		 ***************************************************/

		icon = new ItemStackManager(Material.BOOKSHELF, 4);
		icon.setTitle(local.keyWordGuiInstrictions);
		for (String loreLine : local.editColorGuiTooltip.split("\n"))
			icon.addToLore(loreLine);
		icon.addToInventory(inv);

		/***************************************************
		 * Blocks
		 ***************************************************/
		int i = 9; // Offset by one line as it is already occupied
		for (ItemStackManager item : arena.getColorManager().getAllAuthorizedGameBlocks()) {
			if(i >= 6*9) {
				// Do not go over 6 lines
				break;
			}
			item.setPosition(i);
			item.addToInventory(inv);
			i++;
		}

		/***************************************************
		 * ArenaName
		 ***************************************************/

		icon = new ItemStackManager(Material.PAPER);
		icon.setTitle("&eArena:");
		icon.addToLore("&f" + arena.getName());

		icon.setPosition(0);
		icon.addToInventory(inv);
		icon.setPosition(8);
		icon.addToInventory(inv);

		/***************************************************
		 * Display
		 ***************************************************/

		player.openInventory(inv);
	}
}