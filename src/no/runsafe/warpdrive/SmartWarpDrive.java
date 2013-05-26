package no.runsafe.warpdrive;

import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.ForegroundWorker;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;

public class SmartWarpDrive extends ForegroundWorker<String, RunsafeLocation>
{
	public SmartWarpDrive(IScheduler scheduler, IOutput console, SmartWarpChunkRepository smartWarpChunks, Engine engine)
	{
		super(scheduler);
		this.console = console;
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
		player.teleport(target);
	}

	private final IOutput console;
	private final SmartWarpChunkRepository smartWarpChunks;
	private final Engine engine;
}
