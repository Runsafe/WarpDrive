package no.runsafe.warpdrive.smartwarp;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.timer.ForegroundWorker;
import no.runsafe.warpdrive.Engine;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;
import no.runsafe.warpdrive.database.SmartWarpRepository;

import java.util.HashMap;

public class SmartWarpScanner extends ForegroundWorker<String, ILocation> implements IPluginEnabled
{
	public SmartWarpScanner(IScheduler scheduler, IConsole console, SmartWarpRepository warpRepository, SmartWarpChunkRepository chunkRepository, Engine engine, IServer server)
	{
		super(scheduler);
		this.console = console;
		this.warpRepository = warpRepository;
		this.chunkRepository = chunkRepository;
		this.engine = engine;
		this.server = server;

		this.setInterval(10);
	}

	public void Setup(IWorld world, String radius, boolean restart)
	{
		if (restart)
		{
			chunkRepository.clear(world);
			progress.put(world.getName(), 0D);
		}
		int r = Integer.valueOf(radius);
		warpRepository.setRange(world.getName(), r);
		range.put(world.getName(), r);
		if (!progress.containsKey(world.getName()))
			progress.put(world.getName(), 0D);
		if (!isQueued(world.getName()))
			ScheduleNext(world.getName());
	}

	@Override
	public void OnPluginEnabled()
	{
		for (String world : warpRepository.getWorlds())
		{
			progress.put(world, warpRepository.getProgress(world));
			range.put(world, warpRepository.getRange(world));
			ScheduleNext(world);
		}
	}

	@Override
	public void process(String world, ILocation location)
	{
		location = engine.findTop(location);
		if (engine.targetFloorIsSafe(location, true))
			chunkRepository.saveTarget(location, true, false);
		if (location.getWorld().isNether())
		{
			location.setY(50);
			int air = 0;
			while (location.getBlockY() > 10)
			{
				location.decrementY(1);
				if (location.getBlock().isAir())
					air++;
				else if (air > 1)
				{
					location.incrementY(1);
					if (engine.targetFloorIsSafe(location, true))
						chunkRepository.saveTarget(location, true, true);
					location.decrementY(2);
				}
				else
					air = 0;
			}
		}
		Double p = progress.get(world) + 1;
		progress.put(world, p);
		warpRepository.setProgress(world, p);
		if (progress.get(world) % 1000 == 0)
		{
			double d = range.get(world) / 16;
			console.logInformation(
				"Scanning location %.0f/%.0f in %s (%.2f%%)",
				p, d * d, world, 100D * p / (d * d)
			);
		}
		ScheduleNext(world);
	}

	private void ScheduleNext(String world)
	{
		int d = range.get(world) / 16;
		double target = d * d;
		if (progress.get(world) >= target)
		{
			console.logInformation("Nothing left to scan in %s, stopping!", world);
			return;
		}
		Push(world, CalculateNextLocation(world));
	}

	private ILocation CalculateNextLocation(String world)
	{
		if (!worlds.containsKey(world))
			worlds.put(world, server.getWorld(world));
		int r = range.get(world) / 16;
		double offset = (range.get(world) / 2) - 0.5;
		double x = 16 * (progress.get(world) % r) - offset;
		double z = 16 * (progress.get(world) / r) - offset;
		return worlds.get(world).getLocation(x, 255.0, z);
	}

	private final HashMap<String, Double> progress = new HashMap<String, Double>();
	private final HashMap<String, Integer> range = new HashMap<String, Integer>();
	private final HashMap<String, IWorld> worlds = new HashMap<String, IWorld>();
	private final IConsole console;
	private final SmartWarpRepository warpRepository;
	private final SmartWarpChunkRepository chunkRepository;
	private final Engine engine;
	private final IServer server;
}