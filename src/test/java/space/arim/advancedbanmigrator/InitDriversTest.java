package space.arim.advancedbanmigrator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class InitDriversTest {

	@Test
	public void ensureRegistered() {
		assertDoesNotThrow(new InitDrivers()::ensureRegistered);
	}

}
