package me.poutineqc.deacoudre.guis;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.User;
import me.poutineqc.deacoudre.tools.ColorManager;
import me.poutineqc.deacoudre.tools.ItemStackManager;
import me.poutineqc.deacoudre.tools.Utils;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryBar implements Listener {
	private final PlayerData playerData;

	public InventoryBar(DeACoudre plugin) {
		this.playerData = plugin.getPlayerData();
	}

	/**
	 * Handle Player Interaction Event.
	 * Used for the Parkour Tools whilst on a Course.
	 *
	 * @param event PlayerInteractEvent
	 */
	@EventHandler
	public void onInventoryInteract(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			return;
		}

		Player player = event.getPlayer();

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			return;
		}

		Language locale = playerData.getLanguageOfPlayer(player);

		if (event.getClickedBlock() != null
				&& event.getClickedBlock().getState() instanceof Sign) {
			return;
		}

		ItemStack itemInHand = player.getInventory().getItemInMainHand();

		if(itemInHand.getType() == Material.AIR || itemInHand.getType() == Material.VOID_AIR || itemInHand.getType() == Material.CAVE_AIR) {
			return;
		}

		if(locale.colorGuiOpenItemTitle.equals(itemInHand.getItemMeta().displayName()) || (itemInHand.getItemMeta().hasLore() && locale.colorGuiOpenItemColorSelectedLore.equals(itemInHand.getItemMeta().lore().get(0)))) {
			event.setCancelled(true);
			ColorsGUI.openColorsGui(player, locale, arena);
		}
	}

	public static void giveLobbyTools(User user, Language locale) {
		Player player = user.getPlayer();

		player.getInventory().clear();

		ItemStackManager item;
		if(user.getColor() == null) {
			item = Utils.getRandomHead();
			item.setTitle(locale.colorGuiOpenItemTitle);
		} else {
			item = Utils.getColorHead(user.getColor().getColor());
			if(item == null) {
				item = Utils.getRandomHead();
			}
			item.setTitle(Utils.replaceInComponent(locale.colorGuiOpenItemColorSelectedTitle, "%material%", ColorManager.getTranslatedMaterialName(user.getColor().getItem(), locale)));
			item.addToLore(locale.colorGuiOpenItemColorSelectedLore);
		}

		player.getInventory().setItem(4, item.getItem());

		player.updateInventory();
	}

	public static void giveGameTools(User user) {
		Player player = user.getPlayer();

		player.getInventory().clear();
		player.updateInventory();
	}
}
