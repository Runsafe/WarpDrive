package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IPlayerProvider;
import no.runsafe.warpdrive.WarpDrive;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummoningEngine implements IConfigurationChanged
{
	public SummoningEngine(SummoningStoneRepository summoningStoneRepository, IScheduler scheduler, IPlayerProvider playerProvider)
	{
		this.summoningStoneRepository = summoningStoneRepository;
		this.scheduler = scheduler;
		this.playerProvider = playerProvider;
	}

	public void registerPendingSummon(String playerName, int stoneID)
	{
		SummoningStone stone = stones.get(stoneID);
		stone.setAwaitingPlayer();
		IPlayer player = playerProvider.getPlayerExact(playerName);
		if (player == null)
			return;

		pendingSummons.put(player, stoneID);

		if (player.isOnline())
			player.sendColouredMessage("&3You have a pending summon, head to the ritual stone to accept.");
	}

	public boolean playerHasPendingSummon(IPlayer player)
	{
		return pendingSummons.containsKey(player);
	}

	public void acceptPlayerSummon(IPlayer player)
	{
		int stoneID = pendingSummons.get(player);
		WarpDrive.debug.debugFine("Player %s is accepting portal %s.", player.getName(), stoneID);
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
		pendingSummons.remove(player);
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
		return scheduler.startSyncTask(() -> summoningStoneExpire(stoneID), stoneExpireTime);
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

			for (Map.Entry<IPlayer, Integer> pendingSummon : pendingSummons.entrySet())
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
	private final HashMap<Integer, SummoningStone> stones = new HashMap<>();
	private final ConcurrentHashMap<IPlayer, Integer> pendingSummons = new ConcurrentHashMap<>();
	private List<String> ritualWorlds = new ArrayList<>();
	private List<String> stoneWorlds = new ArrayList<>();
	private final SummoningStoneRepository summoningStoneRepository;
	private final IScheduler scheduler;
	private final IPlayerProvider playerProvider;
}
