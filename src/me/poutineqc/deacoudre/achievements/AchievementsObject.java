package me.poutineqc.deacoudre.achievements;

public class AchievementsObject {
	private int level;
	private double reward;
	
	public AchievementsObject(int level, double reward) {
		this.level = level;
		this.reward = reward;
	}
	
	public int get_level() {
		return level;
	}
	
	public double get_reward() {
		return reward;
	}
}
