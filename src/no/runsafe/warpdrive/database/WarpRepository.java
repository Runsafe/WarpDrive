package no.runsafe.warpdrive.database;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;

import java.util.List;

public class WarpRepository extends Repository
{
	public WarpRepository(IScheduler scheduler)
	{
		cache = new TimedCache<String, ILocation>(scheduler);
	}

	@Override
	public String getTableName()
	{
		return "warpdrive_locations";
	}

	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE warpdrive_locations (" +
				"`creator` varchar(20) NOT NULL," +
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
		update.addQueries( // Add a column for the creator's UUID.
			String.format("ALTER TABLE %s ADD COLUMN `creator_id` VARCHAR(36) NOT NULL DEFAULT 'default'", getTableName())
		);
		return update;
	}

	public void Persist(IPlayer creator, String name, boolean publicWarp, ILocation location)
	{
		String creatorName = "";
		String creatorId = "";
		if (creator != null)
		{
			creatorName = creator.getName();
			creatorId = creator.getUniqueId().toString();
		}

		database.update(
			"INSERT INTO warpdrive_locations (creator, creator_id, name, `public`, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
				"ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch)",
			creatorName,
			creatorId,
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
		cache.Invalidate(key);
		cache.Cache(key, location);
	}

	public List<String> GetPublicList()
	{
		return GetWarps(null, true);
	}

	public List<String> GetPrivateList(IPlayer owner)
	{
		return GetWarps(owner, false);
	}

	public ILocation GetPublic(String name)
	{
		return GetWarp(null, name, true);
	}

	public ILocation GetPrivate(IPlayer owner, String name)
	{
		return GetWarp(owner, name, false);
	}

	public boolean DelPublic(String name)
	{
		return DelWarp(null, name, true);
	}

	public boolean DelPrivate(IPlayer owner, String name)
	{
		return DelWarp(owner, name, false);
	}

	public void DelAllPrivate(String world)
	{
		database.execute("DELETE FROM warpdrive_locations WHERE world=? AND public=?", world, false);
	}

	private String cacheKey(IPlayer creator, String name, boolean publicWarp)
	{
		if (publicWarp)
			return name;

		String creatorName = "";
		if (creator != null)
			creatorName = creator.getName();

		return String.format("%s:%s", creatorName, name);
	}

	private List<String> GetWarps(IPlayer owner, boolean publicWarp)
	{
		if (publicWarp)
			return database.queryStrings("SELECT name FROM warpdrive_locations WHERE `public`=1");
		else
		{
			String ownerId = "";
			if (owner != null)
				ownerId = owner.getUniqueId().toString();
			return database.queryStrings("SELECT name FROM warpdrive_locations WHERE `public`=0 AND creator_id=?", ownerId);
		}
	}

	private ILocation GetWarp(IPlayer owner, String name, boolean publicWarp)
	{
		String key = cacheKey(owner, name, publicWarp);
		ILocation location = cache.Cache(key);
		if (location != null)
			return location;

		if (publicWarp)
			location = database.queryLocation(
				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=1",
				name
			);
		else
		{
			String ownerId = "";
			if (owner != null)
				ownerId = owner.getUniqueId().toString();
			location = database.queryLocation(
				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=0 AND creator_id=?",
				name, ownerId
			);
		}

		return cache.Cache(key, location);
	}

	private boolean DelWarp(IPlayer owner, String name, boolean publicWarp)
	{
		if (GetWarp(owner, name, publicWarp) == null)
			return false;
		boolean success;
		if (publicWarp)
			success = database.execute("DELETE FROM warpdrive_locations WHERE name=? AND public=1", name);
		else
		{
			String ownerId = "";
			if (owner != null)
				ownerId = owner.getUniqueId().toString();
			success = database.execute("DELETE FROM warpdrive_locations WHERE name=? AND public=0 AND creator_id=?", name, ownerId);
		}
		cache.Invalidate(cacheKey(owner, name, publicWarp));
		return success;
	}

	private final TimedCache<String, ILocation> cache;
}
