package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.player.RunsafePlayer;
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

	public void setAwaitingPlayer()
	{
		this.transformPortal(SummoningStone.awaitingPortal);
	}

	public void setComplete()
	{
		this.transformPortal(SummoningStone.completePortal);
	}

	public void reset()
	{
		this.transformPortal(SummoningStone.constructedPortal);
	}

	public void teleportPlayer(RunsafePlayer player)
	{
		player.teleport(this.location.getWorld(), this.location.getX() + 0.5, this.location.getY() + 1, this.location.getZ() + 0.5);
	}

	private void transformPortal(int[][] data)
	{
		this.preparePortalForEditing();

		for (int[] bounds : data)
		{
			RunsafeLocation checkLocation = new RunsafeLocation(
					this.location.getWorld(),
					this.location.getX() + bounds[0],
					this.location.getY() + bounds[1],
					this.location.getZ() + bounds[2]
			);
			checkLocation.getBlock().setMaterial(Item.get(bounds[3]));
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
					this.location.getY() + bounds[1],
					this.location.getZ() + bounds[2]
			);

			Chunk chunk = this.location.getWorld().getRaw().getChunkAt(checkLocation.getRaw());
			if (!chunk.isLoaded())
				chunk.load();
		}
	}

	public RunsafeLocation getLocation()
	{
		return this.location;
	}

	public void setTimerID(int timerID)
	{
		this.timerID = timerID;
	}

	public boolean hasTimer()
	{
		return this.timerID != -1;
	}

	public int getTimerID()
	{
		return this.timerID;
	}

	private RunsafeLocation location;
	private int timerID = -1;

	public static boolean isSummoningStone(RunsafeLocation location)
	{
		for (int[] bounds : SummoningStone.constructedPortal)
		{
			RunsafeLocation checkLocation = new RunsafeLocation(
					location.getWorld(),
					location.getX() + bounds[0],
					location.getY() + bounds[1],
					location.getZ() + bounds[2]
			);
			if (checkLocation.getBlock().is(Item.get(bounds[3])))
				return false;
		}
		return true;
	}

	public static int[][] constructedPortal = {
			{0, 0, 0, 133},
			{1, 0, 0, 57},
			{0, 0, 1, 57},
			{-1, 0, 0, 57},
			{0, 0, -1, 57},
			{-1, 0, -1, 42},
			{-1, 0, 1, 42},
			{1, 0, -1, 42},
			{1, 0, 1, 42},
			{0, 2, 0, 152}
	};

	public static int[][] activatedPortal = {
		{0, 0, 0, 119},
		{1, 0, 0, 7},
		{0, 0, 1, 7},
		{-1, 0, 0, 7},
		{0, 0, -1, 7},
		{-1, 0, -1, 7},
		{-1, 0, 1, 7},
		{1, 0, -1, 7},
		{1, 0, 1, 7},
		{0, -1, 0, 7},
		{0, 2, 0, 7}
	};

	public static int[][] awaitingPortal = {
			{0, 0, 0, 49},
			{1, 0, 0, 7},
			{0, 0, 1, 7},
			{-1, 0, 0, 7},
			{0, 0, -1, 7},
			{-1, 0, -1, 7},
			{-1, 0, 1, 7},
			{1, 0, -1, 7},
			{1, 0, 1, 7},
			{0, 2, 0, 0},
			{0, -1, 0, 0}
	};

	public static int[][] completePortal = {
			{0, 0, 0, 49},
			{1, 0, 0, 155},
			{0, 0, 1, 155},
			{-1, 0, 0, 155},
			{0, 0, -1, 155},
			{-1, 0, -1, 155},
			{-1, 0, 1, 155},
			{1, 0, -1, 155},
			{1, 0, 1, 155},
			{0, 2, 0, 0},
			{0, -1, 0, 0}
	};

	public static int[][] removedPortal = {
			{0, 0, 0, 0},
			{1, 0, 0, 0},
			{0, 0, 1, 0},
			{-1, 0, 0, 0},
			{0, 0, -1, 0},
			{-1, 0, -1, 0},
			{-1, 0, 1, 0},
			{1, 0, -1, 0},
			{1, 0, 1, 0},
			{0, 2, 0, 0},
			{0, -1, 0, 0}
	};
}
