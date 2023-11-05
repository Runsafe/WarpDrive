package no.runsafe.warpdrive.portals;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.vector.IRegion3D;
import no.runsafe.warpdrive.WarpDrive;

public class PortalWarp
{
	private IRegion3D region;

	public PortalWarp(String id, ILocation location, ILocation destination, PortalType type, int radius, String permission, IRegion3D region, String enterRegion) throws NullPointerException
	{
		this.id = id;
		this.location = location;
		this.region = region;
		this.type = type;

		if (destination == null || destination.getWorld() == null)
			throw new NullPointerException("Invalid destination world provided.");

		this.destinationWorld = destination.getWorld();
		this.radius = radius;

		perm = permission;

		if (this.type == PortalType.NORMAL || this.type == PortalType.RANDOM_RADIUS)
			this.destination = destination;

		this.enterRegion = enterRegion;
	}

	public String getID()
	{
		return id;
	}

	public void setPermission(String perm)
	{
		this.perm = perm;
	}

	public String getPermission()
	{
		return perm;
	}

	public boolean canTeleport(IPlayer player)
	{
		return (this.perm == null || player.hasPermission(this.perm));
	}

	public PortalType getType()
	{
		return this.type;
	}

	public IWorld getPortalWorld()
	{
		return this.location.getWorld();
	}

	public IWorld getWorld()
	{
		return this.destinationWorld;
	}

	public ILocation getLocation()
	{
		return this.destination;
	}

	public ILocation getPortalLocation()
	{
		return location;
	}

	public boolean isInPortal(IPlayer player)
	{
		ILocation playerLocation = player.getLocation();
		if (playerLocation == null)
		{
			return false;
		}
		if (region != null)
		{
			WarpDrive.debug.debugFine("Checking if %s is within %s", playerLocation, region);
			return region.contains(playerLocation.getPoint());
		}
		WarpDrive.debug.debugFine("Checking if %s is close to %s", playerLocation, location);
		return playerLocation.distance(this.location) < 2;
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

	public String getWorldName()
	{
		return location.getWorld().getName();
	}

	public double getX()
	{
		return location.getX();
	}

	public double getY()
	{
		return location.getY();
	}

	public double getZ()
	{
		return location.getZ();
	}

	public String getDestinationWorldName()
	{
		return destinationWorld.getName();
	}

	public double getDestinationX()
	{
		return destination.getX();
	}

	public double getDestinationY()
	{
		return destination.getY();
	}

	public double getDestinationZ()
	{
		return destination.getZ();
	}

	public float getDestinationYaw()
	{
		return destination.getYaw();
	}

	public float getDestinationPitch()
	{
		return destination.getPitch();
	}

	public void setLocation(ILocation location)
	{
		this.location = location;
	}

	public void setDestination(ILocation location)
	{
		destination = location;
		destinationWorld = location.getWorld();
	}

	public void setRegion(IRegion3D region)
	{
		this.region = region;
	}

	public IRegion3D getRegion()
	{
		return region;
	}

	public void setEnterRegion(String region)
	{
		enterRegion = region;
	}

	public String getEnterRegion()
	{
		return enterRegion;
	}

	public boolean hasEnterRegion()
	{
		return enterRegion != null;
	}

	private final String id;
	private String perm;
	private final PortalType type;
	private ILocation location;
	private ILocation destination;
	private IWorld destinationWorld;
	private final int radius;
	private boolean locked = false;
	private String enterRegion;
}
