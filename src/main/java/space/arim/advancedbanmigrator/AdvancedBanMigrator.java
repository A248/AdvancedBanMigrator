package space.arim.advancedbanmigrator;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.helper.ConfigurationHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

class AdvancedBanMigrator {

	private final Path dataFolder;

	AdvancedBanMigrator(Path dataFolder) {
		this.dataFolder = dataFolder;
	}

	void run() {
		Config config;
		try {
			config = new ConfigurationHelper<>(dataFolder, "config.yml",
					new SnakeYamlConfigurationFactory<>(Config.class, ConfigurationOptions.defaults()))
					.reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InvalidConfigException ex) {
			System.err.println("Your configuration is invalid. Fix it and try again");
			ex.printStackTrace();
			return;
		}
		if (config.mySqlSettings().isDefault()) {
			System.out.println("Please configure your database settings, then restart the server");
			return;
		}
		migrate(dataFolder, config);
	}

	private void migrate(Path dataFolder, Config config) {
		Path pluginsFolder = dataFolder.toAbsolutePath().getParent();
		Path advancedBanData = pluginsFolder.resolve("AdvancedBan").resolve("data");
		if (!Files.isDirectory(advancedBanData)) {
			throw new IllegalStateException("plugins/AdvancedBan/data does not exist as a directory");
		}
		Path storage = advancedBanData.resolve("storage");
		ConnectionSource hsqldb = () -> DriverManager.getConnection("jdbc:hsqldb:file:" + storage + ";hsqldb.lock_file=false");
		ConnectionSource mysql = config.mySqlSettings().toConnectionSource();
		Migration migration = new Migration(config.batchAmount());
		if (config.reverse()) {
			migration.migrate(mysql, hsqldb);
		} else {
			migration.migrate(hsqldb, mysql);
		}
	}

}
