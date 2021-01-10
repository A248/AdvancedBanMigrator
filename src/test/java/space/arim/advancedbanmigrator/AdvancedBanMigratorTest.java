package space.arim.advancedbanmigrator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AdvancedBanMigratorTest {

	@TempDir
	public Path tempDir;

	@Test
	public void run() {
		AdvancedBanMigrator core = new AdvancedBanMigrator(tempDir);
		assertDoesNotThrow(core::run);
	}
}
