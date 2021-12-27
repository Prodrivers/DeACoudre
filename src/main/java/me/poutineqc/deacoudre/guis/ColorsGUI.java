package me.poutineqc.deacoudre.guis;

import java.util.List;
import java.util.Optional;

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

		if(item == null || item.getItemMeta() == null) {
			return;
		}

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

		Optional<ItemStackManager> correspondingArenaItem = arena.getColorManager().getBlock(item);
		if(!correspondingArenaItem.isPresent()) {
			return;
		}

		if (!correspondingArenaItem.get().isAvailable()) {
			local.sendMsg(player, local.colorAlreadyPicked);
			achievements.testAchievement(Achievement.colorRivalery, player);
		} else {
			user.setColor(correspondingArenaItem.get());
			local.sendMsg(player,
					local.colorChoosen
							.replace("%material%",
									arena.getColorManager().getBlockMaterialName(user.getColor().getItem(), local)));
		}

		player.closeInventory();
	}

	public static void openColorsGui(Player player, Language local, Arena arena) {
		ItemStackManager userCurrentItem = arena.getUser(player).getColor();
		Inventory inv;
		ItemStackManager icon;
		List<ItemStackManager> availableBlocks = arena.getColorManager().getAvailableArenaBlocks();
		int size = (availableBlocks.size() % 9 == 0 ? availableBlocks.size() : availableBlocks.size() / 9 * 9 + 9) + 18;
		if(size > 6 * 9) {
			size = 6 * 9;
		}

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
			icon = userCurrentItem;
			icon.addToLore(ChatColor.translateAlternateColorCodes('&',
					arena.getColorManager().getBlockMaterialName(userCurrentItem.getItem(), local)));
			icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.colorGuiCurrent));
		}

		icon.setPosition(4);
		inv = icon.addToInventory(inv);

		/***************************************************
		 * Available Colors
		 ***************************************************/

		int slot = 9;
		for (ItemStackManager item : availableBlocks) {
			if(slot >= 6 * 9) {
				break;
			}
			icon = item.clone();
			icon.setPosition(slot++);
			icon.addToInventory(inv);
		}

		// Offset to next line
		while ((slot - 1) % 9 != 0) {
			slot++;
		}
		// Offset to line center
		slot += 3;
		// If there is no space in dialog, go back to first line
		if(slot >= size) {
			slot = 0;
		}

		icon = new ItemStackManager(Material.PLAYER_HEAD, slot);
		icon.setPlayerHeadName("azbandit2000");
		icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.keyWordColorRandom));

		icon.addToInventory(inv);

		player.openInventory(inv);

	}
}
