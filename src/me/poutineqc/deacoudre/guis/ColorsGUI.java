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
		Inventory inv = event.getInventory();
		Player player = (Player) event.getWhoClicked();
		Language local = playerData.getLanguageOfPlayer(player);

		if (!ChatColor.stripColor(inv.getTitle()).equalsIgnoreCase(
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.colorGuiTitle))))
			return;

		if (event.getAction().equals(InventoryAction.NOTHING) || event.getAction().equals(InventoryAction.UNKNOWN))
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

		if (item.getType() != Material.WOOL && item.getType() != Material.STAINED_CLAY)
			return;

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
		int size = 18;
		List<ItemStackManager> availableWoolItems = arena.getColorManager().getSpecificAvailableItems(Material.WOOL);
		List<ItemStackManager> availableClayItems = arena.getColorManager()
				.getSpecificAvailableItems(Material.STAINED_CLAY);

		if (availableWoolItems.size() > 9)
			size += 18;
		else if (availableWoolItems.size() > 0)
			size += 9;

		if (availableClayItems.size() > 9)
			size += 18;
		else if (availableClayItems.size() > 0)
			size += 9;

		inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', local.colorGuiTitle));

		/***************************************************
		 * Current Item
		 ***************************************************/

		if (userCurrentItem == null) {
			icon = new ItemStackManager(Material.SKULL_ITEM);
			icon.setData((short) 3);
			icon.setPlayerHeadName("azbandit2000");
			icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.colorGuiCurrent));
			icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.keyWordColorRandom));

		} else {
			icon = new ItemStackManager(userCurrentItem.getType());
			icon.setData(userCurrentItem.getDurability());
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

		icon = new ItemStackManager(Material.STAINED_GLASS_PANE);
		icon.setTitle(" ");
		icon.setData((short) 1);

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
		for (ItemStackManager woolItem : availableWoolItems) {
			if (slot % 9 == 0)
				slot++;

			icon = new ItemStackManager(woolItem.getMaterial());
			icon.setData(woolItem.getData());
			icon.setPosition(slot++);
			icon.addToInventory(inv);
		}

		while ((slot - 1) % 9 != 0)
			slot++;

		for (ItemStackManager clayItem : availableClayItems) {
			if (slot % 9 == 0)
				slot++;

			icon = new ItemStackManager(clayItem.getMaterial());
			icon.setData(clayItem.getData());
			icon.setPosition(slot++);
			icon.addToInventory(inv);
		}

		icon = new ItemStackManager(Material.SKULL_ITEM, 18);
		icon.setData((short) 3);
		icon.setPlayerHeadName("azbandit2000");
		icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.keyWordColorRandom));

		icon.addToInventory(inv);

		player.openInventory(inv);

	}
}
