package me.poutineqc.deacoudre.tools;

import org.bukkit.entity.Player;

import me.poutineqc.deacoudre.DeACoudre;

public class Utils {

	public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
		if (!DeACoudre.NMS_VERSION.equals("v1_8_R1")) {
			if (nmsClassString.equals("ChatSerializer"))
				nmsClassString = "IChatBaseComponent$ChatSerializer";

			if (nmsClassString.equals("EnumTitleAction"))
				nmsClassString = "PacketPlayOutTitle$EnumTitleAction";
		}

		return Class.forName("net.minecraft.server." + DeACoudre.NMS_VERSION + "." + nmsClassString);
	}

	private static void sendPacket(Player p, Object packet) {
		try {
			Object player = p.getClass().getMethod("getHandle").invoke(p);
			Object connection = player.getClass().getField("playerConnection").get(player);
			connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendTitle(Player p, String title, String subtitle, int paramInt1, int paramInt2, int paramInt3) {
		try {
			Object titleEnum = getNMSClass("EnumTitleAction").getEnumConstants()[0];
			Object subtitleEnum = getNMSClass("EnumTitleAction").getEnumConstants()[1];

			Object timePacket = getNMSClass("PacketPlayOutTitle").getConstructor(int.class, int.class, int.class)
					.newInstance(paramInt1, paramInt2, paramInt3);
			Object titlePacket = getNMSClass("PacketPlayOutTitle")
					.getConstructor(getNMSClass("EnumTitleAction"),
							getNMSClass("IChatBaseComponent"))
					.newInstance(titleEnum, getJsonMessage(title));
			Object subtitlePacket = getNMSClass("PacketPlayOutTitle")
					.getConstructor(getNMSClass("EnumTitleAction"),
							getNMSClass("IChatBaseComponent"))
					.newInstance(subtitleEnum, getJsonMessage(subtitle));

			sendPacket(p, timePacket);
			sendPacket(p, titlePacket);
			sendPacket(p, subtitlePacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Object getJsonMessage(String json) throws Exception {
		return getNMSClass("ChatSerializer").getMethod("a", String.class).invoke(null, json);
	}
}
