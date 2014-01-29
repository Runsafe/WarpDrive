package no.runsafe.warpdrive.smartwarp;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.warpdrive.Engine;
import no.runsafe.warpdrive.database.SmartWarpChunkRepository;

public class ChunkScanner implements Runnable
{
	public ChunkScanner(IScheduler scheduler, SmartWarpChunkRepository smartWarpChunks, IWorld target, boolean cave, Engine engine)
	{
		this.scheduler = scheduler;
		this.smartWarpChunks = smartWarpChunks;
		this.target = target;
		this.cave = cave;
		this.engine = engine;
	}

	public ILocation find()
	{
		while (!finished)
			scheduler.runNow(this);

		return destination;
	}

	@Override
	public void run()
	{
		ILocation candidate = smartWarpChunks.getTarget(target, cave);
		if (candidate == null)
			finished = true;
		else if (engine.targetFloorIsSafe(candidate, true))
		{
			candidate.incrementX(0.5);
			candidate.incrementZ(0.5);
			destination = candidate;
			finished = true;
		}
		else
			smartWarpChunks.setUnsafe(candidate);
	}

	private final IScheduler scheduler;
	private final SmartWarpChunkRepository smartWarpChunks;
	private final IWorld target;
	private final boolean cave;
	private final Engine engine;
	private ILocation destination = null;
	private boolean finished = false;
}
