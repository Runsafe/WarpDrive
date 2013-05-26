package no.runsafe.warpdrive.portals;

import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.player.RunsafePlayer;

public class PortalWarp
{
	public PortalWarp(String id, RunsafeLocation location, RunsafeWorld destWorld, double destX, double destY, double destZ, float destYaw, float destPitch, PortalType type)
	{
		this.id = id;
		this.location = location;
		this.type = type;
		this.destinationWorld = destWorld;

		if (this.type == PortalType.NORMAL)
			this.destination = new RunsafeLocation(destWorld, destX, destY, destZ, destYaw, destPitch);
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
		return (this.perm == null || player.hasPermission(this.perm));
	}

	public PortalType getType()
	{
		return this.type;
	}

	public RunsafeWorld getWorld()
	{
		return this.destinationWorld;
	}

	public RunsafeLocation getLocation()
	{
		return this.destination;
	}

	public boolean isInPortal(RunsafePlayer player)
	{
		return player.getLocation().distance(this.location) < 2;
	}

	private String id;
	private String perm;
	private PortalType type;
	private RunsafeLocation location;
	private RunsafeLocation destination;
	private RunsafeWorld destinationWorld;
	private int innerRadius = 0;
	private int outerRadius = 0;
}
