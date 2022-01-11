package me.poutineqc.deacoudre.ui;

import fr.prodrivers.bukkit.commons.sections.SectionManager;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.User;
import me.poutineqc.deacoudre.tools.ColorManager;
import me.poutineqc.deacoudre.tools.ItemStackManager;
import me.poutineqc.deacoudre.tools.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryBar implements Listener {
	private final SectionManager sectionManager;
	private final PlayerData playerData;
	private final ColorsGUI playerSelectColorGUI;

	public InventoryBar(SectionManager sectionManager, PlayerData playerData, ColorsGUI playerSelectColorGUI) {
		this.sectionManager = sectionManager;
		this.playerData = playerData;
		this.playerSelectColorGUI = playerSelectColorGUI;
	}

	@EventHandler
	public void onInventoryInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}

		Player player = event.getPlayer();

		Arena arena = Arena.getArenaFromPlayer(player);
		if(arena == null) {
			return;
		}

		Language locale = playerData.getLanguageOfPlayer(player);

		if(event.getClickedBlock() != null
				&& event.getClickedBlock().getState() instanceof Sign) {
			return;
		}

		ItemStack itemInHand = player.getInventory().getItemInMainHand();

		if(itemInHand.getType() == Material.AIR || itemInHand.getType() == Material.VOID_AIR || itemInHand.getType() == Material.CAVE_AIR) {
			return;
		}

		if(locale.colorGuiOpenItemTitle.equals(itemInHand.getItemMeta().displayName()) || (itemInHand.getItemMeta().hasLore() && locale.colorGuiOpenItemColorSelectedLore.equals(itemInHand.getItemMeta().lore().get(0)))) {
			event.setCancelled(true);
			this.playerSelectColorGUI.openColorsGui(player, locale, arena);
		}

		if(itemInHand.getItemMeta().hasLore() && locale.quitGameItemLore.equals(itemInHand.getItemMeta().lore().get(0))) {
			event.setCancelled(true);
			this.sectionManager.enter(player);
		}
	}

	public static void giveArenaLobbyTools(Arena arena, User user, Language locale) {
		Player player = user.getPlayer();

		player.getInventory().clear();

		boolean hasBedrockSession = Utils.hasBedrockSession(player);

		ItemStackManager colorItem;
		if(user.getColor() == null) {
			colorItem = Utils.getRandomHead(hasBedrockSession);
			colorItem.setTitle(locale.colorGuiOpenItemTitle);
		} else {
			colorItem = Utils.getColorHead(user.getColor().getColor(), hasBedrockSession);
			if(colorItem == null) {
				colorItem = Utils.getRandomHead(hasBedrockSession);
			}
			colorItem.setTitle(Utils.replaceInComponent(locale.colorGuiOpenItemColorSelectedTitle, "%material%", ColorManager.getTranslatedMaterialName(user.getColor().getItem(), locale)));
			colorItem.addToLore(locale.colorGuiOpenItemColorSelectedLore);
		}

		player.getInventory().setItem(3, colorItem.getItem());

		ItemStackManager quitItem = new ItemStackManager(Material.BARRIER);
		quitItem.setTitle(Utils.replaceInComponent(locale.quitGameItemTitle, "%arena%", Component.text(arena.getDisplayName())));
		quitItem.addToLore(locale.quitGameItemLore);

		player.getInventory().setItem(5, quitItem.getItem());

		player.updateInventory();
	}

	public static void giveGameTools(User user) {
		Player player = user.getPlayer();

		player.getInventory().clear();
		player.updateInventory();
	}
}
