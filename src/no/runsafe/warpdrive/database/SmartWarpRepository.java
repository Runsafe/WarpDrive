package no.runsafe.warpdrive.database;

import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
		HashMap<Integer, List<String>> queries = new LinkedHashMap<Integer, List<String>>(1);
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

	private final IDatabase database;
}
