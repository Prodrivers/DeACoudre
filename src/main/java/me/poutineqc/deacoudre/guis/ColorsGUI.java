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
import me.poutineqc.deacoudre.achievements.Achievement;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.User;
import me.poutineqc.deacoudre.tools.ItemStackManager;

public class ColorsGUI implements Listener {

	private PlayerData playerData;
	private Achievement achievements;

	public ColorsGUI(DeACoudre plugin) {
		this.playerData = plugin.getPlayerData();
		this.achievements = plugin.getAchievement();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();

		if (event.getAction().equals(InventoryAction.NOTHING) || event.getAction().equals(InventoryAction.UNKNOWN))
			return;

		Language local = playerData.getLanguageOfPlayer(player);

		if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase(
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.colorGuiTitle))))
			return;

		event.setCancelled(true);
		Arena arena = Arena.getArenaFromPlayer(player);
		if (arena == null)
			return;

		User user = arena.getUser(player);

		ItemStack item = event.getCurrentItem();
		String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
		if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.colorGuiCurrent))
				.equalsIgnoreCase(itemName))
			return;

		if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordColorRandom))
				.equalsIgnoreCase(itemName)) {
			user.removeColor();
			local.sendMsg(player, local.colorRandom);
			player.closeInventory();
		}

		if (arena.getColorManager().isBlockUsed(item)) {
			local.sendMsg(player, local.colorAlreadyPicked);
			achievements.testAchievement(Achievement.colorRivalery, player);
		} else {
			user.setColor(item);
			local.sendMsg(player,
					local.colorChoosen
							.replace("%material%",
									arena.getColorManager().getBlockMaterialName(user.getItemStack(), local))
							.replace("%color%", arena.getColorManager().getBlockColorName(user.getItemStack(), local)));
		}

		player.closeInventory();

	}

	public static void openColorsGui(Player player, Language local, Arena arena) {
		ItemStack userCurrentItem = arena.getUser(player).getItemStack();
		Inventory inv;
		ItemStackManager icon;
		List<ItemStackManager> availableBlocks = arena.getColorManager().getAvailableBlocks();
		int size = (availableBlocks.size() % 9 == 0 ? availableBlocks.size() : availableBlocks.size() / 9 * 9 + 9);

		inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', local.colorGuiTitle));

		/***************************************************
		 * Current Item
		 ***************************************************/

		if (userCurrentItem == null) {
			icon = new ItemStackManager(Material.PLAYER_HEAD);
			icon.setPlayerHeadName("azbandit2000");
			icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.colorGuiCurrent));
			icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.keyWordColorRandom));

		} else {
			icon = new ItemStackManager(userCurrentItem.getType());
			icon.addToLore(ChatColor.translateAlternateColorCodes('&',
					arena.getColorManager().getBlockColorName(userCurrentItem, local) + " : "
							+ arena.getColorManager().getBlockMaterialName(userCurrentItem, local)));
			icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.colorGuiCurrent));
		}

		icon.setPosition(4);
		inv = icon.addToInventory(inv);

		/***************************************************
		 * Glass Separator
		 ***************************************************/

		icon = new ItemStackManager(Material.WHITE_STAINED_GLASS_PANE);
		icon.setTitle(" ");

		for (int i = 0; i < inv.getSize(); i++) {
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
			case 27:
			case 36:
			case 45:
				icon.setPosition(i);
				inv = icon.addToInventory(inv);
				break;
			}
		}

		/***************************************************
		 * Available Colors
		 ***************************************************/

		int slot = 19;
		for (ItemStackManager item : availableBlocks) {
			if (slot % 9 == 0)
				slot++;

			icon = new ItemStackManager(item.getMaterial());
			icon.setPosition(slot++);
			icon.addToInventory(inv);
		}

		while ((slot - 1) % 9 != 0)
			slot++;

		icon = new ItemStackManager(Material.PLAYER_HEAD, 18);
		icon.setPlayerHeadName("azbandit2000");
		icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.keyWordColorRandom));

		icon.addToInventory(inv);

		player.openInventory(inv);

	}
}
