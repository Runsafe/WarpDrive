package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.event.IPluginDisabled;
import no.runsafe.framework.event.IPluginEnabled;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;

import java.util.Map;

public class SummoningStoneCleaner implements IPluginEnabled, IPluginDisabled
{
	public SummoningStoneCleaner(SummoningEngine summoningEngine, SummoningStoneRepository summoningStoneRepository, IOutput output)
	{
		this.summoningEngine = summoningEngine;
		this.summoningStoneRepository = summoningStoneRepository;
		this.output = output;
	}

	@Override
	public void OnPluginDisabled()
	{
		// Server shutting down, clean up any stones in the world.
		for (Map.Entry<Integer, SummoningStone> node : this.summoningEngine.getLoadedStones().entrySet())
		{
			SummoningStone stone = node.getValue();
			stone.remove();
			this.summoningStoneRepository.deleteSummoningStone(node.getKey());
			this.output.write("Removing summoning portal: " + stone.getLocation().toString());
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
			this.output.write("Reset leftover portal: " + stoneLocation.toString());
		}

		// Wipe any stones we had remembered
		this.summoningStoneRepository.wipeStoneList();
	}

	private SummoningEngine summoningEngine;
	private SummoningStoneRepository summoningStoneRepository;
	private IOutput output;
}
