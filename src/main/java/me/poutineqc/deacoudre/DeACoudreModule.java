package me.poutineqc.deacoudre;

import com.google.inject.AbstractModule;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class DeACoudreModule extends AbstractModule {
	private final DeACoudre plugin;

	public DeACoudreModule(DeACoudre plugin) {
		this.plugin = plugin;
	}

	@Override
	protected void configure() {
		bind(Plugin.class).toInstance(this.plugin);
		bind(JavaPlugin.class).toInstance(this.plugin);
		bind(DeACoudre.class).toInstance(this.plugin);
		bind(Configuration.class).toInstance(this.plugin.getConfiguration());
	}
}
