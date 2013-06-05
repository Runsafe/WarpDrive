package no.runsafe.warpdrive.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.Repository;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public RunsafeLocation getTarget(RunsafeWorld world, boolean cave)
	{
		Map<String, Object> target = database.QueryRow(
			"SELECT x, y, z FROM smartwarp_targets WHERE world=? AND safe=true AND cave=? ORDER BY RAND() LIMIT 1",
			world.getName(), cave
		);
		if (target == null)
			return null;
		return new RunsafeLocation(
			world,
			(Integer) target.get("x"),
			(Integer) target.get("y"),
			(Integer) target.get("z")
		);
	}

	public void setUnsafe(RunsafeLocation candidate)
	{
		database.Update(
			"UPDATE smartwarp_targets SET safe=false WHERE world=? AND x=? AND y=? AND z=?",
			candidate.getWorld().getName(),
			candidate.getBlockX(), candidate.getBlockY(), candidate.getBlockZ()
		);
	}

	public void saveTarget(RunsafeLocation target, boolean safe, boolean cave)
	{
		database.Update(
			"INSERT INTO smartwarp_targets (world, x, y, z, safe, cave) VALUES (?, ?, ?, ?, ?, ?)" +
				" ON DUPLICATE KEY UPDATE safe=VALUES(safe), cave=VALUES(cave)",
			target.getWorld().getName(),
			target.getBlockX(), target.getBlockY(), target.getBlockZ(),
			safe, cave
		);
	}

	private IDatabase database;
}
