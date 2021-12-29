package me.poutineqc.deacoudre.guis;

import fr.prodrivers.bukkit.commons.sections.SectionManager;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.tools.ItemStackManager;
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

import java.util.ArrayList;
import java.util.List;

public class JoinGUI implements Listener {
	private final PlayerData playerData;
	private final SectionManager sectionManager;

	public JoinGUI(DeACoudre plugin) {
		this.playerData = plugin.getPlayerData();
		this.sectionManager = plugin.getSectionManager();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();

		if(event.getAction().equals(InventoryAction.NOTHING) || event.getAction().equals(InventoryAction.UNKNOWN)) {
			return;
		}

		Language local = playerData.getLanguageOfPlayer(player);

		if(!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase(
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.joinGuiTitle)))) {
			return;
		}

		event.setCancelled(true);

		ItemStack item = event.getCurrentItem();
		String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

		if(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordGuiNextPage))
				.equalsIgnoreCase(itemName)) {
			player.closeInventory();
			openJoinGui(player, Integer.parseInt(item.getItemMeta().getLore().get(0)));
			return;
		}

		if(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordGuiPreviousPage))
				.equalsIgnoreCase(itemName)) {
			player.closeInventory();
			openJoinGui(player, Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(0))));
			return;
		}

		Arena arena = Arena.getArenaFromName(itemName);
		if(arena == null) {
			return;
		}

		if(event.getAction() == InventoryAction.PICKUP_HALF) {
			player.closeInventory();
			arena.displayInformation(player);
			return;
		}

		sectionManager.enter(player, arena.getFullSectionName());

		player.closeInventory();
	}

	public void openJoinGui(Player player, int page) {
		Language local = playerData.getLanguageOfPlayer(player);

		List<String> Arenas = new ArrayList<>();
		for(Arena arena : Arena.getArenas()) {
			Arenas.add(arena.getName());
		}
		java.util.Collections.sort(Arenas);

		if((page - 1) * 36 > 0) {
			Arenas.subList(0, (page - 1) * 36).clear();
		}

		int size;
		if(Arenas.size() > 36) {
			size = 54;
		} else {
			size = (int) (Math.ceil((Arenas.size() + 18.0) / 9.0) * 9.0);
		}

		Inventory inv = Bukkit.createInventory(null, size,
				ChatColor.translateAlternateColorCodes('&', local.joinGuiTitle));
		ItemStackManager icon;

		/***************************************************
		 * Main Item
		 ***************************************************/

		icon = new ItemStackManager(Material.BOOKSHELF, 4);
		icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.keyWordGuiInstrictions));
		for(String s : local.joinGuiTooltip.split("\n")) {
			icon.addToLore(s);
		}
		inv = icon.addToInventory(inv);

		/***************************************************
		 * Previous Page
		 ***************************************************/

		if(page > 1) {
			icon = new ItemStackManager(Material.ARROW, 7);
			icon.setTitle(local.keyWordGuiPreviousPage);
			icon.addToLore(String.valueOf(page - 1));
			inv = icon.addToInventory(inv);
		}

		/***************************************************
		 * Glass Separator
		 ***************************************************/

		icon = new ItemStackManager(Material.BLUE_STAINED_GLASS_PANE);
		icon.setTitle(" ");

		for(int i = 0; i < inv.getSize(); i++) {
			switch(i) {
				case 9, 10, 11, 12, 13, 14, 15, 16, 17 -> {
					icon.setPosition(i);
					inv = icon.addToInventory(inv);
				}
			}
		}

		/***************************************************
		 * arenas
		 ***************************************************/

		int slot = 18;

		for(String s : Arenas) {
			Arena arena = Arena.getArenaFromName(s);

			if(!arena.isAllSet()) {
				icon = new ItemStackManager(Material.GRAY_DYE);
				icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateUnset));

			} else if(arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
				icon = new ItemStackManager(Material.RED_DYE);
				icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateStarted));

			} else if(arena.getUsers().size() >= arena.getMaxPlayer()) {
				icon = new ItemStackManager(Material.RED_DYE);
				icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateFull));
				icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers)
						+ ChatColor.DARK_GRAY + " : " + arena.getNonEliminated().size() + "/"
						+ arena.getMaxPlayer());

			} else {
				icon = new ItemStackManager(Material.GREEN_DYE);
				icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateReady));
				icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers)
						+ ChatColor.DARK_GRAY + " : " + arena.getNonEliminated().size() + "/"
						+ arena.getMaxPlayer());
			}

			icon.setTitle(ChatColor.GOLD + s);

			icon.setPosition(slot++);
			icon.addToInventory(inv);

			/***************************************************
			 * NextPage
			 ***************************************************/

			if(slot == 54 && Arenas.size() > 36) {
				icon = new ItemStackManager(Material.ARROW, 8);
				icon.setTitle(local.keyWordGuiNextPage);
				icon.addToLore(String.valueOf(page + 1));
				inv = icon.addToInventory(inv);
				break;
			}
		}
		player.openInventory(inv);
	}
}
