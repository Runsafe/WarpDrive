package no.runsafe.warpdrive;

import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

public class StaticWarp
{
	public static boolean safePlayerTeleport(RunsafeLocation originalLocation, RunsafePlayer player, boolean forceTop)
	{
		RunsafeLocation target = findSafeSpot(originalLocation, forceTop);
		if (target != null)
		{
			player.setFallDistance(0.0F);
			player.teleport(target);
			return true;
		}
		return false;
	}

	public static RunsafeLocation findSafeSpot(RunsafeLocation originalLocation, boolean forceTop)
	{
		int maxScan = 100;
		double x = originalLocation.getX();
		double z = originalLocation.getZ();
		RunsafeLocation location = new RunsafeLocation(
			originalLocation.getWorld(),
			originalLocation.getX(),
			originalLocation.getY() - 1,
			originalLocation.getZ(),
			originalLocation.getYaw(),
			originalLocation.getPitch()
		);
		if (forceTop || !targetFloorIsSafe(location, false))
			location = findTop(location);

		while (true)
		{
			if (targetFloorIsSafe(location, false))
			{
				location.setY(location.getY() + 1);
				return location;
			}
			maxScan--;
			if (maxScan < 0)
				return null;

			// Spiral out to find a safe location
			double t = 100 - maxScan;
			location.setX(5 * t * Math.cos(t) + x);
			location.setZ(5 * t * Math.sin(t) + z);
			location = findTop(location);
		}
	}

	public static RunsafeLocation findTop(RunsafeLocation location)
	{
		RunsafeWorld world = location.getWorld();
		if (location.getWorld().getRaw().getEnvironment() == World.Environment.NETHER)
		{
			int maxHeight = 125;
			int minHeight = 4;
			int y = maxHeight - 1;
			int air = 0;
			while (y > minHeight)
			{
				location.setY(y);
				if (world.getBlockAt(location).isAir())
					air++;
				else
					air = 0;
				if (air > 1)
					break;
				y--;
			}
			return location;
		}
		else
			return new RunsafeLocation(world.getRaw().getHighestBlockAt(location.getRaw()).getLocation());
	}

	public static boolean targetFloorIsSafe(RunsafeLocation location, boolean playerLocation)
	{
		Chunk chunk = location.getWorld().getRaw().getChunkAt(location.getRaw());
		if (!chunk.isLoaded())
			chunk.load();
		RunsafeBlock floor;
		if (playerLocation)
			floor = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		else
			floor = location.getWorld().getBlockAt(location);
		if (floor.isHazardous() || (floor.canPassThrough() && floor.getTypeId() != Material.WATER.getId()))
			return false;

		for (int y = playerLocation ? 0 : 1; y < (playerLocation ? 2 : 3); ++y)
		{
			RunsafeBlock block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() + y, location.getBlockZ());
			if (block.isHazardous() || !block.canPassThrough())
				return false;
		}
		for (int x = -1; x < 2; ++x)
			for (int z = -1; z < 2; ++z)
			{
				RunsafeBlock adjacent = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + 1, location.getBlockZ() + z);
				if (adjacent.isHazardous())
					return false;
			}
		return true;
	}
}
