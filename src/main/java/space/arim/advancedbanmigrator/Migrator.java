package space.arim.advancedbanmigrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class Migrator {

	private final Connection source;
	private final Connection destination;

	private static final int BATCH_COUNT = 200;

	Migrator(Connection source, Connection destination) {
		this.source = source;
		this.destination = destination;
	}

	void conduct(String table) throws SQLException {
		try (PreparedStatement selectStatement = source.prepareStatement("SELECT * FROM " + table);
			 PreparedStatement insertStatement = destination.prepareStatement(
			 		"INSERT INTO " + table + " (id, name, uuid, reason, operator, punishmentType, start, end, calculation) " +
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
			selectStatement.setFetchSize(BATCH_COUNT);
			try (ResultSet resultSet = selectStatement.executeQuery()) {
				int batches = 0;
				int bufferCount = 0;
				while (resultSet.next()) {
					Object[] params = new Object[] {
							resultSet.getInt("id"),
							resultSet.getString("name"),
							resultSet.getString("uuid"),
							resultSet.getString("reason"),
							resultSet.getString("operator"),
							resultSet.getString("punishmentType"),
							resultSet.getLong("start"),
							resultSet.getLong("end"),
							resultSet.getString("calculation")};
					for (int n = 0; n < params.length; n++) {
						insertStatement.setObject(n + 1, params[n]);
					}
					insertStatement.addBatch();
					if (++bufferCount == BATCH_COUNT) {
						bufferCount = 0;
						System.out.println("Executing batch update " + ++batches);
						insertStatement.executeBatch();
						destination.commit();
					}
				}
				if (bufferCount > 0) {
					insertStatement.executeBatch();
					destination.commit();
				}
				int totalTransferred = batches * BATCH_COUNT + bufferCount;
				System.out.println("Transferred " + totalTransferred + " punishments");
			}
		}
	}
}
