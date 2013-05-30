package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.event.IPluginDisabled;
import no.runsafe.framework.event.IPluginEnabled;
import no.runsafe.framework.server.RunsafeLocation;

import java.util.Map;

public class SummoningStoneCleaner implements IPluginEnabled, IPluginDisabled
{
	public SummoningStoneCleaner(SummoningEngine summoningEngine, SummoningStoneRepository summoningStoneRepository)
	{
		this.summoningEngine = summoningEngine;
		this.summoningStoneRepository = summoningStoneRepository;
	}

	@Override
	public void OnPluginDisabled()
	{
		// Server shutting down, clean up any stones in the world.
		for (Map.Entry<Integer, SummoningStone> node : this.summoningEngine.getLoadedStones().entrySet())
		{
			node.getValue().remove();
			this.summoningStoneRepository.deleteSummoningStone(node.getKey());
		}
	}

	@Override
	public void OnPluginEnabled()
	{
		// If we have any stones stored here, the server crashed, reset them all.
		for (RunsafeLocation stoneLocation : this.summoningStoneRepository.getStoneList())
		{
			SummoningStone stone = new SummoningStone(stoneLocation);
			stone.reset();
		}

		// Wipe any stones we had remembered
		this.summoningStoneRepository.wipeStoneList();
	}

	private SummoningEngine summoningEngine;
	private SummoningStoneRepository summoningStoneRepository;
}
