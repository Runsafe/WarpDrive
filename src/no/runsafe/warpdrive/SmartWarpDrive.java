package no.runsafe.warpdrive;

import no.runsafe.framework.api.*;
import no.runsafe.framework.api.event.player.IPlayerCommandPreprocessEvent;
import no.runsafe.framework.api.event.player.IPlayerDamageEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerCommandPreprocessEvent;
import no.runsafe.framework.timer.ForegroundWorker;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;

import java.util.ArrayList;
import java.util.List;

public class SmartWarpDrive extends ForegroundWorker<String, ILocation> implements IPlayerDamageEvent, IPlayerCommandPreprocessEvent, IConfigurationChanged
{
	public SmartWarpDrive(IScheduler scheduler, SmartWarpChunkRepository smartWarpChunks, Engine engine, IServer server)
	{
		super(scheduler);
		this.scheduler = scheduler;
		this.smartWarpChunks = smartWarpChunks;
		this.engine = engine;
		this.server = server;
		setInterval(10);
	}

	public void Engage(IPlayer player, IWorld target, boolean cave, boolean lock)
	{
		if (lockedLocation != null)
		{
			process(player.getName(), lockedLocation);
			return;
		}
		if (lock)
			shouldLock = true; // Lock the next location we produce.

		ILocation candidate;
		while (true)
		{
			candidate = smartWarpChunks.getTarget(target, cave);
			if (candidate == null)
				return;
			if (engine.targetFloorIsSafe(candidate, true))
				break;
			smartWarpChunks.setUnsafe(candidate);
		}
		Push(player.getName(), candidate);
		if (!cave && skyFall)
			fallen.add(player.getName());
	}

	@Override
	public void process(String playerName, ILocation target)
	{
		IPlayer player = server.getPlayerExact(playerName);
		if (player == null)
			return;
		target.incrementX(0.5);
		target.incrementZ(0.5);
		if (skyFall && fallen.contains(playerName))
		{
			target.setY(300);
			target.setPitch(-90);
		}
		if(!player.teleport(target))
			fallen.remove(playerName);

		if (shouldLock)
		{
			shouldLock = false;
			lockedLocation = target;
			scheduler.startSyncTask(new Runnable()
			{
				@Override
				public void run()
				{
					unlock();
				}
			}, 20);
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		skyFall = configuration.getConfigValueAsBoolean("smart.skyfall");
	}

	@Override
	public void OnPlayerDamage(IPlayer player, RunsafeEntityDamageEvent event)
	{
		if (event.getCause() == RunsafeEntityDamageEvent.RunsafeDamageCause.FALL)
		{
			if (fallen.contains(player.getName()))
			{
				event.cancel();
				fallen.remove(player.getName());
			}
		}
	}

	@Override
	public void OnBeforePlayerCommand(RunsafePlayerCommandPreprocessEvent event)
	{
		if (fallen.contains(event.getPlayer().getName()))
			event.cancel();
	}

	private void unlock()
	{
		lockedLocation = null;
	}

	private boolean skyFall = false;
	private boolean shouldLock = false;
	private ILocation lockedLocation;
	private final List<String> fallen = new ArrayList<String>(0);
	private final IScheduler scheduler;
	private final SmartWarpChunkRepository smartWarpChunks;
	private final Engine engine;
	private final IServer server;
}
