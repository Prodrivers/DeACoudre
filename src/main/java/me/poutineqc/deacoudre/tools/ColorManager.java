package me.poutineqc.deacoudre.tools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
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
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;

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
		allBlocks = new ArrayList<>();
		onlyChoosenBlocks = new ArrayList<>();
		long tempColorIndice = colorIndice;

		int i = 0;
		for (Material material : config.usableBlocks) {
			ItemStackManager icon = new ItemStackManager(material);

			int value = (int) Math.pow(2, i);
			if (value <= tempColorIndice) {
				icon.addEnchantement(Enchantment.DURABILITY, 1);
				tempColorIndice -= value;
				onlyChoosenBlocks.add(0, icon);
			}

			allBlocks.add(0, icon);

			i++;
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

	public boolean isBlockUsed(ItemStack item) {
		for (User user : arena.getUsers())
			if (user.getItemStack() != null)
				if (user.getItemStack().getType() == item.getType())
					return true;

		return false;
	}

	public String getBlockMaterialName(ItemStack item, Language local) {
		if (item.getType().toString().contains(Material.TERRACOTTA.toString())) {
			return local.keyWordColorClay;
		}
		if (item.getType().toString().contains(Material.LEGACY_WOOL.toString())) {
			return local.keyWordColorWool;
		}
		return local.keyWordColorRandom;
	}

	public String getBlockColorName(ItemStack item, Language local) {
		MaterialData data = item.getData();
		if(data instanceof Colorable) {
			DyeColor color = ((Colorable) data).getColor();
			if(color != null) {
				switch(color) {
					case WHITE:
						return local.keyWordColorWhite;
					case ORANGE:
						return local.keyWordColorOrange;
					case MAGENTA:
						return local.keyWordColorMagenta;
					case LIGHT_BLUE:
						return local.keyWordColorLightBlue;
					case YELLOW:
						return local.keyWordColorYellow;
					case LIME:
						return local.keyWordColorLime;
					case PINK:
						return local.keyWordColorPink;
					case GRAY:
						return local.keyWordColorGrey;
					case LIGHT_GRAY:
						return local.keyWordColorLightGrey;
					case CYAN:
						return local.keyWordColorCyan;
					case PURPLE:
						return local.keyWordColorPurple;
					case BLUE:
						return local.keyWordColorBlue;
					case BROWN:
						return local.keyWordColorBrown;
					case GREEN:
						return local.keyWordColorGreen;
					case RED:
						return local.keyWordColorRed;
					case BLACK:
						return local.keyWordColorBlack;
				}
			}
		}

		return local.keyWordColorRandom;
	}

}
