package me.poutineqc.deacoudre.guis;

import fr.prodrivers.bukkit.commons.sections.Section;
import fr.prodrivers.bukkit.commons.sections.SectionManager;
import fr.prodrivers.bukkit.commons.ui.section.SelectionUI;
import me.eddie.inventoryguiapi.gui.contents.UnlimitedGUIPopulator;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.view.BedrockGUIPresenter;
import me.eddie.inventoryguiapi.util.Callback;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.PlayerData;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.GameState;
import me.poutineqc.deacoudre.sections.MainDACSection;
import me.poutineqc.deacoudre.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class JoinGUI implements SelectionUI {
	private final Plugin plugin;
	private final PlayerData playerData;
	private final SectionManager sectionManager;

	@Inject
	public JoinGUI(final Plugin plugin, final PlayerData playerData, final SectionManager sectionManager) {
		this.plugin = plugin;
		this.playerData = playerData;
		this.sectionManager = sectionManager;
	}

	private List<GUIElement> generateContent(Language local, boolean isBedrockContent) {
		List<GUIElement> contents = new ArrayList<>();

		// Main Item

		ItemStack mainItem;
		if(isBedrockContent) {
			mainItem = GUIElementFactory.formatItem(
					new ItemStack(Material.BOOKSHELF, 1),
					local.joinGuiTooltipBedrock[0],
					Arrays.stream(local.joinGuiTooltipBedrock).skip(1).toArray(String[]::new)
			);
		} else {
			mainItem = GUIElementFactory.formatItem(
					new ItemStack(Material.BOOKSHELF, 1),
					ChatColor.translateAlternateColorCodes('&', local.keyWordGuiInstructions),
					local.joinGuiTooltip
			);
		}

		contents.add(GUIElementFactory.createLabelItem(
				4,
				mainItem
		));

		// Glass Separator

		if(!isBedrockContent) {
			for(int i = 9; i <= 17; i++) {
				contents.add(GUIElementFactory.createLabelItem(
						i,
						GUIElementFactory.formatItem(
								new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1),
								" "
						)
				));
			}
		}

		// Arenas
		int slot = 18;

		for(Arena arena : Arena.getArenas()) {
			String title = ChatColor.GOLD + arena.getDisplayName();
			ItemStack item;
			if(!arena.isAllSet()) {
				item = GUIElementFactory.formatItem(
						new ItemStack(Material.GRAY_CONCRETE, 1),
						title,
						ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateUnset)
				);
			} else if(arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
				item = GUIElementFactory.formatItem(
						new ItemStack(Material.RED_CONCRETE, 1),
						title,
						ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateStarted)
				);
			} else if(arena.getUsers().size() >= arena.getMaxPlayer()) {
				if(isBedrockContent) {
					item = GUIElementFactory.formatItem(
							new ItemStack(Material.RED_CONCRETE, 1),
							title,
							ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateFull)
									+ " | "
									+ ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers)
									+ ChatColor.DARK_GRAY + " : " + arena.getNonEliminated().size() + "/"
									+ arena.getMaxPlayer()
					);
				} else {
					item = GUIElementFactory.formatItem(
							new ItemStack(Material.RED_CONCRETE, 1),
							title,
							ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateFull),
							ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers)
									+ ChatColor.DARK_GRAY + " : " + arena.getNonEliminated().size() + "/"
									+ arena.getMaxPlayer()
					);
				}
			} else {
				if(isBedrockContent) {
					item = GUIElementFactory.formatItem(
							new ItemStack(Material.GREEN_CONCRETE, 1),
							title,
							ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateReady)
									+ " | "
									+ ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers)
									+ ChatColor.DARK_GRAY + " : " + arena.getNonEliminated().size() + "/"
									+ arena.getMaxPlayer()
					);
				} else {
					item = GUIElementFactory.formatItem(
							new ItemStack(Material.GREEN_CONCRETE, 1),
							title,
							ChatColor.translateAlternateColorCodes('&', local.keyWordGameStateReady),
							ChatColor.translateAlternateColorCodes('&', local.keyWordScoreboardPlayers)
									+ ChatColor.DARK_GRAY + " : " + arena.getNonEliminated().size() + "/"
									+ arena.getMaxPlayer()
					);
				}
			}

			contents.add(GUIElementFactory.createActionItem(
					slot++,
					item,
					(Callback<Player>) player -> Bukkit.getScheduler().runTask(this.plugin,
							() -> {
								player.closeInventory();
								sectionManager.enter(player, arena.getFullSectionName());
							})
			));
		}

		return contents;
	}

	public void openJoinGui(Player player) {
		Language local = playerData.getLanguageOfPlayer(player);

		String title = ChatColor.translateAlternateColorCodes('&', local.joinGuiTitle);

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
							generateContent(local, true),
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
							generateContent(local, false),
							true,
							true,
							true
					)
					.build();
		}

		gui.open(player);
	}

	@Override
	public void ui(Section section, Player player) {
		if(MainDACSection.DAC_SECTION_NAME.equals(section.getFullName())) {
			openJoinGui(player);
		}
	}
}
