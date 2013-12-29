package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.WarpDrive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummoningEngine implements IConfigurationChanged
{
	public SummoningEngine(SummoningStoneRepository summoningStoneRepository, IScheduler scheduler, IServer server)
	{
		this.summoningStoneRepository = summoningStoneRepository;
		this.scheduler = scheduler;
		this.server = server;
	}

	public void registerPendingSummon(String playerName, int stoneID)
	{
		SummoningStone stone = stones.get(stoneID);
		stone.setAwaitingPlayer();
		pendingSummons.put(playerName, stoneID);

		IPlayer player = server.getPlayerExact(playerName);
		if (player != null && player.isOnline())
			player.sendColouredMessage("&3You have a pending summon, head to the ritual stone to accept.");
	}

	public boolean playerHasPendingSummon(IPlayer player)
	{
		return pendingSummons.containsKey(player.getName());
	}

	public void acceptPlayerSummon(IPlayer player)
	{
		String playerName = player.getName();
		int stoneID = pendingSummons.get(playerName);
		WarpDrive.debug.debugFine("Player %s is accepting portal %s.", playerName, stoneID);
		SummoningStone stone = stones.get(stoneID);

		stone.setComplete();
		stone.teleportPlayer(player);

		if (stone.hasTimer())
		{
			WarpDrive.debug.debugFine("Stone %s has a timer, cancelling it.", stoneID);
			scheduler.cancelTask(stone.getTimerID());
		}

		summoningStoneRepository.deleteSummoningStone(stoneID);
		stones.remove(stoneID);
		pendingSummons.remove(playerName);
	}

	public int getStoneAtLocation(ILocation location)
	{
		for (Map.Entry<Integer, SummoningStone> stone : stones.entrySet())
		{
			ILocation stoneLocation = stone.getValue().getLocation();
			if (stoneLocation.getWorld().equals(location.getWorld()) && stoneLocation.distance(location) < 2)
				return stone.getKey();
		}
		return -1;
	}

	public int registerExpireTimer(final int stoneID)
	{
		return scheduler.startSyncTask(new Runnable()
		{
			@Override
			public void run()
			{
				summoningStoneExpire(stoneID);
			}
		}, stoneExpireTime);
	}

	public void registerStone(int stoneID, SummoningStone stone)
	{
		stones.put(stoneID, stone);
	}

	public void summoningStoneExpire(int stoneID)
	{
		// If this is false, we actually had a fault somewhere, but we check anyway.
		if (stones.containsKey(stoneID))
		{
			// The stone expired, remove it from everything.
			SummoningStone stone = stones.get(stoneID);
			stone.remove();
			summoningStoneRepository.deleteSummoningStone(stoneID);
			stones.remove(stoneID);

			for (Map.Entry<String, Integer> pendingSummon : pendingSummons.entrySet())
				if (pendingSummon.getValue() == stoneID)
					pendingSummons.remove(pendingSummon.getKey());
		}
	}

	public HashMap<Integer, SummoningStone> getLoadedStones()
	{
		return stones;
	}

	public boolean isRitualWorld(IWorld world)
	{
		return ritualWorlds.contains(world.getName());
	}

	public boolean canCreateStone(IWorld world)
	{
		return stoneWorlds.contains(world.getName());
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		stoneExpireTime = config.getConfigValueAsInt("summoningStone.expire") * 60;
		ritualWorlds = config.getConfigValueAsList("summoningStone.ritualWorlds");
		stoneWorlds = config.getConfigValueAsList("summoningStone.stoneWorlds");
	}

	private int stoneExpireTime = 600;
	private final HashMap<Integer, SummoningStone> stones = new HashMap<Integer, SummoningStone>();
	private final HashMap<String, Integer> pendingSummons = new HashMap<String, Integer>();
	private List<String> ritualWorlds = new ArrayList<String>();
	private List<String> stoneWorlds = new ArrayList<String>();
	private final SummoningStoneRepository summoningStoneRepository;
	private final IScheduler scheduler;
	private final IServer server;
}
