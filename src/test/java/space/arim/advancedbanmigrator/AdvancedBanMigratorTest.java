package space.arim.advancedbanmigrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdvancedBanMigratorTest {

	@TempDir
	public Path pluginsFolder;
	private Path dataFolder;

	private AdvancedBanMigrator core;

	@BeforeEach
	public void setup() {
		dataFolder = pluginsFolder.resolve("AdvancedBanMigrator");
		core = new AdvancedBanMigrator(dataFolder, new InitDrivers() {
			@Override
			void ensureRegistered() { }
		});
	}

	@Test
	public void runUnconfigured() {
		assertDoesNotThrow(core::run);
	}

	@Test
	public void runWithAdvancedBanData() throws IOException, SQLException {
		Config config = mock(Config.class);
		Config.MySqlSettings mySqlSettings = mock(Config.MySqlSettings.class);
		when(config.batchAmount()).thenReturn(10);
		when(config.mySqlSettings()).thenReturn(mySqlSettings);
		ConnectionSource mySqlConnectionSource = () -> DriverManager.getConnection("jdbc:hsqldb:mem:notreallymysql");
		when(mySqlSettings.toConnectionSource()).thenReturn(mySqlConnectionSource);
		Queries.createSchema(mySqlConnectionSource);

		Path advancedBanData = pluginsFolder.resolve("AdvancedBan").resolve("data");
		Files.createDirectories(advancedBanData);
		Queries.createSchema(() -> DriverManager.getConnection(
				"jdbc:hsqldb:file:" + advancedBanData.resolve("storage") + ";hsqldb.lock_file=false"));
		assertDoesNotThrow(() -> core.migrate(dataFolder, config));
	}
}
