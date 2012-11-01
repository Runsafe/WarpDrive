package no.runsafe.warpdrive.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.ISchemaChanges;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class WarpRepository implements ISchemaChanges
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
		PreparedStatement query = database.prepare(
			"INSERT INTO warpdrive_locations (creator, name, `public`, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
				"ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch)"
		);
		try
		{
			query.setString(1, creator);
			query.setString(2, name);
			query.setBoolean(3, publicWarp);
			query.setString(4, location.getWorld().getName());
			query.setDouble(5, location.getX());
			query.setDouble(6, location.getY());
			query.setDouble(7, location.getZ());
			query.setFloat(8, location.getYaw());
			query.setFloat(9, location.getPitch());
			query.execute();
			cache.put(cacheKey(creator, name, publicWarp), location);
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
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

	private String cacheKey(String creator, String name, boolean publicWarp)
	{
		if (publicWarp)
			return name;
		return String.format("{0}:{1}", creator, name);
	}

	private RunsafeLocation GetWarp(String owner, String name, boolean publicWarp)
	{
		String key = cacheKey(owner, name, publicWarp);
		if (cache.containsKey(key))
			return cache.get(key);

		PreparedStatement query;
		if (publicWarp)
			query = database.prepare(
				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=?"
			);
		else
			query = database.prepare(
				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=? AND creator=?"
			);
		try
		{
			query.setString(1, name);
			query.setBoolean(2, publicWarp);
			if (!publicWarp)
				query.setString(3, owner);
			ResultSet result = query.executeQuery();
			if (result.next())
			{
				RunsafeLocation location = new RunsafeLocation(
					RunsafeServer.Instance.getWorld(result.getString("world")),
					result.getDouble("x"),
					result.getDouble("y"),
					result.getDouble("z"),
					result.getFloat("yaw"),
					result.getFloat("pitch")
				);
				console.outputDebugToConsole(
					String.format(
						"[%.2f,%.2f,%.2f y:%.2f p:%.2f]",
						result.getDouble("x"),
						result.getDouble("y"),
						result.getDouble("z"),
						result.getFloat("yaw"),
						result.getFloat("pitch")
					),
					Level.FINE
				);
				cache.put(key, location);
				return location;
			}
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
		return null;
	}

	private void DelWarp(String owner, String name, boolean publicWarp)
	{
		PreparedStatement query;
		if (publicWarp)
			query = database.prepare(
				"DELETE FROM warpdrive_locations WHERE name=? AND public=?"
			);
		else
			query = database.prepare(
				"DELETE FROM warpdrive_locations WHERE name=? AND public=? AND creator=?"
			);
		try
		{
			query.setString(1, name);
			query.setBoolean(2, publicWarp);
			if (!publicWarp)
				query.setString(3, owner);
			query.execute();
			String key = cacheKey(owner, name, publicWarp);
			if (cache.containsKey(key))
				cache.remove(key);
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	private IDatabase database;
	private HashMap<String, RunsafeLocation> cache = new HashMap<String, RunsafeLocation>();
	private IOutput console;
}
