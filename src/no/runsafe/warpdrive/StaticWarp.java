package no.runsafe.warpdrive;

import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;
import org.bukkit.World;

public class StaticWarp
{
	public static boolean safePlayerTeleport(RunsafeLocation location, RunsafePlayer player)
	{
		int maxScan = 100;
		double x = location.getX();
		double z = location.getZ();
		while (true)
		{
			location = findTop(location);
			if (targetIsSafe(location))
			{
				player.setFallDistance(0.0F);
				player.teleport(location);
				return true;
			}
			maxScan--;
			if (maxScan < 0)
				return false;

			double t = 100 - maxScan;
			location.setX(5 * t * Math.cos(t) + x);
			location.setZ(5 * t * Math.sin(t) + z);
		}
	}

	public static RunsafeLocation findTop(RunsafeLocation location)
	{
		RunsafeWorld world = location.getWorld();
		int maxHeight = world.getMaxHeight();
		int minHeight = 1;

		if (location.getWorld().getRaw().getEnvironment() == World.Environment.NETHER)
		{
			maxHeight = 126;
			minHeight = 0;
		}

		int y = maxHeight;
		while (y > minHeight)
		{
			location.setY(y--);
			if (!world.getBlockAt(location).isAir())
				break;
		}
		return location;
	}

	public static boolean targetIsSafe(RunsafeLocation location)
	{
		RunsafeBlock floor = location.getWorld().getBlockAt(location);
		if (floor.canPassThrough() && floor.getTypeId() != Material.WATER.getId())
			return false;

		for (int y = 1; y < 3; ++y)
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
