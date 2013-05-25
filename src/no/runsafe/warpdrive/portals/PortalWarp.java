package no.runsafe.warpdrive.portals;

import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;

public class PortalWarp
{
	public PortalWarp(String id, RunsafeLocation location, RunsafeLocation destination, PortalType type)
	{
		this.id = id;
		this.location = location;
		this.destination = destination;
		this.type = type;
	}

	public void setInnerRadius(int innerRadius)
	{
		this.innerRadius = innerRadius;
	}

	public void setOuterRadius(int outerRadius)
	{
		this.outerRadius = outerRadius;
	}

	public void setPermission(String perm)
	{
		this.perm = perm;
	}

	public boolean canTeleport(RunsafePlayer player)
	{
		return (this.perm != null && player.hasPermission(this.perm));
	}

	public PortalType getType()
	{
		return this.type;
	}

	public boolean isInPortal(RunsafePlayer player)
	{
		return (player.getLocation().distance(this.location)) < 2;
	}

	public void teleportPlayer(RunsafePlayer player)
	{
		if (this.type == PortalType.NORMAL)
			player.teleport(this.destination);
	}

	private String id;
	private String perm;
	private PortalType type;
	private RunsafeLocation location;
	private RunsafeLocation destination;
	private int innerRadius = 0;
	private int outerRadius = 0;
}
