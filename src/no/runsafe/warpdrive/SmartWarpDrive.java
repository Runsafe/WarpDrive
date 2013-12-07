package no.runsafe.warpdrive;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.timer.ForegroundWorker;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;

public class SmartWarpDrive extends ForegroundWorker<String, RunsafeLocation>
{
	public SmartWarpDrive(IScheduler scheduler, SmartWarpChunkRepository smartWarpChunks, Engine engine)
	{
		super(scheduler);
		this.scheduler = scheduler;
		this.smartWarpChunks = smartWarpChunks;
		this.engine = engine;
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

		RunsafeLocation candidate;
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
	}

	@Override
	public void process(String playerName, RunsafeLocation target)
	{
		IPlayer player = RunsafeServer.Instance.getPlayerExact(playerName);
		if (player == null)
			return;
		target.incrementX(0.5);
		target.incrementZ(0.5);
		player.teleport(target);

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

	private void unlock()
	{
		lockedLocation = null;
	}

	private boolean shouldLock = false;
	private RunsafeLocation lockedLocation;
	private final IScheduler scheduler;
	private final SmartWarpChunkRepository smartWarpChunks;
	private final Engine engine;
}
