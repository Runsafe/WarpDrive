package no.runsafe.warpdrive.portals;

import no.runsafe.framework.event.player.IPlayerPortalEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.event.player.RunsafePlayerPortalEvent;
import no.runsafe.framework.server.player.RunsafePlayer;

import java.util.ArrayList;
import java.util.List;

public class PortalEngine implements IPlayerPortalEvent
{
	public PortalEngine(PortalRepository repository, IOutput output)
	{
		this.repository = repository;
		this.output = output;

		this.reloadPortals();
	}

	public void reloadPortals()
	{
		this.portals = this.repository.getPortalWarps();
		this.output.write(this.portals.size() + " portals loaded.");
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
					portal.teleportPlayer(player);
				else
					player.sendColouredMessage("&cYou do not have permission to use this portal.");
			}
		}
	}

	private List<PortalWarp> portals = new ArrayList<PortalWarp>();
	private PortalRepository repository;
	private IOutput output;
}
