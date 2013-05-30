package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.server.RunsafeLocation;
import org.bukkit.Chunk;

public class SummoningStone
{
	public SummoningStone(RunsafeLocation location)
	{
		this.location = location;
	}

	public void activate()
	{
		this.transformPortal(SummoningStone.activatedPortal);
	}

	public void remove()
	{
		this.transformPortal(SummoningStone.removedPortal);
	}

	public void reset()
	{
		this.transformPortal(SummoningStone.constructedPortal);
	}

	private void transformPortal(int[][] data)
	{
		this.preparePortalForEditing();

		for (int[] bounds : data)
		{
			RunsafeLocation checkLocation = new RunsafeLocation(
					this.location.getWorld(),
					this.location.getX() + bounds[0],
					this.location.getY(),
					this.location.getZ() + bounds[1]
			);
			checkLocation.getBlock().setTypeId(bounds[2]);
		}
	}

	// Load all chunks the portal is in so we can edit it.
	private void preparePortalForEditing()
	{
		for (int[] bounds : SummoningStone.constructedPortal)
		{
			RunsafeLocation checkLocation = new RunsafeLocation(
					this.location.getWorld(),
					this.location.getX() + bounds[0],
					this.location.getY(),
					this.location.getZ() + bounds[1]
			);

			Chunk chunk = this.location.getWorld().getRaw().getChunkAt(checkLocation.getRaw());
			if (!chunk.isLoaded())
				chunk.load();
		}
	}

	private RunsafeLocation location;

	public static boolean isSummoningStone(RunsafeLocation location)
	{
		for (int[] bounds : SummoningStone.constructedPortal)
		{
			RunsafeLocation checkLocation = new RunsafeLocation(
					location.getWorld(),
					location.getX() + bounds[0],
					location.getY(),
					location.getZ() + bounds[1]
			);
			if (checkLocation.getBlock().getTypeId() != bounds[2])
				return false;
		}
		return true;
	}

	public static int[][] constructedPortal = {
			{0, 0, 133},
			{1, 0, 57},
			{0, 1, 57},
			{-1, 0, 57},
			{0, -1, 57},
			{-1, -1, 42},
			{-1, 1, 42},
			{1, -1, 42},
			{1, 1, 42}
	};

	public static int[][] activatedPortal = {
			{0, 0, 119},
			{1, 0, 7},
			{0, 1, 7},
			{-1, 0, 7},
			{0, -1, 7},
			{-1, -1, 7},
			{-1, 1, 7},
			{1, -1, 7},
			{1, 1, 7}
	};

	public static int[][] removedPortal = {
			{0, 0, 0},
			{1, 0, 0},
			{0, 1, 0},
			{-1, 0, 0},
			{0, -1, 0},
			{-1, -1, 0},
			{-1, 1, 0},
			{1, -1, 0},
			{1, 1, 0}
	};
}
