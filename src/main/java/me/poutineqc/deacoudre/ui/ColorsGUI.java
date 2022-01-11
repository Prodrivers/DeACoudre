package me.poutineqc.deacoudre.ui;

import me.eddie.inventoryguiapi.gui.contents.UnlimitedGUIPopulator;
import me.eddie.inventoryguiapi.gui.elements.FormImage;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.view.BedrockGUIPresenter;
import me.eddie.inventoryguiapi.util.Callback;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.achievements.Achievement;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.User;
import me.poutineqc.deacoudre.tools.ColorManager;
import me.poutineqc.deacoudre.tools.ItemStackManager;
import me.poutineqc.deacoudre.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ColorsGUI {
	private final Plugin plugin;
	private final Achievement achievements;

	private final FormImage imageSelected;
	private final FormImage imageTaken;
	private final FormImage imageRandom;

	@Inject
	public ColorsGUI(DeACoudre plugin) {
		this.plugin = plugin;
		this.achievements = plugin.getAchievement();
		this.imageSelected = plugin.getConfiguration().guiPlayerColorSelectedImage;
		this.imageTaken = plugin.getConfiguration().guiPlayerColorTakenImage;
		this.imageRandom = plugin.getConfiguration().guiPlayerColorRandomImage;
	}

	public void onColorSelected(Arena arena, User user, Language locale, ItemStackManager arenaItem) {
		Player player = user.getPlayer();

		player.closeInventory();

		if(!arenaItem.isAvailable()) {
			locale.sendMsg(player, locale.colorAlreadyPicked);
			this.achievements.testAchievement(Achievement.colorRivalery, player);
		} else {
			user.setColor(arenaItem);
			locale.sendMsg(player,
					Utils.replaceInComponent(locale.colorChoosen, "%material%", ColorManager.getTranslatedMaterialName(user.getColor().getItem(), locale))
			);
		}

		InventoryBar.giveArenaLobbyTools(arena, user, locale);
	}

	private List<GUIElement> generateContent(Player player, Arena arena, Language locale, boolean isBedrockContent) {
		User user = arena.getUser(player);

		List<GUIElement> contents = new ArrayList<>();

		// Current Item

		ItemStackManager userCurrentColor = arena.getUser(player).getColor();

		ItemStackManager userCurrentItem;
		if(userCurrentColor == null) {
			userCurrentItem = Utils.getRandomHead(isBedrockContent);
			userCurrentItem.setTitle(ChatColor.translateAlternateColorCodes('&', locale.colorGuiCurrent));
			userCurrentItem.addToLore(ChatColor.translateAlternateColorCodes('&', locale.keyWordColorRandom));
		} else {
			userCurrentItem = userCurrentColor;
			userCurrentItem.clearLore();
			userCurrentItem.addToLore(ColorManager.getTranslatedMaterialName(userCurrentItem.getItem(), locale));
			userCurrentItem.setTitle(ChatColor.translateAlternateColorCodes('&', locale.colorGuiCurrent));
		}

		userCurrentItem.addEnchantement(Enchantment.DURABILITY, 1);

		contents.add(GUIElementFactory.createLabelItem(
				4,
				userCurrentItem.getItem()
		));

		// Available Colors
		Collection<ItemStackManager> arenaBlocks = arena.getColorManager().getArenaBlocks();

		int slot = 9;
		for(ItemStackManager arenaBlock : arenaBlocks) {
			ItemStackManager item = arenaBlock.clone();
			item.setTitle(ColorManager.getTranslatedMaterialName(item.getItem(), locale));

			FormImage image = FormImage.DEFAULT;

			item.clearEnchantements();
			if(!item.isAvailable()) {
				item.addEnchantement(Enchantment.DURABILITY, 1);
				image = this.imageTaken;
			}

			if(userCurrentColor != null && item.getMaterial() == userCurrentColor.getMaterial()) {
				image = this.imageSelected;
			}

			contents.add(GUIElementFactory.createActionItem(
					slot++,
					item.getItem(),
					(Callback<Player>) callbackPlayer -> Bukkit.getScheduler().runTask(this.plugin,
							() -> this.onColorSelected(arena, user, locale, arenaBlock)
					),
					image
			));
		}

		// Offset to next line
		while((slot - 1) % 9 != 0) {
			slot++;
		}
		// Offset to line center
		slot += 3;

		contents.add(GUIElementFactory.createActionItem(
				slot,
				GUIElementFactory.formatItem(
						Utils.getRandomHead(isBedrockContent).getItem(),
						ChatColor.translateAlternateColorCodes('&', locale.keyWordColorRandom)
				),
				(Callback<Player>) callbackPlayer -> {
					player.closeInventory();

					user.removeColor();
					locale.sendMsg(player, locale.colorRandom);
					InventoryBar.giveArenaLobbyTools(arena, user, locale);
				},
				this.imageRandom
		));

		return contents;
	}

	public void openColorsGui(Player player, Language locale, Arena arena) {
		String title = ChatColor.translateAlternateColorCodes('&', locale.colorGuiTitle);

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
		} catch (IllegalStateException e) {
			// Ignore multiple form opening exception
		}
	}
}
