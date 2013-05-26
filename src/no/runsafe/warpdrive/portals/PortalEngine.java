package no.runsafe.warpdrive.portals;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.player.IPlayerPortalEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.event.player.RunsafePlayerPortalEvent;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.SmartWarpDrive;

import java.util.ArrayList;
import java.util.List;

public class PortalEngine implements IPlayerPortalEvent, IConfigurationChanged
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
		this.portals = this.repository.getPortalWarps();
		this.output.write(this.portals.size() + " portals loaded.");
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
	public void OnPlayerPortalEvent(RunsafePlayerPortalEvent event)
	{
		RunsafePlayer player = event.getPlayer();
		for (PortalWarp portal : this.portals)
		{
			if (portal.isInPortal(player))
			{
				event.setCancelled(true);
				if (portal.canTeleport(player))
					this.teleportPlayer(portal, player);
				else
					player.sendColouredMessage("&cYou do not have permission to use this portal.");
			}
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration iConfiguration)
	{
		this.reloadPortals();
	}

	private List<PortalWarp> portals = new ArrayList<PortalWarp>();
	private PortalRepository repository;
	private SmartWarpDrive smartWarpDrive;
	private IOutput output;
}
