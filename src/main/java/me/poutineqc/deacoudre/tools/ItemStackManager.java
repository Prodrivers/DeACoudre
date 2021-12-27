package me.poutineqc.deacoudre.tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStackManager {
	private int position;
	private ItemStack item;
	private ItemMeta meta;
	List<String> lore = new ArrayList<String>();
	private boolean available = true;

	public ItemStackManager(Material material) {
		item = new ItemStack(material);
		meta = item.getItemMeta();
	}

	public ItemStackManager(Material material, int position) {
		this.position = position;
		item = new ItemStack(material);
		meta = item.getItemMeta();
	}

	public ItemStackManager clone() {
		ItemStackManager newItem = new ItemStackManager(this.item.getType(), this.position);
		newItem.item.setItemMeta(this.meta.clone());
		newItem.lore.addAll(this.lore);
		newItem.available = this.available;
		return newItem;
	}

	public Material getMaterial() {
		return item.getType();
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setTitle(String displayName) {
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		meta.setLocalizedName(ChatColor.translateAlternateColorCodes('&', displayName));
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
		meta.addEnchant(enchantment, level, true);
	}

	public void removeEnchantement(Enchantment enchantment) {
		meta.removeEnchant(enchantment);
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

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}
}