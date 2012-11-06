package no.runsafe.warpdrive;

import no.runsafe.framework.event.IAsyncEvent;
import no.runsafe.framework.event.player.IPlayerRightClickSign;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.block.RunsafeSign;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.ChatColor;

import java.util.HashMap;

import static no.runsafe.warpdrive.StaticWarp.safePlayerTeleport;

public class SnazzyWarp implements IPlayerRightClickSign, IAsyncEvent
{
	@Override
	public boolean OnPlayerRightClickSign(RunsafePlayer thePlayer, RunsafeItemStack runsafeItemStack, RunsafeSign theSign)
	{
		if (theSign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[Snazzy Warp]"))
		{
			safePlayerTeleport(getRandomWarp(theSign), thePlayer, true);
			return false;
		}
		return true;
	}

	private RunsafeLocation getRandomWarp(RunsafeSign theSign)
	{
		String warpName = theSign.getLine(1);

		if (this.signWarpExpires.containsKey(warpName))
		{
			long age = (System.currentTimeMillis() - this.signWarpExpires.get(warpName)) / 1000;
			if (age <= 60)
				return signWarpLocations.get(warpName);
		}
		int radius = Integer.parseInt(theSign.getLine(2));
		int boundingRadius = Integer.parseInt(theSign.getLine(3));

		boolean negX = Math.random() * 100.0D > 50;
		boolean negZ = Math.random() * 100.0D > 50;

		double randomXB = (negX ? -1 : 1) * ((radius - boundingRadius) * Math.random() + boundingRadius);
		double randomZB = (negZ ? -1 : 1) * ((radius - boundingRadius) * Math.random() + boundingRadius);

		double randomX = randomXB + theSign.getX();
		double randomZ = randomZB + theSign.getZ();

		RunsafeLocation newLocation = new RunsafeLocation(theSign.getWorld(), randomX, 60.0D, randomZ);

		signWarpLocations.put(warpName, newLocation);
		signWarpExpires.put(warpName, System.currentTimeMillis());

		return signWarpLocations.get(warpName);
	}

	private final HashMap<String, RunsafeLocation> signWarpLocations = new HashMap<String, RunsafeLocation>();
	private final HashMap<String, Long> signWarpExpires = new HashMap<String, Long>();
}
