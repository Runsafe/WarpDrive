package no.runsafe.warpdrive.database;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;

import java.util.List;

public class WarpRepository extends Repository
{
	public WarpRepository(IScheduler scheduler, IConsole console)
	{
		cache = new TimedCache<String, ILocation>(scheduler);
		this.console = console;
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
		// Add a column for the creator's UUID.
		update.addQueries(
			String.format(
				"ALTER TABLE %s ADD COLUMN `creator_id` VARCHAR(36) NOT NULL DEFAULT 'default'", getTableName()
			)
		);
		UpdateUUIDs(); // Make sure no one has a default UUID.
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
			"INSERT INTO warpdrive_locations (creator, name, `public`, world, x, y, z, yaw, pitch, creator_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
				"ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch)",
			creatorName,
			name,
			publicWarp,
			location.getWorld().getName(),
			location.getX(),
			location.getY(),
			location.getZ(),
			location.getYaw(),
			location.getPitch(),
			creatorId
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

	/**
	 * Updates players stored with the default UUID to the UUID they have stored in UserControl
	 * Requires player_db from UserControl to be a table in MySQL
	 */
	public void UpdateUUIDs()
	{
		console.logInformation("Updating WarpDrive UUIDs.");
		// Check if any players still have the default UUID
		if (this.database.queryString(String.format("SELECT `creator` FROM `%s` WHERE `creator_id` = 'default'", getTableName())) != null)
		{
			// Copy needed UUIDs over from the player database.
			database.execute(
				String.format(
					"UPDATE `%s` SET `creator_id`=(SELECT `uuid` FROM player_db WHERE `name`=`%s`.`creator`) WHERE `creator_id` = 'default'",
					getTableName(), getTableName()
				)
			);

			// Check if all players now have a UUID.
			if (this.database.queryString(String.format("SELECT `creator` FROM `%s` WHERE `creator_id` = 'default'", getTableName())) != null)
				console.logWarning("Not all players could be assigned a UUID.");
			else
				console.logInformation("All players assigned a UUID.");
		}
		else
			console.logInformation("No UUIDs to update.");
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

	private final IConsole console;
	private final TimedCache<String, ILocation> cache;
}
