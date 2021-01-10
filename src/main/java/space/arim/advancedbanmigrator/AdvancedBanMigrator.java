package space.arim.advancedbanmigrator;

import org.bukkit.plugin.java.JavaPlugin;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.helper.ConfigurationHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.logging.Level;

public class AdvancedBanMigrator extends JavaPlugin {

	@Override
	public void onEnable() {
		Path dataFolder = getDataFolder().toPath();
		Config config;
		try {
			config = new ConfigurationHelper<>(dataFolder, "config.yml",
					new SnakeYamlConfigurationFactory<>(Config.class, ConfigurationOptions.defaults()))
					.reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InvalidConfigException ex) {
			getLogger().log(Level.WARNING, "Your configuration is invalid. Fix it and try again", ex);
			return;
		}
		if (config.mySqlSettings().isDefault()) {
			getLogger().info("Please configure your database settings, then restart the server");
			return;
		}
		migrate(dataFolder, config);
	}

	private void migrate(Path dataFolder, Config config) {
		Path pluginsFolder = dataFolder.toAbsolutePath().getParent();
		Path advancedBanData = pluginsFolder.resolve("AdvancedBan").resolve("data");
		Path storage = advancedBanData.resolve("storage");
		if (!Files.isDirectory(advancedBanData)) {
			throw new IllegalStateException("AdvancedBan/data does not exist as a directory");
		}
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException ex) {
			throw new IllegalStateException("AdvancedBan is not present", ex);
		}
		ConnectionSource hsqldb = () -> DriverManager.getConnection("jdbc:hsqldb:file:" + storage + ";hsqldb.lock_file=false");
		ConnectionSource mysql = config.mySqlSettings().toConnectionSource();
		Migration migration = new Migration(config.batchAmount());
		if (config.reverse()) {
			migration.migrate(mysql, hsqldb);
		} else {
			migration.migrate(hsqldb, mysql);
		}
		getLogger().info("Finished migration");
	}

}
