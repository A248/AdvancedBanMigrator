package space.arim.advancedbanmigrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class Queries {

	private Queries() {}

	static void createSchema(ConnectionSource source) throws SQLException {
		for (String table : new String[] {"Punishments", "PunishmentHistory"}) {
			execute(source,
					"CREATE TABLE " + table + " (" +
							"id INTEGER IDENTITY PRIMARY KEY," +
							"name VARCHAR(16)," +
							"uuid VARCHAR(35)," +
							"reason VARCHAR(100)," +
							"operator VARCHAR(16)," +
							"punishmentType VARCHAR(16)," +
							"start BIGINT," +
							"end BIGINT," +
							"calculation VARCHAR(50))");
		}
	}

	static void execute(ConnectionSource source, String statement, Object... parameters) throws SQLException {
		try (Connection conn = source.openConnection();
			 PreparedStatement prepStmt = conn.prepareStatement(statement)) {
			for (int n = 0; n < parameters.length; n++) {
				prepStmt.setObject(n + 1, parameters[n]);
			}
			prepStmt.execute();
		}
	}
}
