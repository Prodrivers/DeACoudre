package me.poutineqc.deacoudre.tools;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemStackManager {
	private final List<Component> lore = new ArrayList<>();
	private final ItemStack item;
	private final ItemMeta meta;
	private int position;
	private boolean available = true;

	public ItemStackManager(Material material) {
		this(material, 0);
	}

	public ItemStackManager(Material material, int position) {
		this.position = position;
		item = new ItemStack(material);
		meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	}

	public ItemStackManager(Material material, ItemMeta itemMeta, int position) {
		this.position = position;
		item = new ItemStack(material);
		meta = itemMeta;
		item.setItemMeta(meta);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	}

	public ItemStackManager clone() {
		ItemStackManager newItem = new ItemStackManager(this.item.getType(), this.meta.clone(), this.position);
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
		meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName));
		meta.setLocalizedName(ChatColor.translateAlternateColorCodes('&', displayName));
	}

	public void setTitle(Component displayName) {
		meta.displayName(displayName);
		meta.setLocalizedName(LegacyComponentSerializer.legacySection().serialize(displayName));
	}

	public void addToLore(String loreLine) {
		addToLore(LegacyComponentSerializer.legacyAmpersand().deserialize(loreLine));
	}

	public void addToLore(Component loreLine) {
		lore.add(loreLine);
	}

	public Inventory addToInventory(Inventory inv) {
		meta.lore(lore);
		item.setItemMeta(meta);
		inv.setItem(position, item);
		return inv;
	}

	public void addEnchantement(Enchantment enchantment, int level) {
		meta.addEnchant(enchantment, level, false);
	}

	public void clearEnchantements() {
		Arrays.stream(Enchantment.values()).forEach(meta::removeEnchant);
	}

	public ItemStack getItem() {
		meta.lore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public void setPlayerHeadTexture(String base64) {
		if(meta instanceof SkullMeta) {
			// Borrow a player's profile
			PlayerProfile borrowedProfile = Bukkit.getServer().getOnlinePlayers().iterator().next().getPlayerProfile();
			// Override the texture with provided one
			borrowedProfile.setProperty(new ProfileProperty("textures", base64));
			// Set profile to skull
			((SkullMeta) meta).setPlayerProfile(borrowedProfile);
		}
	}

	public void clearLore() {
		lore.clear();
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public DyeColor getColor() {
		try {
			String colorName = item.getType().toString().split("_", 2)[0];
			return DyeColor.valueOf(colorName);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
}