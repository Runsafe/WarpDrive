package no.runsafe.warpdrive.database;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;

import javax.annotation.Nonnull;
import java.util.List;

public class WarpRepository extends Repository
{
	public WarpRepository(IScheduler scheduler)
	{
		cache = new TimedCache<String, ILocation>(scheduler);
	}

	@Nonnull
	@Override
	public String getTableName()
	{
		return "warpdrive_locations";
	}

	@Nonnull
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

		update.addQueries( // Convert from storing player data as user names to Unique IDs.
			String.format("ALTER TABLE `%s` MODIFY COLUMN creator VARCHAR(36)", getTableName()),
			String.format( // User names -> Unique Ids
				"UPDATE IGNORE `%s` SET `creator` = " +
					"COALESCE((SELECT `uuid` FROM player_db WHERE `name`=`%s`.`creator`), `creator`) " +
					"WHERE length(`creator`) != 36",
				getTableName(), getTableName()
			),
			String.format("ALTER TABLE `%s` MODIFY COLUMN `public` TINYINT(1) NOT NULL", getTableName())
		);

		return update;
	}

	public void Persist(IPlayer creator, String name, boolean publicWarp, ILocation location)
	{
		database.update(
			"INSERT INTO warpdrive_locations (creator, name, `public`, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
				"ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch)",
			creator,
			name,
			publicWarp ? 1:0,
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

	/**
	 * Get warps created by the player or all public warps.
	 * @param owner Warp creator.  Only needs to be valid if publicWarp is false.
	 * @param publicWarp Whether or not the warps are public.
	 * @return All public warp names when public warp is true, otherwise the player's home names.
	 */
	private List<String> GetWarps(IPlayer owner, boolean publicWarp)
	{
		if (publicWarp)
			return database.queryStrings("SELECT name FROM warpdrive_locations WHERE `public`=1");
		else
			return database.queryStrings("SELECT name FROM warpdrive_locations WHERE `public`=0 AND creator=?", owner);
	}

	/**
	 * Gets a warp from the mysql database.
	 * @param owner Warp creator. Only needs to be valid if the warp is private.
	 * @param name Warp name.
	 * @param publicWarp True if the warp is a public warp.
	 * @return Warp location. Null if the location is invalid or isn't stored.
	 */
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
			location = database.queryLocation(
				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=0 AND creator=?",
				name, owner
			);

		return cache.Cache(key, location);
	}

	/**
	 * Checks if a warp is in the database.
	 * @param owner Warp creator. Only needs to be valid if the warp is private.
	 * @param name Warp name.
	 * @param publicWarp True if it's a public warp.
	 * @return True if the warp exists even if it has an invalid location.
	 */
	private boolean doesWarpExist(IPlayer owner, String name, boolean publicWarp)
	{
		// Check if the warp is already stored in the cache.
		if (cache.Cache(cacheKey(owner, name, publicWarp)) != null)
			return true;

		// Check for invalid warp creator if it's a private warp.
		if (!publicWarp && (owner == null || owner.getUniqueId() == null))
			return false;

		String privateWarp = "";
		if (!publicWarp)
			privateWarp = " AND `creator`='" + owner.getUniqueId().toString() + "'";

		return database.queryString(
			"SELECT y FROM `warpdrive_locations` WHERE `name`=? AND `public`=?" + privateWarp,
			name, publicWarp ? 1:0
		) != null;
	}

	/**
	 * Deletes a warp from mysql.
	 * @param owner Warp creator. Only needs to be valid if the warp is private.
	 * @param name Warp name.
	 * @param publicWarp True if the warp is public.
	 * @return True if deleted, false otherwise.
	 */
	private boolean DelWarp(IPlayer owner, String name, boolean publicWarp)
	{
		if (!doesWarpExist(owner, name, publicWarp))
			return false;
		boolean success;
		if (publicWarp)
			success = database.execute("DELETE FROM warpdrive_locations WHERE name=? AND public=1", name);
		else
			success = database.execute("DELETE FROM warpdrive_locations WHERE name=? AND public=0 AND creator=?", name, owner);
		cache.Invalidate(cacheKey(owner, name, publicWarp));
		return success;
	}

	private final TimedCache<String, ILocation> cache;
}
