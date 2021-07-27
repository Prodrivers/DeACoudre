package me.poutineqc.deacoudre.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
	private List<ItemStackManager> allBlocks;
	private List<ItemStackManager> onlyChoosenBlocks;
	private MySQL mysql;
	private Configuration config;
	private Arena arena;
	private ArenaData arenaData;
	private Logger logger;

	public ColorManager(DeACoudre plugin, Arena arena) {
		this.logger = plugin.getLogger();
		this.mysql = plugin.getMySQL();
		this.arenaData = plugin.getArenaData();
		this.config = plugin.getConfiguration();
		this.arena = arena;
		load();
	}

	public boolean isChoosable(ItemStack item) {
		return isChoosable(item.getType());
	}

	public boolean isChoosable(Material material) {
		return onlyChoosenBlocks.stream().anyMatch(block -> block.getItem().getType() == material);
	}

	public void setChoosable(ItemStack item, boolean choosable) {
		setChoosable(item.getType(), choosable);
	}

	public void setChoosable(Material material, boolean choosable) throws IllegalArgumentException {
		Optional<ItemStackManager> block = getBlock(material);
		if(!block.isPresent()) {
			throw new IllegalArgumentException("Material not corresponding to any usable block.");
		}

		this.onlyChoosenBlocks.add(block.get());

		if (mysql.hasConnection()) {
			List<String> materials = this.onlyChoosenBlocks.stream().map(item -> item.getItem().getType().toString()).collect(Collectors.toList());
			String serializedMaterials = String.join(";", materials);
			Optional<PreparedStatement> ost = mysql.getPreparedStatement("UPDATE ? SET usableBlocks=? WHERE name=?;");
			if(ost.isPresent()) {
				try(PreparedStatement st = ost.get()) {
					st.setString(1, config.tablePrefix + "ARENAS");
					st.setString(2, serializedMaterials);
					st.setString(3, arena.getName());
					st.execute();
				} catch(SQLException e) {
					logger.log(Level.SEVERE, "Unable to update choosable block for arena " + arena.getName(), e);
				}
			}
		} else {
			arenaData.getData().set("arenas." + arena.getName() + ".usableBlocks." + material, choosable);
			arenaData.saveArenaData();
		}
	}

	public void load() {
		allBlocks = new ArrayList<>();
		onlyChoosenBlocks = new ArrayList<>();
		List<String> choosenMaterialsStrings;

		if (mysql.hasConnection()) {
			choosenMaterialsStrings = new ArrayList<>();
			List<String> materials = this.onlyChoosenBlocks.stream().map(item -> item.getItem().getType().toString()).collect(Collectors.toList());
			Optional<PreparedStatement> ost = mysql.getPreparedStatement("SELECT usableBlocks FROM ? WHERE name=?;");
			if(ost.isPresent()) {
				try(PreparedStatement st = ost.get()) {
					st.setString(1, config.tablePrefix + "ARENAS");
					st.setString(2, arena.getName());
					ResultSet sql = st.executeQuery();
					if(sql.next()) {
						String serializedMaterials = sql.getString(1);
						choosenMaterialsStrings = Arrays.asList(serializedMaterials.split(";"));
					}
				} catch(SQLException e) {
					logger.log(Level.SEVERE, "Unable to update choosable block for arena " + arena.getName(), e);
				}
			}
		} else {
			choosenMaterialsStrings = arenaData.getData().getStringList("arenas." + arena.getName() + ".usableBlocks");
		}
		Set<Material> choosenMaterials = choosenMaterialsStrings.stream().map(Material::valueOf).collect(Collectors.toSet());

		for (Material material : config.usableBlocks) {
			ItemStackManager icon = new ItemStackManager(material);

			if(choosenMaterials.contains(material)) {
				icon.addEnchantement(Enchantment.DURABILITY, 1);
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
		return onlyChoosenBlocks.stream().filter(ItemStackManager::isAvailable).collect(Collectors.toList());
	}

	public List<ItemStackManager> getAllBlocks() {
		return allBlocks;
	}

	public List<ItemStackManager> getOnlyChoosenBlocks() {
		return onlyChoosenBlocks;
	}

	public Optional<ItemStackManager> getBlock(ItemStack item) {
		return getBlock(item.getType());
	}

	public Optional<ItemStackManager> getBlock(Material material) {
		return allBlocks.stream().filter(item -> item.getItem().getType() == material).findFirst();
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
