package me.poutineqc.deacoudre.guis;

import java.util.List;

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

public class ChooseColorGUI implements Listener {

	private PlayerData playerData;

	public ChooseColorGUI(DeACoudre plugin) {
		this.playerData = plugin.getPlayerData();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) event.getWhoClicked();
		Language local = playerData.getLanguageOfPlayer(player);

		if (!ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase(ChatColor
				.stripColor(ChatColor.translateAlternateColorCodes('&', local.editColorGuiTitle))))
			return;

		if (event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.UNKNOWN)
			return;

		event.setCancelled(true);

		ItemStack item = event.getCurrentItem();
		if (item.getType() != Material.STAINED_CLAY && item.getType() != Material.WOOL)
			return;

		Arena arena = Arena
				.getArenaFromName(ChatColor.stripColor(event.getInventory().getItem(0).getItemMeta().getLore().get(0)));
		ColorManager colorManager = arena.getColorManager();

		boolean enchanted = item.getItemMeta().hasEnchants();
		if (enchanted && colorManager.getOnlyChoosenBlocks().size() <= arena.getMaxPlayer()) {
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
		
		if (arena.getColorManager().isBlockUsed(item)) {
			player.closeInventory();
			local.sendMsg(player, local.editColorChoosen);
			return;
		}

		int valueOfItem = item.getDurability();
		if (item.getType() == Material.STAINED_CLAY)
			valueOfItem += 16;

		if (enchanted)
			colorManager.setColorIndice(arena.getColorManager().getColorIndice() - (int) Math.pow(2, valueOfItem));
		else
			colorManager.setColorIndice(arena.getColorManager().getColorIndice() + (int) Math.pow(2, valueOfItem));

		arena.resetArena(item);
		openColorGUI(player, arena);
	}

	public void openColorGUI(Player player, Arena arena) {
		Language local = playerData.getLanguageOfPlayer(player);

		Inventory inv = Bukkit.createInventory(null, 54,
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
		 * Glass Spacer
		 ***************************************************/

		icon = new ItemStackManager(Material.STAINED_GLASS_PANE);
		icon.setData((short) 10);
		icon.setTitle(" ");

		for (int i = 0; i < inv.getSize(); i++)
			switch (i) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 27:
			case 36:
			case 45:
				icon.setPosition(i);
				icon.addToInventory(inv);
			}

		/***************************************************
		 * Blocks
		 ***************************************************/

		List<ItemStackManager> colorManager = arena.getColorManager().getAllBlocks();
		for (int i = 0; i < 32; i++) {
			ItemStackManager item = colorManager.get(i);
			item.setPosition((int) ((Math.floor(i / 8.0) * 9) + 19 + (i % 8)));
			item.addToInventory(inv);
		}

		/***************************************************
		 * ArenaNAme
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