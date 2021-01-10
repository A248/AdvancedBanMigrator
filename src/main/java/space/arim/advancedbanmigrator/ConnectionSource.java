package space.arim.advancedbanmigrator;

import java.sql.Connection;
import java.sql.SQLException;

interface ConnectionSource {

	Connection openConnection() throws SQLException;
}
