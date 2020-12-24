package me.poutineqc.deacoudre.tools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public class JsonBuilder {

	List<JsonElement> messages = new ArrayList<JsonElement>();

	public void add(JsonElement element) {
		messages.add(element);
	}
	
	public void clear() {
		messages.clear();
	}
	
	public List<JsonElement> getMessages() {
		return messages;
	}

	public String getJson() {
		StringBuilder json = new StringBuilder();

		for (JsonElement message : messages) {
			if (json.length() == 0)
				json.append('[');
			else
				json.append(',');

			json.append(message.generate());
		}

		json.append(']');
		return json.toString();
	}

	public static String getJson(JsonElement element) {
		JsonBuilder builder = new JsonBuilder();
		builder.add(element);
		return builder.getJson();
	}

	public static String getEmpty() {
		JsonBuilder builder = new JsonBuilder();
		builder.add(new JsonElement(""));
		return builder.getJson();
	}

	public static class JsonElement {

		String message;
		boolean bold;
		boolean italic;
		boolean underlined;
		boolean strikethrough;
		boolean obfuscated;
		ChatColor color;

		public JsonElement(String message, ChatColor color, boolean bold, boolean italic, boolean underlined,
				boolean strikethrough, boolean obfuscated) {
			this.message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
			this.color = color;
			this.bold = bold;
			this.italic = italic;
			this.underlined = underlined;
			this.strikethrough = strikethrough;
			this.obfuscated = obfuscated;
		}

		public JsonElement(String message) {
			this.message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
			this.bold = false;
			this.italic = false;
			this.underlined = false;
			this.strikethrough = false;
			this.obfuscated = false;
		}

		public JsonElement(String message, ChatColor color) {
			this.message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
			this.color = color;
			this.bold = false;
			this.italic = false;
			this.underlined = false;
			this.strikethrough = false;
			this.obfuscated = false;
		}

		public void setMessage(String message) {
			this.message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
		}

		public void setColor(ChatColor color) {
			this.color = color;
		}

		public void setBold(boolean bold) {
			this.bold = bold;
		}

		public void setItalic(boolean italic) {
			this.italic = italic;
		}

		public void setUnderlined(boolean underlined) {
			this.underlined = underlined;
		}

		public void setStrikethrough(boolean strikethrough) {
			this.strikethrough = strikethrough;
		}

		public void setObfuscated(boolean obfuscated) {
			this.obfuscated = obfuscated;
		}
		
		public String generate() {
			StringBuilder json = new StringBuilder();

			json.append('{');
			json.append("\"text\":\"" + message + "\"");

			if (color != null)
				json.append(",\"color\":\"" + color.name().toLowerCase() + "\"");

			if (bold)
				json.append(",\"bold\":true");

			if (italic)
				json.append(",\"italic\":true");

			if (underlined)
				json.append(",\"underlined\":true");

			if (strikethrough)
				json.append(",\"strikethrough\":true");

			if (obfuscated)
				json.append(",\"obfuscated\":true");

			json.append('}');

			return json.toString();
		}

	}

}