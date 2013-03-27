package no.runsafe.warpdrive;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IAsyncEvent;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.block.ISignChange;
import no.runsafe.framework.event.player.IPlayerRightClickSign;
import no.runsafe.framework.output.ChatColour;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
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

import static no.runsafe.warpdrive.StaticWarp.findSafeSpot;
import static no.runsafe.warpdrive.StaticWarp.targetFloorIsSafe;

public class SnazzyWarp extends ForegroundWorker<String, SnazzyWarp.WarpParameters> implements
	IPlayerRightClickSign, IAsyncEvent, IConfigurationChanged, ISignChange
{
	public SnazzyWarp(IScheduler scheduler, IOutput output)
	{
		super(scheduler, 2);
		console = output;
	}

	@Override
	public boolean OnPlayerRightClickSign(RunsafePlayer thePlayer, RunsafeItemStack runsafeItemStack, RunsafeSign theSign)
	{
		if (theSign.getLine(0).equalsIgnoreCase(signHeader))
		{
			if (!snazzyWarps.containsKey(theSign.getLine(1)))
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

	@Override
	public boolean OnSignChange(RunsafePlayer player, RunsafeBlock block, String[] strings)
	{
		if (!strings[0].toLowerCase().contains("[snazzy warp]") && !strings[0].toLowerCase().contains(signHeader))
			return true;
		if (player.hasPermission("runsafe.snazzysign.create"))
		{
			console.writeColoured("%s created a snazzy warp sign for named %s.", player.getPrettyName(), strings[1]);
			strings[0] = signHeader;
			return true;
		}
		return false;	}

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
			if (target == null || expires.isBefore(DateTime.now()) || !targetFloorIsSafe(target, true))
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
				target = findSafeSpot(new RunsafeLocation(world, randomX + originX + 0.5, 64.0D, randomZ + originZ + 0.5), true);
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
	}

	private final ConcurrentHashMap<String, WarpParameters> snazzyWarps = new ConcurrentHashMap<String, WarpParameters>();
	private final IOutput console;
	private Duration change_after;
	private static final String signHeader = ChatColour.DARK_BLUE.toBukkit() + "[Snazzy Warp]";
}