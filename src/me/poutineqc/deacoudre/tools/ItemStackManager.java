package me.poutineqc.deacoudre.tools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemStackManager {

	private int position;
	private ItemStack item;
	private ItemMeta meta;
	List<String> lore = new ArrayList<String>();

	public ItemStackManager(Material material) {
		item = new ItemStack(material);
		meta = item.getItemMeta();
	}

	public ItemStackManager(Material material, int position) {
		this.position = position;
		item = new ItemStack(material);
		meta = item.getItemMeta();
	}

	public Material getMaterial() {
		return item.getType();
	}

	public short getData() {
		return item.getDurability();
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setData(short data) {
		item.setDurability(data);
	}

	public void setTitle(String displayName) {
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
	}

	public void addToLore(String loreLine) {
		lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
	}

	public Inventory addToInventory(Inventory inv) {
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(position, item);
		return inv;
	}

	public void addEnchantement(Enchantment enchantment, int level) {
		meta.addEnchant(Enchantment.DURABILITY, -1, true);
	}

	public ItemStack getItem() {
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public void setPlayerHeadName(String player) {
		if (meta instanceof SkullMeta)
			((SkullMeta) meta).setOwner(player);
	}

	public String getDisplayName() {
		return meta.getDisplayName();
	}

	public void clearLore() {
		lore.clear();
	}

	public void clearEnchantements() {
		if (meta.hasEnchant(Enchantment.DURABILITY))
			meta.removeEnchant(Enchantment.DURABILITY);
	}
}