package no.runsafe.warpdrive;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IAsyncEvent;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.player.IPlayerRightClickSign;
import no.runsafe.framework.output.ChatColour;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeSign;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.ForegroundWorker;
import no.runsafe.framework.timer.IScheduler;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SnazzyWarp extends ForegroundWorker<String, SnazzyWarp.WarpParameters> implements
	IPlayerRightClickSign, IAsyncEvent, IConfigurationChanged
{
	public SnazzyWarp(IScheduler scheduler, Engine engine, IOutput output)
	{
		super(scheduler, 2);
		this.engine = engine;
		console = output;
	}

	@Override
	public boolean OnPlayerRightClickSign(RunsafePlayer thePlayer, RunsafeItemStack runsafeItemStack, RunsafeSign theSign)
	{
		if (theSign.getLine(0).equalsIgnoreCase(signHeader))
		{
			if (!snazzyWarps.containsKey(theSign.getLine(1))
				|| snazzyWarps.get(theSign.getLine(1)).refresh(theSign))
				snazzyWarps.put(theSign.getLine(1), new WarpParameters(theSign));
			Push(thePlayer.getName(), snazzyWarps.get(theSign.getLine(1)));
			console.outputDebugToConsole("Pushed player click..", Level.FINE);
			return false;
		}
		return true;
	}

	@Override
	public void process(String player, WarpParameters parameters)
	{
		if (parameters == null)
			return;
		console.outputDebugToConsole(String.format("Player %s teleporting", player), Level.FINE);
		RunsafePlayer target = RunsafeServer.Instance.getPlayer(player);
		RunsafeLocation destination = parameters.getTarget();
		if (target.isOnline() && destination != null)
			target.teleport(destination);
		else
			console.outputDebugToConsole("Unable to find destination..", Level.FINE);
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		change_after = Duration.standardSeconds(configuration.getConfigValueAsInt("snazzy.timeout"));
	}

	private final Engine engine;

	class WarpParameters
	{
		WarpParameters(RunsafeSign sign)
		{
			maxDistance = Integer.parseInt(sign.getLine(2));
			minDistance = Integer.parseInt(sign.getLine(3));
			world = sign.getWorld();
			originX = sign.getX();
			originZ = sign.getZ();
			console.outputDebugToConsole(
				String.format(
					"Configured new warp sign %s in world %s [%d-%d] at (%d,%d).",
					sign.getLine(1),
					world.getName(),
					minDistance,
					maxDistance,
					originX,
					originZ
				),
				Level.FINE
			);
		}

		public RunsafeLocation getTarget()
		{
			if (target == null || expires.isBefore(DateTime.now()) || !engine.targetFloorIsSafe(target, true))
			{
				target = getNewTarget();
				expires = DateTime.now().plus(change_after);
			}
			return target;
		}

		private RunsafeLocation getNewTarget()
		{
			boolean negX = rng.nextInt(100) > 50;
			boolean negZ = rng.nextInt(100) > 50;
			double randomX = (negX ? -1 : 1) * (rng.nextInt(maxDistance - minDistance) + minDistance);
			double randomZ = (negZ ? -1 : 1) * (rng.nextInt(maxDistance - minDistance) + minDistance);
			RunsafeLocation target = null;
			int retries = 10;
			while (target == null && retries-- > 0)
				target = engine.findSafeSpot(new RunsafeLocation(world, randomX + originX + 0.5, 64.0D, randomZ + originZ + 0.5), true);
			return target;
		}

		private final RunsafeWorld world;
		private final int originX;
		private final int originZ;
		private final int maxDistance;
		private final int minDistance;
		private final Random rng = new Random();
		private DateTime expires = DateTime.now();
		private RunsafeLocation target;

		public boolean refresh(RunsafeSign sign)
		{
			return maxDistance != Integer.parseInt(sign.getLine(2)) || minDistance != Integer.parseInt(sign.getLine(3));
		}
	}

	private final ConcurrentHashMap<String, WarpParameters> snazzyWarps = new ConcurrentHashMap<String, WarpParameters>();
	private final IOutput console;
	private Duration change_after;
	public static final String signHeader = ChatColour.DARK_BLUE.toBukkit() + "[Snazzy Warp]";
	public static final String signTag = "[snazzy warp]";
}