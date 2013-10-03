package no.runsafe.warpdrive.portals;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IOutput;
import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
import no.runsafe.framework.api.event.player.IPlayerPortal;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.framework.minecraft.block.RunsafeBlock;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.minecraft.player.RunsafePlayer;
import no.runsafe.warpdrive.SmartWarpDrive;

import java.util.*;

public class PortalEngine implements IPlayerPortal, IConfigurationChanged, IPlayerInteractEvent
{
	public PortalEngine(PortalRepository repository, SmartWarpDrive smartWarpDrive, IOutput output)
	{
		this.repository = repository;
		this.smartWarpDrive = smartWarpDrive;
		this.output = output;
	}

	public void reloadPortals()
	{
		this.portals.clear();
		int portalCount = 0;
		for (PortalWarp portal : this.repository.getPortalWarps())
		{
			String portalWorldName = portal.getPortalWorld().getName();
			if (!portals.containsKey(portalWorldName))
				portals.put(portalWorldName, new ArrayList<PortalWarp>());

			portalCount += 1;
			portals.get(portalWorldName).add(portal);
		}
		this.output.logInformation("%d portals loaded in %d worlds.", portalCount, portals.size());
	}

	public void teleportPlayer(PortalWarp portal, RunsafePlayer player)
	{
		this.output.fine("Teleporting player in portal: " + player.getName());
		if (portal.getType() == PortalType.NORMAL)
			player.teleport(portal.getLocation());

		if (portal.getType() == PortalType.RANDOM_SURFACE)
			this.smartWarpDrive.Engage(player, portal.getWorld(), false, portal.isLocked());

		if (portal.getType() == PortalType.RANDOM_CAVE)
			this.smartWarpDrive.Engage(player, portal.getWorld(), true, portal.isLocked());

		if (portal.getType() == PortalType.RANDOM_RADIUS)
			this.randomRadiusTeleport(player, portal.getLocation(), portal.getRadius());

		portal.setLocked(false);
	}

	@Override
	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		RunsafeBlock block = event.getBlock();
		if (block != null && block.is(Item.Redstone.Button.Stone))
		{
			RunsafePlayer player = event.getPlayer();
			RunsafeWorld world = player.getWorld();

			if (world != null && portals.containsKey(world.getName()))
			{
				List<PortalWarp> portalList = portals.get(world.getName());
				for (PortalWarp warp : portalList)
					if (warp.getLocation().distance(block.getLocation()) < 5)
						warp.setLocked(true);
			}
		}
	}

	private void randomRadiusTeleport(RunsafePlayer player, RunsafeLocation theLocation, int radius)
	{
		RunsafeLocation location = new RunsafeLocation(
				theLocation.getWorld(),
				theLocation.getX(),
				theLocation.getY(),
				theLocation.getZ()
		);

		int highX = location.getBlockX() + radius;
		int highZ = location.getBlockZ() + radius;
		int lowX = location.getBlockX() - radius;
		int lowZ = location.getBlockZ() - radius;

		location.setX(this.getRandom(lowX, highX));
		location.setZ(this.getRandom(lowZ, highZ));

		while (!this.safeToTeleport(location))
		{
			location.setX(this.getRandom(lowX, highX));
			location.setZ(this.getRandom(lowZ, highZ));
		}
		player.teleport(location);
	}

	private int getRandom(int low, int high)
	{
		return low + (int)(Math.random() * ((high - low) + 1));
	}

	private boolean safeToTeleport(RunsafeLocation location)
	{
		if (location.getBlock().is(Item.Unavailable.Air))
		{
			location.incrementY(1);
			if (location.getBlock().is(Item.Unavailable.Air))
				return true;
		}
		return false;
	}

	@Override
	public boolean OnPlayerPortal(RunsafePlayer player, RunsafeLocation from, RunsafeLocation to)
	{
		this.output.fine("Portal event detected: " + player.getName());
		String worldName = player.getWorld().getName();
		if (portals.containsKey(worldName))
		{
			for (PortalWarp portal : this.portals.get(worldName))
			{
				if (portal.isInPortal(player))
				{
					if (portal.canTeleport(player))
						this.teleportPlayer(portal, player);
					else
						player.sendColouredMessage("&cYou do not have permission to use this portal.");
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration iConfiguration)
	{
		this.reloadPortals();
	}

	private Map<String, List<PortalWarp>> portals = new HashMap<String, List<PortalWarp>>();
	private PortalRepository repository;
	private SmartWarpDrive smartWarpDrive;
	private IOutput output;
	private boolean shouldLock = false;
}
