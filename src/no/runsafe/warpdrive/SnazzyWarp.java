package no.runsafe.warpdrive;

import no.runsafe.framework.event.IAsyncEvent;
import no.runsafe.framework.event.player.IPlayerRightClickBlockEvent;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.event.player.RunsafePlayerClickEvent;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.util.HashMap;

import static no.runsafe.warpdrive.StaticWarp.safePlayerTeleport;

public class SnazzyWarp implements IPlayerRightClickBlockEvent, IAsyncEvent
{
	@Override
	public void OnPlayerRightClick(RunsafePlayerClickEvent event)
	{
		RunsafePlayer thePlayer = event.getPlayer();
		RunsafeBlock theBlock = event.getBlock();

		BlockState theBlockState = theBlock.getRaw().getState();
		if ((theBlockState instanceof Sign))
		{
			Sign theSign = (Sign) theBlockState;

			if (theSign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[Snazzy Warp]"))
			{
				safePlayerTeleport(getRandomWarp(theSign), thePlayer);
				event.setCancelled(true);
			}
		}
	}

	private RunsafeLocation getRandomWarp(Sign theSign)
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

		RunsafeWorld theWorld = new RunsafeWorld(theSign.getWorld());
		RunsafeLocation newLocation = new RunsafeLocation(theWorld, randomX, 60.0D, randomZ);

		signWarpLocations.put(warpName, newLocation);
		signWarpExpires.put(warpName, System.currentTimeMillis());

		return signWarpLocations.get(warpName);
	}

	private HashMap<String, RunsafeLocation> signWarpLocations = new HashMap<String, RunsafeLocation>();
	private HashMap<String, Long> signWarpExpires = new HashMap<String, Long>();
}
