package space.arim.advancedbanmigrator;

import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		new AdvancedBanMigrator(getDataFolder().toPath()).run();
		getLogger().info("Finished migration");
	}

}
