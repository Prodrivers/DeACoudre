package me.poutineqc.deacoudre.tools;

import me.poutineqc.deacoudre.*;
import me.poutineqc.deacoudre.instances.Arena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ColorManager {
	private final MySQL mysql;
	private final Configuration config;
	private final Arena arena;
	private final ArenaData arenaData;
	private List<ItemStackManager> allAuthorizedGameBlocks;
	private List<ItemStackManager> arenaBlocks;
	private Set<Material> arenaMaterials;

	public ColorManager(DeACoudre plugin, Arena arena) {
		this.mysql = plugin.getMySQL();
		this.arenaData = plugin.getArenaData();
		this.config = plugin.getConfiguration();
		this.arena = arena;
		load();
	}

	public boolean isArenaBlockChoosableByPlayers(ItemStack item) {
		return isArenaBlockChoosableByPlayers(item.getType());
	}

	public boolean isArenaBlockChoosableByPlayers(Material material) {
		return arenaBlocks.stream().anyMatch(block -> block.getItem().getType() == material);
	}

	public void setAsArenaBlock(ItemStack item, boolean choosable) {
		setAsArenaBlock(item.getType(), choosable);
	}

	public void setAsArenaBlock(Material material, boolean choosable) throws IllegalArgumentException {
		Optional<ItemStackManager> block = getBlock(material);
		if(!block.isPresent()) {
			throw new IllegalArgumentException("Material not corresponding to any usable block.");
		}

		String usableBlocksConfigurationPath = "arenas." + arena.getShortName() + ".usableBlocks";

		List<String> arenaMaterialStrings = arenaData.getData().getStringList(usableBlocksConfigurationPath);

		// Fill arena materials with all possible values if it is empty, before any modification
		if(arenaMaterialStrings.isEmpty()) {
			arenaMaterialStrings = config.usableBlocks.stream().map(Object::toString).collect(Collectors.toList());
			arenaMaterials.addAll(config.usableBlocks);
		}

		if(choosable) {
			markArenaBlockAsSelected(block.get());
			arenaMaterialStrings.add(material.toString());
			this.arenaBlocks.add(block.get());
			this.arenaMaterials.add(block.get().getMaterial());
		} else {
			unmarkArenaBlockAsSelected(block.get());
			arenaMaterialStrings.remove(material.toString());
			this.arenaBlocks.remove(block.get());
			this.arenaMaterials.remove(block.get().getMaterial());
		}

		arenaData.getData().set(usableBlocksConfigurationPath, arenaMaterialStrings);

		arenaData.saveArenaData();
	}

	private void markArenaBlockAsSelected(ItemStackManager item) {
		item.addEnchantement(Enchantment.DURABILITY, 1);
	}

	private void unmarkArenaBlockAsSelected(ItemStackManager item) {
		item.clearEnchantements();
	}

	public void load() {
		allAuthorizedGameBlocks = new ArrayList<>();
		arenaBlocks = new ArrayList<>();

		List<String> arenaMaterialStrings = arenaData.getData().getStringList("arenas." + arena.getShortName() + ".usableBlocks");

		arenaMaterials = arenaMaterialStrings.stream().map(Material::valueOf).collect(Collectors.toSet());

		if(arenaMaterials.isEmpty()) {
			config.usableBlocks.stream()
					.map(ItemStackManager::new)
					.forEachOrdered(item -> {
						markArenaBlockAsSelected(item);
						allAuthorizedGameBlocks.add(item);
						arenaBlocks.add(item);
						arenaMaterials.add(item.getMaterial());
					});
		} else {
			for(Material material : config.usableBlocks) {
				ItemStackManager icon = new ItemStackManager(material);

				if(arenaMaterials.contains(material)) {
					markArenaBlockAsSelected(icon);
					arenaBlocks.add(0, icon);
				}

				allAuthorizedGameBlocks.add(0, icon);
			}
		}
	}

	public ItemStackManager getRandomAvailableArenaBlock() {
		List<ItemStackManager> availableBlocks = getAvailableArenaBlocks();
		return availableBlocks.get((int) Math.floor(Math.random() * availableBlocks.size()));
	}

	public List<ItemStackManager> getAvailableArenaBlocks() {
		return arenaBlocks.stream().filter(ItemStackManager::isAvailable).collect(Collectors.toList());
	}

	public List<ItemStackManager> getAllAuthorizedGameBlocks() {
		return allAuthorizedGameBlocks;
	}

	public List<ItemStackManager> getArenaBlocks() {
		return arenaBlocks;
	}

	public Set<Material> getArenaMaterials() {
		return arenaMaterials;
	}

	public Optional<ItemStackManager> getBlock(ItemStack item) {
		return getBlock(item.getType());
	}

	public Optional<ItemStackManager> getBlock(Material material) {
		return allAuthorizedGameBlocks.stream().filter(item -> item.getItem().getType() == material).findFirst();
	}

	public static Component getTranslatedMaterialName(ItemStack item, Language local) {
		return Component.join(
				LegacyComponentSerializer.legacyAmpersand().deserialize(local.keyWordMaterialPrefix),
				Component.translatable(item.getTranslationKey())
		);
	}
}
