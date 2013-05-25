package no.runsafe.warpdrive;

import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.timer.ForegroundWorker;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;
import org.bukkit.World;

public class SmartWarpScanner extends ForegroundWorker<String, RunsafeLocation>
{
	public SmartWarpScanner(IScheduler scheduler, IOutput console, SmartWarpChunkRepository chunkRepository, Engine engine)
	{
		super(scheduler);
		this.console = console;
		this.chunkRepository = chunkRepository;
		this.engine = engine;
	}

	@Override
	public void process(String world, RunsafeLocation location)
	{
		location = engine.findTop(location);
		if (engine.targetFloorIsSafe(location, true))
			chunkRepository.saveTarget(location, true, false);
		if (location.getWorld().getRaw().getEnvironment() != World.Environment.NETHER)
		{
			while (location.getBlockY() > 10)
			{
				location.decrementY(1);
				if(engine.targetFloorIsSafe(location, true))
					chunkRepository.saveTarget(location, true, true);
			}
		}
	}

	private IOutput console;
	private SmartWarpChunkRepository chunkRepository;
	private Engine engine;
}
