package no.runsafe.warpdrive.database;

import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.internal.database.Repository;
import no.runsafe.framework.internal.database.Row;
import no.runsafe.framework.internal.database.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmartWarpRepository extends Repository
{
	public SmartWarpRepository(IDatabase database)
	{
		this.database = database;
	}

	@Override
	public String getTableName()
	{
		return "smartwarp_settings";
	}

	@Override
	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
	{
		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
		ArrayList<String> sql = new ArrayList<String>();
		sql.add(
			"CREATE TABLE smartwarp_settings (" +
				"`world` varchar(255) NOT NULL," +
				"`range` integer NOT NULL," +
				"`progress` double NOT NULL," +
				"PRIMARY KEY(`world`)" +
				")"
		);
		queries.put(1, sql);
		return queries;
	}

	public int getRange(String world)
	{
		Row settings = database.QueryRow(
			"SELECT `range` FROM smartwarp_settings WHERE world=?",
			world
		);
		if (settings == null)
			return -1;

		return settings.Integer("range");
	}

	public void setProgress(String world, double progress)
	{
		database.Update(
			"UPDATE smartwarp_settings SET progress=? WHERE world=?",
			progress, world
		);
	}

	public double getProgress(String world)
	{
		Row progress = database.QueryRow(
			"SELECT progress FROM smartwarp_settings WHERE world=?",
			world
		);
		if (progress == null)
			return -1;

		return progress.Double("progress");
	}

	public void setRange(String world, int range)
	{
		database.Update(
			"INSERT INTO smartwarp_settings (`world`, `range`, `progress`) VALUES (?, ?, 0)" +
				" ON DUPLICATE KEY UPDATE `range`=VALUES(`range`)",
				world, range
		);
	}

	public List<String> getWorlds()
	{
		List<String> result = new ArrayList<String>();
		List<Value> worlds = database.QueryColumn("SELECT world FROM smartwarp_settings");

		if (worlds != null)
			for (Value world : worlds)
				result.add(world.String());

		return result;
	}

	private IDatabase database;
}
