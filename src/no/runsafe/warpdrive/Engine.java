package no.runsafe.warpdrive;

import com.google.common.collect.Lists;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.chunk.RunsafeChunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Engine
{
	public boolean safePlayerTeleport(ILocation originalLocation, IPlayer player)
	{
		ILocation target = findSafeSpot(originalLocation);
		if (target != null)
		{
			player.setFallDistance(0.0F);
			player.teleport(target);
			return true;
		}
		return false;
	}

	public ILocation findSafeSpot(ILocation location)
	{
		int maxScan = 100;
		double x = location.getX();
		double z = location.getZ();
		int posX = (int) x;
		int posZ = (int) z;

		while (true)
		{
			location.setX(posX);
			location.setZ(posZ);
			if (targetFloorIsSafe(location, false))
				return location;

			maxScan--;
			if (maxScan < 0)
				return null;

			// Spiral out to find a safe location
			double t = 100 - maxScan;
			posX = (int) (5 * t * Math.cos(t) + x);
			posZ = (int) (5 * t * Math.sin(t) + z);
		}
	}

	public ILocation findRandomSafeSpot(ILocation originalLocation)
	{
		int maxScan = 100;
		double x = originalLocation.getX();
		double z = originalLocation.getZ();
		int posX = (int) x;
		int posZ = (int) z;

		while (true)
		{
			for (ILocation option : findSafePoints(originalLocation.getWorld(), posX, posZ))
			{
				if (targetFloorIsSafe(option, false))
				{
					option.setY(option.getBlockY() + 2);
					return option;
				}
			}
			maxScan--;
			if (maxScan < 0)
				return null;

			// Spiral out to find a safe location
			double t = 100 - maxScan;
			posX = (int) (5 * t * Math.cos(t) + x);
			posZ = (int) (5 * t * Math.sin(t) + z);
		}
	}

	private List<ILocation> findSafePoints(IWorld world, int x, int z)
	{
		ArrayList<ILocation> options = new ArrayList<ILocation>();
		int safe = 0;
		boolean safeFloor = false;
		ILocation floor = null;
		int maxy = world.getMaxHeight();
		if (((World) ObjectUnwrapper.convert(world)).getEnvironment() == World.Environment.NETHER)
			maxy = 125;
		for (int y = 0; y < maxy; ++y)
		{
			IBlock block = world.getBlockAt(x, y, z);
			if (!block.canPassThrough())
			{
				safeFloor = !block.isHazardous();
				floor = block.getLocation();
			}
			if (block.isHazardous() || !block.isAir())
			{
				safe = 0;
				continue;
			}
			if (!safeFloor)
				continue;

			if (block.isAir())
				safe++;
			if (safe == 2)
				options.add(floor);
		}
		// Return top first
		return Lists.reverse(options);
	}

	public ILocation findTop(ILocation location)
	{
		IWorld world = location.getWorld();
		if (((World) ObjectUnwrapper.convert(location.getWorld())).getEnvironment() == World.Environment.NETHER)
		{
			int maxHeight = 125;
			int minHeight = 4;
			int y = maxHeight - 1;
			int air = 0;
			while (y > minHeight)
			{
				if (world.getBlockAt(location).canPassThrough())
					air++;
				else if (air > 1)
					break;
				else
					air = 0;
				y--;
			}
			location.setY(y);
			return location;
		}
		else
			return location.findTop();
	}

	public boolean targetFloorIsSafe(ILocation location, boolean playerLocation)
	{
		RunsafeChunk chunk = location.getChunk();
		if (chunk.isUnloaded())
			chunk.load();
		IBlock floor;
		if (location.getWorld().getBlockAt(location).isAir() || playerLocation)
			floor = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		else
			floor = location.getWorld().getBlockAt(location);
		if (floor.isHazardous() || floor.canPassThrough() || floor.isAbleToFall())
			return false;

		for (int y = playerLocation ? 0 : 1; y < (playerLocation ? 2 : 3); ++y)
		{
			IBlock block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() + y, location.getBlockZ());
			if (!block.isAir())
				return false;
		}
		for (int x = -1; x < 2; ++x)
			for (int z = -1; z < 2; ++z)
			{
				IBlock adjacent = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + 1, location.getBlockZ() + z);
				if (adjacent.isHazardous())
					return false;
			}
		return true;
	}
}
