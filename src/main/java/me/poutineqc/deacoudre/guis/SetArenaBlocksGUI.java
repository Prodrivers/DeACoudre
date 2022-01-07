package me.poutineqc.deacoudre.guis;

import me.eddie.inventoryguiapi.gui.contents.UnlimitedGUIPopulator;
import me.eddie.inventoryguiapi.gui.elements.FormImage;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.view.BedrockGUIPresenter;
import me.eddie.inventoryguiapi.util.Callback;
import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.tools.ColorManager;
import me.poutineqc.deacoudre.tools.ItemStackManager;
import me.poutineqc.deacoudre.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class SetArenaBlocksGUI {
	private final Plugin plugin;
	private final PlayerData playerData;
	private final Configuration config;

	private final FormImage imageSelected;

	@Inject
	public SetArenaBlocksGUI(DeACoudre plugin) {
		this.plugin = plugin;
		this.playerData = plugin.getPlayerData();
		this.config = plugin.getConfiguration();
		this.imageSelected = plugin.getConfiguration().guiPlayerColorSelectedImage;
	}

	public void onColorSelected(Player player, Arena arena, Language locale, ItemStack authorizedGameItem) {
		ColorManager colorManager = arena.getColorManager();

		boolean isChoosable = colorManager.isArenaBlockChoosableByPlayers(authorizedGameItem);
		if(isChoosable && colorManager.getArenaBlocks().size() <= arena.getMaxPlayer()) {
			locale.sendMsg(player, locale.editColorColorLessPlayer);
			openColorGUI(player, arena);
			return;
		}

		if(arena.getGameState() == GameState.UNREADY) {
			if(arena.getMinPoolPoint() == null || arena.getMaxPoolPoint() == null) {
				player.closeInventory();
				locale.sendMsg(player, locale.editColorNoPool);
				return;
			}
		}

		if(arena.getGameState() != GameState.READY && arena.getGameState() != GameState.UNREADY) {
			player.closeInventory();
			locale.sendMsg(player, locale.editColorActive);
			return;
		}

		Optional<ItemStackManager> correspondingArenaItem = arena.getColorManager().getBlock(authorizedGameItem);
		if(correspondingArenaItem.isEmpty()) {
			return;
		}

		if(!correspondingArenaItem.get().isAvailable()) {
			player.closeInventory();
			locale.sendMsg(player, locale.editColorChoosen);
			return;
		}


		int valueOfItem = config.usableBlocks.indexOf(authorizedGameItem.getType());
		if(valueOfItem == -1) {
			return;
		}

		colorManager.setAsArenaBlock(authorizedGameItem, !isChoosable);

		arena.resetArena(authorizedGameItem);
		openColorGUI(player, arena);
	}

	private List<GUIElement> generateContent(Player player, Arena arena, Language locale, boolean isBedrockContent) {
		List<GUIElement> contents = new ArrayList<>();

		// Instructions

		ItemStackManager instructionIcon = new ItemStackManager(Material.BOOKSHELF);
		instructionIcon.setTitle(locale.keyWordGuiInstructions);
		instructionIcon.addToLore(locale.editColorGuiTooltip.split("\n"));

		contents.add(GUIElementFactory.createLabelItem(
				4,
				instructionIcon.getItem()
		));

		// Blocks

		int slot = 9; // Offset by one line as it is already occupied
		for(ItemStackManager authorizedGameBlock : arena.getColorManager().getAllAuthorizedGameBlocks()) {
			ItemStackManager item = authorizedGameBlock.clone();
			item.setTitle(ColorManager.getTranslatedMaterialName(item.getItem(), locale));

			FormImage image = FormImage.DEFAULT;
			if(authorizedGameBlock.hasEnchantment(Enchantment.DURABILITY)) {
				image = this.imageSelected;
			}

			contents.add(GUIElementFactory.createActionItem(
					slot++,
					item.getItem(),
					(Callback<Player>) callbackPlayer -> Bukkit.getScheduler().runTask(this.plugin,
							() -> this.onColorSelected(player, arena, locale, authorizedGameBlock.getItem())
					),
					image
			));
		}

		// Arena name

		ItemStack arenaItem = GUIElementFactory.formatItem(
				new ItemStack(Material.PAPER),
				ChatColor.translateAlternateColorCodes('&', locale.arenaColorSelectInfoTitle),
				ChatColor.translateAlternateColorCodes('&', locale.arenaColorSelectInfoLore.replaceAll("%ARENA%", arena.getDisplayName()))
		);

		if(!isBedrockContent) {
			contents.add(GUIElementFactory.createActionItem(
					0,
					arenaItem,
					(Callback<Player>) callbackPlayer -> {
					}
			));
			contents.add(GUIElementFactory.createActionItem(
					8,
					arenaItem,
					(Callback<Player>) callbackPlayer -> {
					}
			));
		}

		return contents;
	}

	public void openColorGUI(Player player, Arena arena) {
		Language locale = playerData.getLanguageOfPlayer(player);

		String title = ChatColor.translateAlternateColorCodes('&', locale.editColorGuiTitle);

		InventoryGUI gui;
		if(Utils.hasBedrockSession(player)) {
			gui = new GUIBuilder()
					.guiStateBehaviour(GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION)
					.inventoryType(InventoryType.CHEST)
					.dynamicallyResizeToWrapContent(true)
					.size(54)
					.presenter(new BedrockGUIPresenter())
					.populator(new UnlimitedGUIPopulator())
					.contents(
							title,
							generateContent(player, arena, locale, true),
							false,
							false,
							false
					)
					.build();
		} else {
			gui = new GUIBuilder()
					.guiStateBehaviour(GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION)
					.inventoryType(InventoryType.CHEST)
					.dynamicallyResizeToWrapContent(true)
					.size(54)
					.contents(
							title,
							generateContent(player, arena, locale, false),
							true,
							true,
							true
					)
					.build();
		}

		try {
			gui.open(player);
		} catch(IllegalStateException e) {
			// Ignore multiple form opening exception
		}
	}
}