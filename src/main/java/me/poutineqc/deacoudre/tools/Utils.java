package me.poutineqc.deacoudre.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.title.Title;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.RegExp;

import java.time.Duration;
import java.util.HashMap;

public class Utils {
	private static final HashMap<DyeColor, Material> colorToStainedGlassBlock = new HashMap<>();

	// https://minecraft-heads.com/custom-heads/miscellaneous/37791-refresh
	private static final String RANDOM_TEXTURE_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmM4ZWExZjUxZjI1M2ZmNTE0MmNhMTFhZTQ1MTkzYTRhZDhjM2FiNWU5YzZlZWM4YmE3YTRmY2I3YmFjNDAifX19";

	private static final HashMap<DyeColor, String> colorToBase64HeadTexture = new HashMap<>();

	static {
		colorToStainedGlassBlock.put(DyeColor.BLACK, Material.BLACK_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.BLUE, Material.BLUE_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.BROWN, Material.BROWN_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.CYAN, Material.CYAN_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.GRAY, Material.GRAY_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.GREEN, Material.GREEN_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.LIGHT_BLUE, Material.LIGHT_BLUE_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.LIGHT_GRAY, Material.LIGHT_GRAY_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.LIME, Material.LIME_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.MAGENTA, Material.MAGENTA_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.ORANGE, Material.ORANGE_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.PINK, Material.PINK_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.PURPLE, Material.PURPLE_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.RED, Material.RED_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.WHITE, Material.WHITE_STAINED_GLASS);
		colorToStainedGlassBlock.put(DyeColor.YELLOW, Material.YELLOW_STAINED_GLASS);
	}

