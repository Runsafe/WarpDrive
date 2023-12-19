package no.runsafe.warpdrive.smartwarp;

import no.runsafe.framework.api.*;
import no.runsafe.framework.api.block.ISign;
import no.runsafe.framework.api.event.IAsyncEvent;
import no.runsafe.framework.api.event.player.IPlayerDamageEvent;
import no.runsafe.framework.api.event.player.IPlayerRightClickSign;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginDisabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IPlayerProvider;
import no.runsafe.framework.api.server.IWorldManager;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.framework.text.ChatColour;
import no.runsafe.framework.timer.ForegroundWorker;
import no.runsafe.warpdrive.Engine;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SnazzyWarp extends ForegroundWorker<String, SnazzyWarp.WarpParameters> implements
                                                                                    IPlayerRightClickSign, IAsyncEvent,
                                                                                    IConfigurationChanged,
                                                                                    IPlayerDamageEvent, IPluginDisabled
{
	public SnazzyWarp(
		IScheduler scheduler, Engine engine, IDebug output, IPlayerProvider playerProvider, IWorldManager worldManager,
		IConsole console
	)
	{
		super(scheduler, 2);
		this.engine = engine;
		debugger = output;
		this.playerProvider = playerProvider;
		this.worldManager = worldManager;
		this.console = console;
	}

	@Override
	public boolean OnPlayerRightClickSign(IPlayer thePlayer, RunsafeMeta runsafeItemStack, ISign theSign)
	{
		if (theSign.getLine(0).equalsIgnoreCase(signHeader))
		{
			if (!snazzyWarps.containsKey(theSign.getLine(1))
				|| snazzyWarps.get(theSign.getLine(1)).refresh(theSign))
				snazzyWarps.put(theSign.getLine(1), new WarpParameters(theSign));
			Push(thePlayer.getName(), snazzyWarps.get(theSign.getLine(1)));
			debugger.debugFine("Pushed player click..");
			return false;
		}
		return true;
	}

	@Override
	public void process(String player, WarpParameters parameters)
	{
		if (parameters == null)
			return;
		debugger.outputDebugToConsole(String.format("Player %s teleporting", player), Level.FINE);
		IPlayer target = playerProvider.getPlayer(player);
		ILocation destination = parameters.getTarget();
		if (skyFall)
		{
			destination.setY(300);
			destination.setPitch(0);
		}
		if (target != null && target.isOnline() && destination != null)
		{
			if (target.teleport(destination))
				fallen.add(target);
		} else
			debugger.outputDebugToConsole("Unable to find destination..", Level.FINE);
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		change_after = Duration.ofSeconds(configuration.getConfigValueAsInt("snazzy.timeout"));
		skyFall = configuration.getConfigValueAsBoolean("snazzy.skyfall");
	}

	@Override
	public void OnPlayerDamage(IPlayer player, RunsafeEntityDamageEvent event)
	{
		if (event.getCause() == RunsafeEntityDamageEvent.RunsafeDamageCause.FALL)
		{
			if (fallen.contains(player))
			{
				event.cancel();
				fallen.remove(player);
			}
		}
	}

	@Override
	public void OnPluginDisabled()
	{
		if (!fallen.isEmpty())
			console.logInformation("Teleporting %d falling players due to plugin shutdown.", fallen.size());
		for (IPlayer player : fallen)
		{
			if (player != null)
			{
				ILocation location = player.getLocation();
				if (location != null)
				{
					player.teleport(location.findTop());
				}
			}
		}
	}

	private final Engine engine;

	public class WarpParameters
	{
		WarpParameters(ISign sign)
		{
			maxDistance = Integer.parseInt(sign.getLine(2));
			minDistance = Integer.parseInt(sign.getLine(3));
			world = worldManager.getWorld(sign.getLine(1));
			if (world == null)
				debugger.debugFine("New warp sign created outside a world!");
			else
				debugger.debugFine(
					"Configured new warp sign %s in world %s [%d-%d].",
					sign.getLine(1),
					world.getName(),
					minDistance,
					maxDistance
				);
		}

		public ILocation getTarget()
		{
			if (target == null || expires.isBefore(Instant.now()) || !engine.targetFloorIsSafe(target, true))
			{
				target = getNewTarget();
				expires = Instant.now().plus(change_after);
			}
			return target;
		}

		private ILocation getNewTarget()
		{
			boolean negX = rng.nextInt(100) > 50;
			boolean negZ = rng.nextInt(100) > 50;
			double randomX = (negX ? -1 : 1) * rng.nextInt(maxDistance);
			double randomZ = (negZ ? -1 : 1) * rng.nextInt(maxDistance);
			if (randomX < minDistance && randomZ < minDistance)
			{
				if (rng.nextBoolean())
					randomX += (negX ? -1 : 1) * minDistance;
				else
					randomZ += (negZ ? -1 : 1) * minDistance;
			}
			ILocation target = null;
			int retries = 10;
			while (target == null && retries-- > 0)
			{
				ILocation randomLocation = world.getLocation(randomX + 0.5, 64.0D, randomZ + 0.5);
				if (randomLocation != null)
				{
					target = engine.findRandomSafeSpot(randomLocation);
				}
			}
			if (target != null)
			{
				target.setX(target.getBlockX() + 0.5);
				target.setY(target.getY() - 1);
				target.setZ(target.getBlockZ() + 0.5);
			}
			return target;
		}

		private final IWorld world;
		private final int maxDistance;
		private final int minDistance;
		private final Random rng = new Random();
		private Instant expires = Instant.now();
		private ILocation target;

		public boolean refresh(ISign sign)
		{
			return maxDistance != Integer.parseInt(sign.getLine(2)) || minDistance != Integer.parseInt(sign.getLine(3));
		}
	}

	private final ConcurrentHashMap<String, WarpParameters> snazzyWarps = new ConcurrentHashMap<>();
	private final List<IPlayer> fallen = new ArrayList<>(0);
	private final IDebug debugger;
	private final IPlayerProvider playerProvider;
	private final IWorldManager worldManager;
	private final IConsole console;
	private Duration change_after;
	private boolean skyFall = false;
	public static final String signHeader = ChatColour.DARK_BLUE.toBukkit() + "[Snazzy Warp]";
	public static final String signTag = "[snazzy warp]";
}