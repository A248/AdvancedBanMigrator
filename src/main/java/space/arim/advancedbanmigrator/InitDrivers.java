package space.arim.advancedbanmigrator;

class InitDrivers {

	void ensureRegistered() {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("HSQLDB driver missing", ex);
		}
		Throwable firstEx = null;
		String[] driverClassNames = new String[] {
				"org.mariadb.jdbc.Driver", "com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"};
		for (String potentialDriverClass : driverClassNames) {
			try {
				Class.forName(potentialDriverClass);
				return;
			} catch (ClassNotFoundException ex) {
				if (firstEx == null) {
					firstEx = ex;
				} else {
					firstEx.addSuppressed(ex);
				}
			}
		}
		throw new RuntimeException("Neither MySQL nor MariaDB driver found", firstEx);
	}

}
