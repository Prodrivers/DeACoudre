package me.poutineqc.deacoudre.achievements;

import me.poutineqc.deacoudre.DeACoudre;
import me.poutineqc.deacoudre.PlayerData;

import java.util.ArrayList;
import java.util.List;


public class TopManager {
	private static List<TopManager> games = new ArrayList<>();
	private static List<TopManager> won = new ArrayList<>();
	private static List<TopManager> lost = new ArrayList<>();
	private static List<TopManager> dacdone = new ArrayList<>();
	private PlayerData playerData;
	private String player;
	private double score;

	public TopManager(DeACoudre plugin) {
		this.playerData = plugin.getPlayerData();
		updateAll();
	}

	public TopManager(String name, double score) {
		this.player = name;
		this.score = score;
	}

	public static List<TopManager> getGames() {
		return games;
	}

	public static List<TopManager> getWon() {
		return won;
	}

	public static List<TopManager> getLost() {
		return lost;
	}

	public static List<TopManager> getDaCdone() {
		return dacdone;
	}

	public void updateAll() {
		games = updateTop("gamesPlayed");
		won = updateTop("gamesWon");
		lost = updateTop("gamesLost");
		dacdone = updateTop("DaCdone");
	}

	private List<TopManager> updateTop(String lookup) {
		List<TopManager> tempList = new ArrayList<>();
		if(playerData.getData().contains("players")) {
			for(String uuid : playerData.getData().getConfigurationSection("players").getKeys(false)) {
				String name = playerData.getData().getString("players." + uuid + ".name", "unknown");
				double score = playerData.getData().getDouble("players." + uuid + "." + lookup, 0);
				tempList.add(0, new TopManager(name, score));

				for(int i = 0; i < 10 && i < tempList.size() - 1; i++) {
					if(tempList.get(i).score < tempList.get(i + 1).score) {
						TopManager tempValue = tempList.get(i);
						tempList.set(i, tempList.get(i + 1));
						tempList.set(i + 1, tempValue);
					}
				}
			}
		}

		return tempList;
	}

	protected String getPlayer() {
		return player;
	}

	protected double getScore() {
		return score;
	}

}
