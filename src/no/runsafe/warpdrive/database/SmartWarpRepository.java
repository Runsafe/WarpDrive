package no.runsafe.warpdrive.database;

import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;

import java.util.List;

public class SmartWarpRepository extends Repository
{
	@Override
	public String getTableName()
	{
		return "smartwarp_settings";
	}

	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE smartwarp_settings (" +
				"`world` varchar(255) NOT NULL," +
				"`range` integer NOT NULL," +
				"`progress` double NOT NULL," +
				"PRIMARY KEY(`world`)" +
			")"
		);

		return update;
	}

	public int getRange(String world)
	{
		Integer range = database.queryInteger("SELECT `range` FROM smartwarp_settings WHERE world=?", world);
		return range == null ? -1 : range;
	}

	public void setProgress(String world, double progress)
	{
		database.update(
			"UPDATE smartwarp_settings SET progress=? WHERE world=?",
			progress, world
		);
	}

	public double getProgress(String world)
	{
		Double progress = database.queryDouble("SELECT progress FROM smartwarp_settings WHERE world=?", world);
		return progress == null ? -1 : progress;
	}

	public void setRange(String world, int range)
	{
		database.update(
			"INSERT INTO smartwarp_settings (`world`, `range`, `progress`) VALUES (?, ?, 0)" +
				" ON DUPLICATE KEY UPDATE `range`=VALUES(`range`)",
			world, range
		);
	}

	public List<String> getWorlds()
	{
		return database.queryStrings("SELECT world FROM smartwarp_settings");
	}
}
