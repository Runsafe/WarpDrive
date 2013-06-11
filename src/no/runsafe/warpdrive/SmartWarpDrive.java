package no.runsafe.warpdrive;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.framework.minecraft.player.RunsafePlayer;
import no.runsafe.framework.timer.ForegroundWorker;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;

public class SmartWarpDrive extends ForegroundWorker<String, RunsafeLocation>
{
	public SmartWarpDrive(IScheduler scheduler, SmartWarpChunkRepository smartWarpChunks, Engine engine)
	{
		super(scheduler);
		this.smartWarpChunks = smartWarpChunks;
		this.engine = engine;
		setInterval(10);
	}

	public void Engage(RunsafePlayer player, RunsafeWorld target, boolean cave)
	{
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
		RunsafePlayer player = RunsafeServer.Instance.getPlayerExact(playerName);
		if (player == null)
			return;
		target.incrementX(0.5);
		target.incrementZ(0.5);
		player.teleport(target);
	}

	private final SmartWarpChunkRepository smartWarpChunks;
	private final Engine engine;
}
