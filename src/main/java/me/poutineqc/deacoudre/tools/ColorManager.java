package me.poutineqc.deacoudre.tools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.poutineqc.deacoudre.ArenaData;
import me.poutineqc.deacoudre.Configuration;
import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.Language;
import me.poutineqc.deacoudre.MySQL;
import me.poutineqc.deacoudre.instances.Arena;
import me.poutineqc.deacoudre.instances.User;

public class ColorManager {

	private long colorIndice;
	private List<ItemStackManager> allBlocks;
	private List<ItemStackManager> onlyChoosenBlocks;
	private MySQL mysql;
	private Configuration config;
	private Arena arena;
	private ArenaData arenaData;

	public ColorManager(Long colorIndice, DeACoudre plugin, Arena arena) {
		this.colorIndice = colorIndice;
		this.mysql = plugin.getMySQL();
		this.arenaData = plugin.getArenaData();
		this.config = plugin.getConfiguration();
		this.arena = arena;
		updateLists();
	}

	public void setColorIndice(long colorIndice) {
		this.colorIndice = colorIndice;
		updateLists();

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET colorIndice=" + colorIndice + " WHERE name='"
					+ arena.getName() + "';");
		} else {
			arenaData.getData().set("arenas." + arena.getName() + ".colorIndice", colorIndice);
			arenaData.saveArenaData();
		}
	}

	public void updateLists() {
		allBlocks = new ArrayList<ItemStackManager>();
		onlyChoosenBlocks = new ArrayList<ItemStackManager>();
		long tempColorIndice = colorIndice;

		for (int i = 31; i >= 0; i--) {
			ItemStackManager icon;
			if (i >= 16)
				icon = new ItemStackManager(Material.STAINED_CLAY);
			else
				icon = new ItemStackManager(Material.WOOL);

			icon.setData((short) (i % 16));

			int value = (int) Math.pow(2, i);
			if (value <= tempColorIndice) {
				icon.addEnchantement(Enchantment.DURABILITY, 1);
				tempColorIndice -= value;
				onlyChoosenBlocks.add(0, icon);
			}

			allBlocks.add(0, icon);
		}

		if (onlyChoosenBlocks.size() == 0)
			onlyChoosenBlocks = allBlocks;
	}

	public ItemStackManager getRandomAvailableBlock() {
		List<ItemStackManager> availableBlocks = getAvailableBlocks();
		return availableBlocks.get((int) Math.floor(Math.random() * availableBlocks.size()));
	}

	public List<ItemStackManager> getAvailableBlocks() {
		List<ItemStackManager> availableBlocks = new ArrayList<ItemStackManager>();
		for (ItemStackManager item : onlyChoosenBlocks) {
			if (isBlockUsed(item.getItem()))
				continue;

			availableBlocks.add(item);
		}

		return availableBlocks;
	}

	public List<ItemStackManager> getAllBlocks() {
		return allBlocks;
	}

	public List<ItemStackManager> getOnlyChoosenBlocks() {
		return onlyChoosenBlocks;
	}

	public long getColorIndice() {
		return colorIndice;
	}

	public List<ItemStackManager> getSpecificAvailableItems(Material material) {
		List<ItemStackManager> specificAvailableBlocks = new ArrayList<ItemStackManager>();
		for (ItemStackManager item : getAvailableBlocks())
			if (item.getMaterial() == material)
				specificAvailableBlocks.add(item);

		return specificAvailableBlocks;
	}

	public boolean isBlockUsed(ItemStack item) {
		for (User user : arena.getUsers())
			if (user.getItemStack() != null)
				if (user.getItemStack().getType() == item.getType()
						&& user.getItemStack().getDurability() == item.getDurability())
					return true;

		return false;
	}

	public String getBlockMaterialName(ItemStack item, Language local) {
		switch (item.getType()) {
		case STAINED_CLAY:
			return local.keyWordColorClay;
		case WOOL:
			return local.keyWordColorWool;
		default:
			return local.keyWordColorRandom;
		}
	}

	public String getBlockColorName(ItemStack item, Language local) {
		switch (item.getDurability()) {
		case 0:
			return local.keyWordColorWhite;
		case 1:
			return local.keyWordColorOrange;
		case 2:
			return local.keyWordColorMagenta;
		case 3:
			return local.keyWordColorLightBlue;
		case 4:
			return local.keyWordColorYellow;
		case 5:
			return local.keyWordColorLime;
		case 6:
			return local.keyWordColorPink;
		case 7:
			return local.keyWordColorGrey;
		case 8:
			return local.keyWordColorLightGrey;
		case 9:
			return local.keyWordColorCyan;
		case 10:
			return local.keyWordColorPurple;
		case 11:
			return local.keyWordColorBlue;
		case 12:
			return local.keyWordColorBrown;
		case 13:
			return local.keyWordColorGreen;
		case 14:
			return local.keyWordColorRed;
		case 15:
			return local.keyWordColorBlack;
		default:
			return local.keyWordColorRandom;
		}
	}

}
