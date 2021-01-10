package space.arim.advancedbanmigrator;

import java.sql.Connection;
import java.sql.SQLException;

class Migration {

	void migrate(ConnectionSource from, ConnectionSource to) {
		try (Connection source = from.openConnection();
			 Connection destination = to.openConnection()) {
			destination.setAutoCommit(false);
			Migrator migrator = new Migrator(source, destination);
			migrator.conduct("Punishments");
			migrator.conduct("PunishmentHistory");
		} catch (SQLException ex) {
			throw new RuntimeException("Failed to migrate", ex);
		}
	}

}
