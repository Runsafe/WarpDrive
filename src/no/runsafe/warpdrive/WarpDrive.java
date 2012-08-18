package no.runsafe.warpdrive;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.event.player.IPlayerInteractEvent;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.util.HashMap;

public class WarpDrive extends RunsafePlugin implements IPlayerInteractEvent
{
	@Override
	protected void PluginSetup()
	{
	}

	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		RunsafePlayer thePlayer = event.getPlayer();
		RunsafeBlock theBlock = event.getBlock();

		if (theBlock != null)
		{
			BlockState theBlockState = theBlock.getRaw().getState();

			if ((theBlockState instanceof Sign))
			{
				Sign theSign = (Sign) theBlockState;

				if (theSign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[Snazzy Warp]"))
				{
					setRandomWarp(theSign, thePlayer);
					event.setCancelled(true);
				}
			}
		}
	}

	private void setRandomWarp(Sign theSign, RunsafePlayer thePlayer)
	{
		String warpName = theSign.getLine(1);

		long difference = 0L;
		if (this.signWarpExpires.containsKey(warpName))
		{
			difference = System.currentTimeMillis() / 1000L - (Long) this.signWarpExpires.get(warpName);
		}

		if ((!this.signWarpLocations.containsKey(warpName)) || (difference > 60L))
		{
			int radius = Integer.parseInt(theSign.getLine(2));
			int boundingRadius = Integer.parseInt(theSign.getLine(3));

			boolean negX = Math.random() * 100.0D > 50.0D;
			boolean negZ = Math.random() * 100.0D > 50.0D;

			double randomXB = (negX ? -1 : 1) * ((radius - boundingRadius) * Math.random() + boundingRadius);
			double randomZB = (negZ ? -1 : 1) * ((radius - boundingRadius) * Math.random() + boundingRadius);

			double randomX = randomXB + theSign.getX();
			double randomZ = randomZB + theSign.getZ();

			RunsafeWorld theWorld = new RunsafeWorld(theSign.getWorld());
			RunsafeLocation newLocation = new RunsafeLocation(theWorld, randomX, 60.0D, randomZ);

			this.signWarpLocations.put(warpName, newLocation);
			this.signWarpExpires.put(warpName, System.currentTimeMillis() / 1000L);

			safePlayerTeleport(newLocation, thePlayer);
		}
		else
		{
			long out = System.currentTimeMillis() / 1000L - (Long) this.signWarpExpires.get(warpName);
			RunsafeLocation location = (RunsafeLocation) this.signWarpLocations.get(warpName);
			safePlayerTeleport(location, thePlayer);
		}
	}

	public void safePlayerTeleport(RunsafeLocation location, RunsafePlayer player)
	{
		boolean canTeleport = false;
		RunsafeWorld world = location.getWorld();
		int x = location.getBlockX();
		int z = location.getBlockZ();

		int maxHeight = world.getMaxHeight();
		int minHeight = 50;

		if (world.getRaw().getEnvironment() == World.Environment.NETHER)
		{
			maxHeight = 126;
			minHeight = 0;
		}

		int y = maxHeight;
		while (y > minHeight)
		{
			RunsafeBlock theBlockBelow = world.getBlockAt(x, y, z);
			RunsafeBlock theBlockWithin = world.getBlockAt(x, y + 1, z);
			RunsafeBlock theBlockAbove = world.getBlockAt(x, y + 2, z);

			if (((!theBlockBelow.canPassThrough()) || (theBlockBelow.getTypeId() == Material.WATER.getId())) && (theBlockWithin.canPassThrough()) && (!theBlockWithin.isHazardous()) && (theBlockAbove.canPassThrough()) && (!theBlockAbove.isHazardous()) && (!world.getBlockAt(x + 1, y + 1, z).isHazardous()) && (!world.getBlockAt(x - 1, y + 1, z).isHazardous()) && (!world.getBlockAt(x, y + 1, z + 1).isHazardous()) && (!world.getBlockAt(x, y + 1, z - 1).isHazardous()) && (!world.getBlockAt(x + 1, y + 1, z - 1).isHazardous()) && (!world.getBlockAt(x - 1, y + 1, z + 1).isHazardous()) && (!world.getBlockAt(x - 1, y + 1, z - 1).isHazardous()) && (!world.getBlockAt(x + 1, y + 1, z + 1).isHazardous()))
			{
				canTeleport = true;
				break;
			}

			y--;
		}

		if (canTeleport)
		{
			player.setFallDistance(0.0F);
			player.teleport(world, x + 0.5D, y + 1, z + 0.5D);
		}
		else
		{
			location.setX(x + 1);
			location.setZ(z + 1);
			safePlayerTeleport(location, player);
		}
	}

	private HashMap<String, RunsafeLocation> signWarpLocations = new HashMap<String, RunsafeLocation>();
	private HashMap<String, Long> signWarpExpires = new HashMap<String, Long>();
}
