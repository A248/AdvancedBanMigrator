package space.arim.advancedbanmigrator;

import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.assertj.db.api.Assertions.assertThat;

public class MigrationTest {

	private static final AtomicInteger dbCounter = new AtomicInteger();
	private final ConnectionSource from;
	private final ConnectionSource to;

	private final Table destActive;
	private final Table destHistory;

	public MigrationTest() {
		int counterValue = dbCounter.getAndIncrement();
		String sourceUrl = "jdbc:hsqldb:mem:source" + counterValue;
		from = () -> DriverManager.getConnection(sourceUrl);
		String destUrl = "jdbc:hsqldb:mem:dest" + counterValue;
		to = () -> DriverManager.getConnection(destUrl);
		destActive = new Table(new Source(destUrl, null, null), "Punishments");
		destHistory = new Table(new Source(destUrl, null, null), "PunishmentHistory");
	}

	@BeforeEach
	public void setup() throws SQLException {
		for (ConnectionSource source : new ConnectionSource[] {from, to}) {
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
	}

	private void execute(ConnectionSource source, String statement, Object... parameters) throws SQLException {
		try (Connection conn = source.openConnection();
			 PreparedStatement prepStmt = conn.prepareStatement(statement)) {
			for (int n = 0; n < parameters.length; n++) {
				prepStmt.setObject(n + 1, parameters[n]);
			}
			prepStmt.execute();
		}
	}

	@Test
	public void migrateEmpty() {
		assertDoesNotThrow(() -> new Migration(10).migrate(from, to));
		assertThat(destActive).isEmpty();
		assertThat(destHistory).isEmpty();
	}

	private void addPunishment(boolean active,
							   int id, String name, String uuid, String reason, String operator,
							   String punishmentType, long start, long end) throws SQLException {
		execute(from, "INSERT INTO PunishmentHistory VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				id, name, uuid, reason, operator, punishmentType, start, end, null);
		if (active) {
			execute(from, "INSERT INTO Punishments VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
					id, name, uuid, reason, operator, punishmentType, start, end, null);
		}
	}

	@Test
	public void migratePunishments() throws SQLException {
		addPunishment(false, 1, "Name", "uuid", "reason", "operator", "BAN", 0, 1500);
		addPunishment(true, 2, "ObWolf", "unknownuuid", "some reason", "A248", "MUTE", 10, -1);
		addPunishment(false, 3, "Someone", "None", "any reason", "op", "WARNING", 20, 30);

		new Migration(2).migrate(from, to);
		assertThat(destHistory).row()
				.value().isEqualTo(1)
				.value().isEqualTo("Name")
				.value().isEqualTo("uuid")
				.value().isEqualTo("reason")
				.value().isEqualTo("operator")
				.value().isEqualTo("BAN")
				.value().isEqualTo(0)
				.value().isEqualTo(1500);
		assertThat(destActive).row()
				.value().isEqualTo(2)
				.value().isEqualTo("ObWolf")
				.value().isEqualTo("unknownuuid")
				.value().isEqualTo("some reason")
				.value().isEqualTo("A248")
				.value().isEqualTo("MUTE")
				.value().isEqualTo(10)
				.value().isEqualTo(-1);
		assertThat(destHistory).row(1)
				.value().isEqualTo(2)
				.value().isEqualTo("ObWolf")
				.value().isEqualTo("unknownuuid")
				.value().isEqualTo("some reason")
				.value().isEqualTo("A248")
				.value().isEqualTo("MUTE")
				.value().isEqualTo(10)
				.value().isEqualTo(-1);
		assertThat(destHistory).row(2)
				.value().isEqualTo(3)
				.value().isEqualTo("Someone")
				.value().isEqualTo("None")
				.value().isEqualTo("any reason")
				.value().isEqualTo("op")
				.value().isEqualTo("WARNING")
				.value().isEqualTo(20)
				.value().isEqualTo(30);
	}
}
