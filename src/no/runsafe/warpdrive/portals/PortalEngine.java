package no.runsafe.warpdrive.portals;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.player.IPlayerPortal;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.SmartWarpDrive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalEngine implements IPlayerPortal, IConfigurationChanged
{
	public PortalEngine(PortalRepository repository, SmartWarpDrive smartWarpDrive, IOutput output)
	{
		this.repository = repository;
		this.smartWarpDrive = smartWarpDrive;
		this.output = output;

		this.reloadPortals();
	}

	public void reloadPortals()
	{
		int portalCount = 0;
		for (PortalWarp portal : this.repository.getPortalWarps())
		{
			if (!portals.containsKey(portal.getWorld().getName()))
				portals.put(portal.getWorld().getName(), new ArrayList<PortalWarp>());

			portalCount += 1;
			portals.get(portal.getWorld().getName()).add(portal);
		}
		this.output.write(portalCount + " portals loaded.");
	}

	public void teleportPlayer(PortalWarp portal, RunsafePlayer player)
	{
		if (portal.getType() == PortalType.NORMAL)
			player.teleport(portal.getLocation());

		if (portal.getType() == PortalType.RANDOM_SURFACE)
			this.smartWarpDrive.Engage(player, portal.getWorld(), false);

		if (portal.getType() == PortalType.RANDOM_CAVE)
			this.smartWarpDrive.Engage(player, portal.getWorld(), true);
	}

	@Override
	public boolean OnPlayerPortal(RunsafePlayer player, RunsafeLocation from, RunsafeLocation to)
	{
		if (portals.containsKey(player.getWorld().getName()))
		{
			for (PortalWarp portal : this.portals.get(player.getWorld().getName()))
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
}
