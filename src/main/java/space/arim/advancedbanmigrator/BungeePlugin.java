package space.arim.advancedbanmigrator;

import net.md_5.bungee.api.plugin.Plugin;

public class BungeePlugin extends Plugin {

	@Override
	public void onEnable() {
		new AdvancedBanMigrator(getDataFolder().toPath()).run();
		getLogger().info("Finished migration");
	}

}
