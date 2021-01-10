package space.arim.advancedbanmigrator;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfHeader;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.annote.SubSection;

import java.sql.DriverManager;

@ConfHeader("Configure migration settings here.")
public interface Config {

	@ConfComments({
			"Most users will want to convert from HSQLDB -> MySQL.",
			"If you want to convert from MySQL to HSQLDB, set this to true"
	})
	@ConfDefault.DefaultBoolean(false)
	boolean reverse();

	@ConfComments("The amount of punishments to transfer in a single batch")
	@ConfDefault.DefaultInteger(200)
	@ConfKey("batch-amount")
	int batchAmount();

	@SubSection
	@ConfKey("mysql-settings")
	MySqlSettings mySqlSettings();

	@ConfHeader("Specify the exact same MySQL/MariaDB credentials you use for AdvancedBan")
	interface MySqlSettings {

		@ConfDefault.DefaultString("user")
		String username();

		@ConfDefault.DefaultString("pass")
		String password();

		@ConfDefault.DefaultString("localhost")
		String host();

		@ConfDefault.DefaultInteger(3306)
		int port();

		@ConfDefault.DefaultString("bans")
		String database();

		@ConfDefault.DefaultString("verifyServerCertificate=false&useSSL=false&useUnicode=true&characterEncoding=utf8")
		String properties();

		default ConnectionSource toConnectionSource() {
			String jdbcUrl = "jdbc:mysql://" + host() + ":" + port() + "/" + database() + "?" + properties();
			String username = username();
			String password = password();
			return () -> DriverManager.getConnection(jdbcUrl, username, password);
		}

	}
}
