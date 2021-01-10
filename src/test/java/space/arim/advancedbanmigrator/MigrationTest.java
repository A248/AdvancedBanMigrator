package space.arim.advancedbanmigrator;

import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.db.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
		Queries.createSchema(from);
		Queries.createSchema(to);
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
		Queries.execute(from, "INSERT INTO PunishmentHistory VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				id, name, uuid, reason, operator, punishmentType, start, end, null);
		if (active) {
			Queries.execute(from, "INSERT INTO Punishments VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
