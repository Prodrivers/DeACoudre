package me.poutineqc.deacoudre.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.title.Title;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;

public class Utils {
	private static HashMap<DyeColor, Material> colorToStainedGlassBlock = new HashMap<>();

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

	public static Material colorToStainedGlassBlock(DyeColor color) {
		return colorToStainedGlassBlock.get(color);
	}

	public static void sendTitle(Player target, Component title, Component subtitle, int fadeInMs, int stayMs, int fadeOutMs) {
		final Title.Times times = Title.Times.of(Duration.ofMillis(fadeInMs), Duration.ofMillis(stayMs), Duration.ofMillis(fadeOutMs));
		final Title playerTitle = Title.title(title, (subtitle != null ? subtitle : Component.empty()), times);
		target.showTitle(playerTitle);
	}

	public static Component replaceInComponent(Component message, String pattern, Component replacement) {
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
