package no.runsafe.warpdrive.database;

import no.runsafe.framework.api.IOutput;
import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.IRow;
import no.runsafe.framework.api.database.IValue;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.minecraft.RunsafeLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WarpRepository extends Repository
{
	public WarpRepository(IDatabase db, IOutput output)
	{
		database = db;
		console = output;
	}

	@Override
	public String getTableName()
	{
		return "warpdrive_locations";
	}

	@Override
	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
	{
		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
		ArrayList<String> sql = new ArrayList<String>();
		sql.add(
			"CREATE TABLE warpdrive_locations (" +
				"`creator` varchar(255) NOT NULL," +
				"`name` varchar(255) NOT NULL," +
				"`public` bit NOT NULL," +
				"`world` varchar(255) NOT NULL," +
				"`x` double NOT NULL," +
				"`y` double NOT NULL," +
				"`z` double NOT NULL," +
				"`yaw` double NOT NULL," +
				"`pitch` double NOT NULL," +
				"PRIMARY KEY(`creator`,`name`,`public`)" +
				")"
		);
		queries.put(1, sql);
		return queries;
	}

	public void Persist(String creator, String name, boolean publicWarp, RunsafeLocation location)
	{
		database.Update(
			"INSERT INTO warpdrive_locations (creator, name, `public`, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
				"ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch)",
			creator,
			name,
			publicWarp,
			location.getWorld().getName(),
			location.getX(),
			location.getY(),
			location.getZ(),
			location.getYaw(),
			location.getPitch()
		);
		String key = cacheKey(creator, name, publicWarp);
		if (cache.containsKey(key))
			cache.remove(key);
		cache.put(key, location);
	}

	public List<String> GetPublicList()
	{
		return GetWarps(null, true);
	}

	public List<String> GetPrivateList(String owner)
	{
		return GetWarps(owner, false);
	}

	public RunsafeLocation GetPublic(String name)
	{
		return GetWarp("", name, true);
	}

	public RunsafeLocation GetPrivate(String owner, String name)
	{
		return GetWarp(owner, name, false);
	}

	public void DelPublic(String name)
	{
		DelWarp("", name, true);
	}

	public void DelPrivate(String owner, String name)
	{
		DelWarp(owner, name, false);
	}

	public void DelAllPrivate(String world)
	{
		database.Execute("DELETE FROM warpdrive_locations WHERE world=? AND public=?", world, false);
	}

	private String cacheKey(String creator, String name, boolean publicWarp)
	{
		if (publicWarp)
			return name;
		return String.format("%s:%s", creator, name);
	}

	private List<String> GetWarps(String owner, boolean publicWarp)
	{
		ArrayList<String> names = new ArrayList<String>();
		List<IValue> result;
		if (publicWarp)
			result = database.QueryColumn("SELECT name FROM warpdrive_locations WHERE `public`=?", publicWarp);
		else
			result = database.QueryColumn("SELECT name FROM warpdrive_locations WHERE `public`=? AND creator=?", publicWarp, owner);

		if (result != null)
			for (IValue entry : result)
				names.add(entry.String().toLowerCase());
		return names;
	}

	private RunsafeLocation GetWarp(String owner, String name, boolean publicWarp)
	{
		String key = cacheKey(owner, name, publicWarp);
		if (cache.containsKey(key))
			return cache.get(key);

		IRow data;
		if (publicWarp)
			data = database.QueryRow(
				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=?",
				name, publicWarp
			);
		else
			data = database.QueryRow(
				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=? AND creator=?",
				name, publicWarp, owner
			);

		if (data == null)
			return null;

		RunsafeLocation location = data.Location();
		console.finer(
			"[%.2f,%.2f,%.2f y:%.2f p:%.2f]",
			location.getX(),
			location.getY(),
			location.getZ(),
			location.getYaw(),
			location.getPitch()
		);

		cache.put(key, location);
		return location;
	}

	private void DelWarp(String owner, String name, boolean publicWarp)
	{
		if (publicWarp)
			database.Execute("DELETE FROM warpdrive_locations WHERE name=? AND public=?", name, publicWarp);
		else
			database.Execute("DELETE FROM warpdrive_locations WHERE name=? AND public=? AND creator=?", name, publicWarp, owner);
		String key = cacheKey(owner, name, publicWarp);
		if (cache.containsKey(key))
			cache.remove(key);
	}

	private final IDatabase database;
	private final ConcurrentHashMap<String, RunsafeLocation> cache = new ConcurrentHashMap<String, RunsafeLocation>();
	private final IOutput console;
}
