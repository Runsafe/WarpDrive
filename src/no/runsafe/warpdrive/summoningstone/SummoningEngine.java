package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummoningEngine implements IConfigurationChanged
{
	public SummoningEngine(SummoningStoneRepository summoningStoneRepository, IScheduler scheduler)
	{
		this.summoningStoneRepository = summoningStoneRepository;
		this.scheduler = scheduler;
	}

	public void registerPendingSummon(String playerName, int stoneID)
	{
		SummoningStone stone = this.stones.get(stoneID);
		stone.setAwaitingPlayer();
		this.pendingSummons.put(playerName, stoneID);

		RunsafePlayer player = RunsafeServer.Instance.getPlayerExact(playerName);
		if (player != null)
			if (player.isOnline())
				player.sendColouredMessage("&3You have a pending summon, head to the ritual stone to accept.");
	}

	public boolean playerHasPendingSummon(RunsafePlayer player)
	{
		return this.pendingSummons.containsKey(player.getName());
	}

	public void acceptPlayerSummon(RunsafePlayer player)
	{
		String playerName = player.getName();
		int stoneID = this.pendingSummons.get(playerName);
		SummoningStone stone = this.stones.get(stoneID);

		stone.setComplete();
		stone.teleportPlayer(player);

		if (stone.hasTimer())
			this.scheduler.cancelTask(stone.getTimerID());

		this.summoningStoneRepository.deleteSummoningStone(stoneID);
		this.stones.remove(stoneID);
		this.pendingSummons.remove(playerName);
	}

	public int getStoneAtLocation(RunsafeLocation location)
	{
		for (Map.Entry<Integer, SummoningStone> stone : this.stones.entrySet())
		{
			RunsafeLocation stoneLocation = stone.getValue().getLocation();
			if (stoneLocation.getWorld().getName().equals(location.getWorld().getName()))
				if (stoneLocation.distance(location) == 0)
					return stone.getKey();
		}
		return -1;
	}

	public int registerExpireTimer(final int stoneID)
	{
		return this.scheduler.startSyncTask(new Runnable() {
			@Override
			public void run() {
				summoningStoneExpire(stoneID);
			}
		}, this.stoneExpireTime);
	}

	public void registerStone(int stoneID, SummoningStone stone)
	{
		this.stones.put(stoneID, stone);
	}

	public void summoningStoneExpire(int stoneID)
	{
		// If this is false, we actually had a fault somewhere, but we check anyway.
		if (this.stones.containsKey(stoneID))
		{
			// The stone expired, remove it from everything.
			SummoningStone stone = this.stones.get(stoneID);
			stone.remove();
			this.summoningStoneRepository.deleteSummoningStone(stoneID);
			this.stones.remove(stoneID);
		}
	}

	public HashMap<Integer, SummoningStone> getLoadedStones()
	{
		return this.stones;
	}

	public boolean isRitualWorld(RunsafeWorld world)
	{
		return this.ritualWorlds.contains(world.getName());
	}

	public boolean canCreateStone(RunsafeWorld world)
	{
		return this.stoneWorlds.contains(world.getName());
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		this.stoneExpireTime = config.getConfigValueAsInt("summoningStone.expire") * 60;
		this.ritualWorlds = config.getConfigValueAsList("summoningStone.ritualWorlds");
		this.stoneWorlds = config.getConfigValueAsList("summoningStone.stoneWorlds");
	}

	private int stoneExpireTime = 600;
	private HashMap<Integer, SummoningStone> stones = new HashMap<Integer, SummoningStone>();
	private HashMap<String, Integer> pendingSummons = new HashMap<String, Integer>();
	private List<String> ritualWorlds = new ArrayList<String>();
	private List<String> stoneWorlds = new ArrayList<String>();
	private SummoningStoneRepository summoningStoneRepository;
	private IScheduler scheduler;
}
