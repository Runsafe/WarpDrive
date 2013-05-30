package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.server.RunsafeLocation;

public class SummoningStone
{
	public static boolean isSummoningStone(RunsafeLocation location)
	{
		for (int i = 0; i < SummoningStone.checkBounds.length; i++)
		{
			int[] bounds = SummoningStone.checkBounds[i];
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

	public static int[][] checkBounds = {
			{0, 0, 133},
			{1, 0, 57},
			{0, 1, 57},
			{-1, 0, 57},
			{0, -1, 57},
			{-1, -1, 42},
			{-1, 1, 42},
			{1, -1, 42},
			{1, 1, 42}};
}
