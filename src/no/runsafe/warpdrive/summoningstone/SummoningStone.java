package no.runsafe.warpdrive.summoningstone;

import com.google.common.collect.Lists;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.chunk.RunsafeChunk;

import java.util.List;

public class SummoningStone
{
	public SummoningStone(ILocation location)
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

	public void teleportPlayer(IPlayer player)
	{
		player.teleport(this.location.getWorld(), this.location.getX() + 0.5, this.location.getY() + 1, this.location.getZ() + 0.5);
	}

	private void transformPortal(int[][] data)
	{
		this.preparePortalForEditing();

		for (int[] bounds : data)
		{
			ILocation checkLocation = new RunsafeLocation(
				this.location.getWorld(),
				this.location.getX() + bounds[0],
				this.location.getY() + bounds[1],
				this.location.getZ() + bounds[2]
			);
			checkLocation.getBlock().setMaterial(palette.get(bounds[3]));
		}
	}

	// Load all chunks the portal is in so we can edit it.
	private void preparePortalForEditing()
	{
		loadChunkAt(-1, -1);
		loadChunkAt(-1, 1);
		loadChunkAt(1, -1);
		loadChunkAt(1, 1);
	}

	private void loadChunkAt(int xOffset, int zOffset)
	{
		ILocation checkLocation = new RunsafeLocation(
			this.location.getWorld(),
			this.location.getX() + xOffset,
			this.location.getY(),
			this.location.getZ() + zOffset
		);

		RunsafeChunk chunk = checkLocation.getChunk();
		if (chunk.isUnloaded())
			chunk.load();
	}

	public ILocation getLocation()
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

	private final ILocation location;
	private int timerID = -1;

	public static boolean isSummoningStone(ILocation location)
	{
		for (int[] bounds : SummoningStone.constructedPortal)
		{
			ILocation checkLocation = new RunsafeLocation(
				location.getWorld(),
				location.getX() + bounds[0],
				location.getY() + bounds[1],
				location.getZ() + bounds[2]
			);

			if (!checkLocation.getBlock().is(palette.get(bounds[3])))
				return false;
		}
		return true;
	}

	private static final List<Item> palette = Lists.newArrayList(
		Item.Unavailable.Air,
		Item.BuildingBlock.Bedrock,
		Item.BuildingBlock.IronBlock,
		Item.BuildingBlock.Obsidian,
		Item.BuildingBlock.Diamond,
		Item.Unavailable.EnderPortal,
		Item.BuildingBlock.Emerald,
		Item.Redstone.Block,
		Item.BuildingBlock.Quartz.Normal
	);
	private static final int AIR = palette.indexOf(Item.Unavailable.Air);
	private static final int BEDROCK = palette.indexOf(Item.BuildingBlock.Bedrock);
	private static final int IRON = palette.indexOf(Item.BuildingBlock.IronBlock);
	private static final int OBSIDIAN = palette.indexOf(Item.BuildingBlock.Obsidian);
	private static final int DIAMOND = palette.indexOf(Item.BuildingBlock.Diamond);
	private static final int PORTAL = palette.indexOf(Item.Unavailable.EnderPortal);
	private static final int EMERALD = palette.indexOf(Item.BuildingBlock.Emerald);
	private static final int REDSTONE = palette.indexOf(Item.Redstone.Block);
	private static final int QUARTZ = palette.indexOf(Item.BuildingBlock.Quartz.Normal);
	public static final int[][] constructedPortal = {
		{0, 0, 0, EMERALD},
		{1, 0, 0, DIAMOND},
		{0, 0, 1, DIAMOND},
		{-1, 0, 0, DIAMOND},
		{0, 0, -1, DIAMOND},
		{-1, 0, -1, IRON},
		{-1, 0, 1, IRON},
		{1, 0, -1, IRON},
		{1, 0, 1, IRON},
		{0, 2, 0, REDSTONE}
	};

	public static final int[][] activatedPortal = {
		{0, 0, 0, PORTAL},
		{1, 0, 0, BEDROCK},
		{0, 0, 1, BEDROCK},
		{-1, 0, 0, BEDROCK},
		{0, 0, -1, BEDROCK},
		{-1, 0, -1, BEDROCK},
		{-1, 0, 1, BEDROCK},
		{1, 0, -1, BEDROCK},
		{1, 0, 1, BEDROCK},
		{0, -1, 0, BEDROCK},
		{0, 2, 0, BEDROCK}
	};

	public static final int[][] awaitingPortal = {
		{0, 0, 0, OBSIDIAN},
		{1, 0, 0, BEDROCK},
		{0, 0, 1, BEDROCK},
		{-1, 0, 0, BEDROCK},
		{0, 0, -1, BEDROCK},
		{-1, 0, -1, BEDROCK},
		{-1, 0, 1, BEDROCK},
		{1, 0, -1, BEDROCK},
		{1, 0, 1, BEDROCK},
		{0, 2, 0, AIR},
		{0, -1, 0, AIR}
	};

	public static final int[][] completePortal = {
		{0, 0, 0, OBSIDIAN},
		{1, 0, 0, QUARTZ},
		{0, 0, 1, QUARTZ},
		{-1, 0, 0, QUARTZ},
		{0, 0, -1, QUARTZ},
		{-1, 0, -1, QUARTZ},
		{-1, 0, 1, QUARTZ},
		{1, 0, -1, QUARTZ},
		{1, 0, 1, QUARTZ},
		{0, 2, 0, AIR},
		{0, -1, 0, AIR}
	};

	public static final int[][] removedPortal = {
		{0, 0, 0, AIR},
		{1, 0, 0, AIR},
		{0, 0, 1, AIR},
		{-1, 0, 0, AIR},
		{0, 0, -1, AIR},
		{-1, 0, -1, AIR},
		{-1, 0, 1, AIR},
		{1, 0, -1, AIR},
		{1, 0, 1, AIR},
		{0, 2, 0, AIR},
		{0, -1, 0, AIR}
	};
}
