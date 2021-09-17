
# AdvancedBanMigrator

Migrates AdvancedBan data from HSQLDB to MySQL or vice-versa.

## Usage

The target database is the database you want to use.

The source database is the database you have your data in.

1. Configure AdvancedBan to use the target database. Make sure you do not add any data to AdvancedBan, yet. 
  * Do not ban, mute, or otherwise use AdvancedBan commands.
  * Just make sure AdvancedBan is installed and configured to use the target database.
2. Install AdvancedBanMigrator.
3. Start the server once.
4. Add your database credentials to the config.yml. Configure which database is the target database.
  * If you want to, you can migrate from MySQL to HSQLDB, but most users will migrate from HSQLDB to MySQL.
5. Restart the server.
6. AdvancedBanMigrator will now perform the import.
7. Your punishments should now be transferred. 
  * You should remove this plugin from your server.
  * If you forget to remove the plugin, it will try to migrate your data again. This isn't dangerous and it is OK if this happens. The duplicate migration will fail because the data has already been transferred.