	static {
		colorToBase64HeadTexture.put(DyeColor.BLACK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY3YTJmMjE4YTZlNmUzOGYyYjU0NWY2YzE3NzMzZjRlZjliYmIyODhlNzU0MDI5NDljMDUyMTg5ZWUifX19"); // https://minecraft-heads.com/custom/miscellaneous/6265-black-000000
		colorToBase64HeadTexture.put(DyeColor.BLUE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmE0NjA1MzAxMmM2OGYyODlhYmNmYjE3YWI4MDQyZDVhZmJhOTVkY2FhOTljOTljMWUwMzYwODg2ZDM1In19fQ=="); // https://minecraft-heads.com/custom-heads/miscellaneous/6251-dark-blue-00008b
		colorToBase64HeadTexture.put(DyeColor.BROWN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA5YTdlY2ZjN2FiOTIyMzY4ZGFkMWQ4NDJkZTNmZjUzOTM2OWE1ZGE2YmY4OTVhYjNkMjVjZmEwNDA1OTAifX19"); // https://minecraft-heads.com/custom/miscellaneous/6159-rosy-brown-bc8f8f
		colorToBase64HeadTexture.put(DyeColor.CYAN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDdjNzhmM2VlNzgzZmVlY2QyNjkyZWJhNTQ4NTFkYTVjNDMyMzA1NWViZDJmNjgzY2QzZTgzMDJmZWE3YyJ9fX0="); // https://minecraft-heads.com/custom/miscellaneous/6252-cyan-00ffff
		colorToBase64HeadTexture.put(DyeColor.GRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmExN2U5NzAzN2NlMzUzZjg1ZjVjNjVkZjQzNWQyOTQ0OWE4OGRhNDQ0MmU0MzYxY2Y5OWFiYmUxZjg5MmZiIn19fQ=="); // https://minecraft-heads.com/custom-heads/miscellaneous/6223-gray-808080
		colorToBase64HeadTexture.put(DyeColor.GREEN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM5ZTYwMWVkOTE5OGRiYjM0YzUxZGRmMzIzOTI5ZjAxYTVmOTU4YWIxMTEzM2UzZTA0MDdiNjk4MzkzYjNmIn19fQ=="); // https://minecraft-heads.com/custom-heads/miscellaneous/6247-dark-green-006400
		colorToBase64HeadTexture.put(DyeColor.LIGHT_BLUE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWY4NjkwNDhmMDZkMzE4ZTUwNThiY2EwYTg3NmE1OTg2MDc5ZjQ1YTc2NGQxMmFiMzRhNDkzMWRiNmI4MGFkYyJ9fX0="); // https://minecraft-heads.com/custom-heads/miscellaneous/31698-lapis-lazuli-456ed1
		colorToBase64HeadTexture.put(DyeColor.LIGHT_GRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQzY2ZjMjM5MDA2YjI1N2I4YjIwZjg1YTdiZjQyMDI2YzRhZGEwODRjMTQ0OGQwNGUwYzQwNmNlOGEyZWEzMSJ9fX0="); // https://minecraft-heads.com/custom-heads/miscellaneous/31693-light-gray-c8c8c8
		colorToBase64HeadTexture.put(DyeColor.LIME, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc2OTVmOTZkZGE2MjZmYWFhMDEwZjRhNWYyOGE1M2NkNjZmNzdkZTBjYzI4MGU3YzU4MjVhZDY1ZWVkYzcyZSJ9fX0="); // https://minecraft-heads.com/custom-heads/miscellaneous/31700-lime-green-83d41c
		colorToBase64HeadTexture.put(DyeColor.MAGENTA, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VmMGM1NzczZGY1NjBjYzNmYzczYjU0YjVmMDhjZDY5ODU2NDE1YWI1NjlhMzdkNmQ0NGYyZjQyM2RmMjAifX19"); // https://minecraft-heads.com/custom/miscellaneous/6194-magenta-ff00ff
		colorToBase64HeadTexture.put(DyeColor.ORANGE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmMyZTk3MmFmYTkxMTViNmQzMjA3NWIxZjFiN2ZlZDdhYTI5YTUzNDFjMTAyNDI4ODM2MWFiZThlNjliNDYifX19"); // https://minecraft-heads.com/custom/miscellaneous/6173-orange-red-ff4500
		colorToBase64HeadTexture.put(DyeColor.PINK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmExMWE1MWY2NTc2Mjg1ZjlmOWM4YWE3ZGVkMWVlMTJjMjAyZjE1ZDc5MWM3MzJiNTg2ZGRhZTcwNjRiMCJ9fX0="); // https://minecraft-heads.com/custom/miscellaneous/6164-pink-ffc0cb
		colorToBase64HeadTexture.put(DyeColor.PURPLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTMyYWUyY2I4ZDJhZTYxNTE0MWQyYzY1ODkyZjM2NGZjYWRkZjczZmRlYzk5YmUxYWE2ODc0ODYzZWViNWMifX19"); // https://minecraft-heads.com/custom-heads/miscellaneous/6161-purple-800080
		colorToBase64HeadTexture.put(DyeColor.RED, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ3MjM3NDM3ZWVmNjM5NDQxYjkyYjIxN2VmZGM4YTcyNTE0YTk1NjdjNmI2YjgxYjU1M2Y0ZWY0YWQxY2FlIn19fQ=="); // https://minecraft-heads.com/custom/miscellaneous/6230-firebrick-b22222
		colorToBase64HeadTexture.put(DyeColor.WHITE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY2YTVjOTg5MjhmYTVkNGI1ZDViOGVmYjQ5MDE1NWI0ZGRhMzk1NmJjYWE5MzcxMTc3ODE0NTMyY2ZjIn19fQ=="); // https://minecraft-heads.com/custom/miscellaneous/6137-white-ffffff
		colorToBase64HeadTexture.put(DyeColor.YELLOW, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjlhMDMwY2EyYjJjNmZlNjdmZTgwOTdkM2NkMjA2OTY5ZmM1YzAwMTdjNjBiNmI0MDk5MGM3NzJhNmYwYWMwYSJ9fX0="); // https://minecraft-heads.com/custom-heads/miscellaneous/31702-dandelion-yellow-e7e769
	}

	public static ItemStackManager getRandomHead() {
		ItemStackManager item = new ItemStackManager(Material.PLAYER_HEAD);
		item.setPlayerHeadTexture(RANDOM_TEXTURE_BASE64);
		return item;
	}

	public static ItemStackManager getColorHead(DyeColor color) {
		String base64Texture = colorToBase64HeadTexture.get(color);
		if(base64Texture != null) {
			ItemStackManager item = new ItemStackManager(Material.PLAYER_HEAD);
			item.setPlayerHeadTexture(base64Texture);
			return item;
		}
		return null;
	}

	public static Material colorToStainedGlassBlock(DyeColor color) {
		return colorToStainedGlassBlock.get(color);
	}

	public static void sendTitle(Player target, Component title, Component subtitle, int fadeInMs, int stayMs, int fadeOutMs) {
		final Title.Times times = Title.Times.of(Duration.ofMillis(fadeInMs), Duration.ofMillis(stayMs), Duration.ofMillis(fadeOutMs));
		final Title playerTitle = Title.title(title, (subtitle != null ? subtitle : Component.empty()), times);
		target.showTitle(playerTitle);
	}

	public static Component replaceInComponent(Component message, @RegExp String pattern, Component replacement) {
		return Component.empty()
				.append(message)
				.replaceText(
						TextReplacementConfig.builder()
								.match(pattern)
								.replacement(replacement)
								.build()
				);
	}
}
