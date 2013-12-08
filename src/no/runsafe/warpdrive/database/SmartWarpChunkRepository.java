package no.runsafe.warpdrive.database;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmartWarpChunkRepository extends Repository
{
	public SmartWarpChunkRepository(IDatabase database)
	{
		this.database = database;
	}

	@Override
	public String getTableName()
	{
		return "smartwarp_targets";
	}

	@Override
	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
	{
		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
		ArrayList<String> sql = new ArrayList<String>();
		sql.add(
			"CREATE TABLE smartwarp_targets (" +
				"`world` varchar(255) NOT NULL," +
				"`x` int NOT NULL," +
				"`y` int NOT NULL," +
				"`z` int NOT NULL," +
				"`safe` bit NOT NULL," +
				"`cave` bit NOT NULL," +
				"PRIMARY KEY(`world`,`x`,`y`,`z`)" +
				")"
		);
		queries.put(1, sql);
		return queries;
	}

	public ILocation getTarget(IWorld world, boolean cave)
	{
		return database.QueryLocation(
			"SELECT world, x, y, z FROM smartwarp_targets WHERE world=? AND safe=true AND cave=? ORDER BY RAND() LIMIT 1",
			world.getName(), cave
		);
	}

	public void setUnsafe(ILocation candidate)
	{
		database.Update(
			"UPDATE smartwarp_targets SET safe=false WHERE world=? AND x=? AND y=? AND z=?",
			candidate.getWorld().getName(),
			candidate.getBlockX(), candidate.getBlockY(), candidate.getBlockZ()
		);
	}

	public void saveTarget(ILocation target, boolean safe, boolean cave)
	{
		database.Update(
			"INSERT INTO smartwarp_targets (world, x, y, z, safe, cave) VALUES (?, ?, ?, ?, ?, ?)" +
				" ON DUPLICATE KEY UPDATE safe=VALUES(safe), cave=VALUES(cave)",
			target.getWorld().getName(),
			target.getBlockX(), target.getBlockY(), target.getBlockZ(),
			safe, cave
		);
	}

	private final IDatabase database;
}
