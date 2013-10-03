package no.runsafe.warpdrive.portals;

import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

public class PortalWarp
{
	public PortalWarp(String id, RunsafeLocation location, RunsafeLocation destination, PortalType type, int radius, String permission)
	{
		this.location = location;
		this.type = type;
		this.destinationWorld = destination.getWorld();
		this.radius = radius;
		this.perm = permission;

		if (this.type == PortalType.NORMAL || this.type == PortalType.RANDOM_RADIUS)
			this.destination = destination;
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

	public RunsafeWorld getPortalWorld()
	{
		return this.location.getWorld();
	}

	public RunsafeWorld getWorld()
	{
		return this.destinationWorld;
	}

	public RunsafeLocation getLocation()
	{
		return this.destination;
	}

	public RunsafeLocation getPortalLocation()
	{
		return location;
	}

	public boolean isInPortal(RunsafePlayer player)
	{
		return player.getLocation().distance(this.location) < 2;
	}

	public int getRadius()
	{
		return this.radius;
	}

	public void setLocked(boolean locked)
	{
		this.locked = locked;
	}

	public boolean isLocked()
	{
		return locked;
	}

	private String perm;
	private PortalType type;
	private RunsafeLocation location;
	private RunsafeLocation destination;
	private RunsafeWorld destinationWorld;
	private int radius;
	private boolean locked = false;
}
