package no.runsafe.warpdrive.portals;

import no.runsafe.framework.api.*;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.chunk.IChunk;
import no.runsafe.framework.api.event.player.IPlayerCustomEvent;
import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
import no.runsafe.framework.api.event.player.IPlayerPortal;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IWorldManager;
import no.runsafe.framework.api.vector.IRegion3D;
import no.runsafe.framework.internal.vector.Point3D;
import no.runsafe.framework.internal.vector.Region3D;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
import no.runsafe.warpdrive.smartwarp.SmartWarpDrive;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PortalEngine implements IPlayerPortal, IConfigurationChanged, IPlayerInteractEvent, IPlayerCustomEvent
{
	public PortalEngine(PortalRepository repository, SmartWarpDrive smartWarpDrive, IDebug debugger, IConsole console, IScheduler scheduler, IWorldManager worldManager)
	{
		this.repository = repository;
		this.smartWarpDrive = smartWarpDrive;
		this.debugger = debugger;
		this.console = console;
		this.scheduler = scheduler;
		this.worldManager = worldManager;
	}

	public void reloadPortals()
	{
		this.portals.clear();
		int portalCount = 0;
		for (PortalWarp portal : this.repository.getPortalWarps())
		{
			ILocation location = portal.getPortalLocation();
			if (location == null || location.getWorld() == null)
			{
				abandoned_portals.put(portal.getID(), portal);
				this.console.logWarning("Found portal %s without a valid location, ignoring", portal.getID());
				continue;
			}
			String portalWorldName = location.getWorld().getName();
			if (!portals.containsKey(portalWorldName))
				portals.put(portalWorldName, new HashMap<>());

			portalCount += 1;
			portals.get(portalWorldName).put(portal.getID(), portal);
		}
		this.console.logInformation("%d portals loaded in %d worlds.", portalCount, portals.size());
	}

	public void teleportPlayer(final PortalWarp portal, final IPlayer player)
	{
		this.debugger.debugFine("Teleporting player in portal: " + player.getName());
		this.debugger.debugFine("Portal lock state: " + (portal.isLocked() ? "locked" : "unlocked"));

		if (portal.getType() == PortalType.NORMAL)
			scheduler.startSyncTask(() -> player.teleport(portal.getLocation()), 0);

		if (portal.getType() == PortalType.RANDOM_SURFACE)
			this.smartWarpDrive.EngageSurface(player, portal.getWorld(), portal.isLocked());

		if (portal.getType() == PortalType.RANDOM_CAVE)
			this.smartWarpDrive.EngageCave(player, portal.getWorld(), portal.isLocked());

		if (portal.getType() == PortalType.RANDOM_RADIUS)
			this.randomRadiusTeleport(player, portal.getLocation(), portal.getRadius());

		portal.setLocked(false);
	}

	@Override
	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		IBlock block = event.getBlock();
		if (block != null && block.is(Item.Redstone.Button.Stone))
		{
			IPlayer player = event.getPlayer();
			IWorld world = player.getWorld();

			if (world != null && portals.containsKey(world.getName()))
			{
				Collection<PortalWarp> portalList = portals.get(world.getName()).values();
				for (PortalWarp warp : portalList)
					if (warp.getPortalLocation().distance(block.getLocation()) < 5)
						warp.setLocked(true);
			}
		}
	}

	private void randomRadiusTeleport(final IPlayer player, ILocation theLocation, int radius)
	{
		final ILocation location = theLocation.clone();
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

		scheduler.startSyncTask(() -> player.teleport(location), 0);
	}

	private int getRandom(int low, int high)
	{
		return low + (int) (Math.random() * ((high - low) + 1));
	}

	private boolean safeToTeleport(final ILocation location)
	{
		final boolean[] safe = {false};
		scheduler.runNow(() ->
		{
			if (location.getBlock().is(Item.Unavailable.Air))
			{
				location.incrementY(1);
				if (location.getBlock().is(Item.Unavailable.Air))
					safe[0] = true;
			}
		});
		return safe[0];
	}

	@Override
	public boolean OnPlayerPortal(IPlayer player, ILocation from, ILocation to)
	{
		this.debugger.debugFiner("Portal event detected: " + player.getName());
		IWorld playerWorld = player.getWorld();
		if (playerWorld == null)
			return false;

		String worldName = playerWorld.getName();
		if (portals.containsKey(worldName))
		{
			for (PortalWarp portal : this.portals.get(worldName).values())
			{
				if (portal.isInPortal(player))
				{
					debugger.debugFine("Player %s using portal %s in world %s.", player.getName(), portal.getID(), portal.getWorldName());
					if (portal.canTeleport(player))
						this.teleportPlayer(portal, player);
					else
						player.sendColouredMessage("&cYou do not have permission to use this portal.");
					return false;
				}
			}
		}
		if (pending.containsKey(player.getName()))
		{
			finalizeWarp(player);
			return OnPlayerPortal(player, from, to);
		}

		if (netherWorlds.contains(worldName))
		{
			IWorld netherWorld = worldManager.getWorld(worldName + "_nether");
			if (netherWorld != null)
			{
				netherTeleport(netherWorld.getLocation(from.getX(), from.getY() / 2, from.getZ()), player);
				return false;
			}
		}
		else if (worldName.contains("_nether"))
		{
			IWorld world = worldManager.getWorld(worldName.replace("_nether", ""));
			if (world != null)
			{
				netherTeleport(world.getLocation(from.getX(), from.getY() * 2, from.getZ()), player);
				return false;
			}
		}

		return true;
	}

	private void netherTeleport(ILocation location, IPlayer player)
	{
		IChunk chunk = location.getChunk();

		if (chunk.isUnloaded()) // Make sure we have the chunk loaded before editing.
			chunk.load();

		// Y correction.
		if (location.getY() > netherMaxY)
			location.setY(netherMaxY);
		else if (location.getY() < netherMinY)
			location.setY(netherMinY);

		// Build a platform for the player to stand on.
		ILocation blockLocation = location.clone();
		if (!blockLocation.getBlock().is(Item.Unavailable.Portal))
			blockLocation.getBlock().set(Item.Unavailable.Air);
		blockLocation.offset(0, -1, 0);
		blockLocation.getBlock().set(Item.BuildingBlock.Obsidian);
		blockLocation.offset(0, 2, 0);
		if (!blockLocation.getBlock().is(Item.Unavailable.Portal))
			blockLocation.getBlock().set(Item.Unavailable.Air);

		player.teleport(location); // Teleport the player.
	}

	@Override
	public void OnConfigurationChanged(IConfiguration iConfiguration)
	{
		this.reloadPortals();
		netherWorlds = iConfiguration.getConfigValueAsList("netherWorlds");
		netherMaxY = iConfiguration.getConfigValueAsInt("netherPortals.maxY");
		netherMinY = iConfiguration.getConfigValueAsInt("netherPortals.minY");
	}

	public PortalWarp getWarp(IWorld world, String portalName)
	{
		String worldName = world.getName();
		if (portals.containsKey(worldName) && portals.get(worldName).containsKey(portalName))
			return portals.get(worldName).get(portalName);
		if (abandoned_portals.containsKey(portalName))
			return abandoned_portals.get(portalName);
		return null;
	}

	public void createWarp(IPlayer creator, String portalName, ILocation destination, PortalType type, String permission) throws NullPointerException
	{
		PortalWarp warp = new PortalWarp(portalName, null, destination, type, -1, permission, null, null); // Create new warp.
		pending.put(creator.getName(), warp);
	}

	public void createRegionWarp(IWorld portalWorld, String region, String portalName, ILocation destination, String permission)
	{
		PortalWarp warp = new PortalWarp(portalName, portalWorld.getLocation(0.0, 0.0, 0.0), destination, PortalType.NORMAL, 0, permission, null, region);
		repository.storeWarp(warp);
		portals.get(portalWorld.getName()).put(warp.getID(), warp);
	}

	public void finalizeWarp(IPlayer player)
	{
		IRegion3D portalArea = scanArea(player.getLocation());
		PortalWarp warp = pending.get(player.getName());
		warp.setRegion(portalArea);
		warp.setLocation(player.getLocation());
		pending.remove(player.getName());
		String worldName = player.getWorldName();
		if (!portals.containsKey(worldName)) // Check if we're missing a container for this world.
			portals.put(worldName, new HashMap<>()); // Create a new warp container.

		repository.storeWarp(warp); // Store the warp in the database.
		portals.get(worldName).put(warp.getID(), warp); // Add to the in-memory warp storage.
	}

	private IRegion3D scanArea(ILocation location)
	{
		Map<Integer, Map<Integer, Map<Integer, Boolean>>> portalMap = new HashMap<>();
		getNeighbouringPortalBlocks(location, portalMap);
		int xMin = Collections.min(portalMap.keySet());
		int xMax = Collections.max(portalMap.keySet());
		int yMin = Integer.MAX_VALUE;
		int yMax = Integer.MIN_VALUE;
		int zMin = Integer.MAX_VALUE;
		int zMax = Integer.MIN_VALUE;
		for (Integer x : portalMap.keySet())
		{
			yMin = Math.min(yMin, Collections.min(portalMap.get(x).keySet()));
			yMax = Math.max(yMax, Collections.max(portalMap.get(x).keySet()));
			for (Integer y : portalMap.get(x).keySet())
			{
				zMin = Math.min(zMin, Collections.min(portalMap.get(x).get(y).keySet()));
				zMax = Math.max(zMax, Collections.max(portalMap.get(x).get(y).keySet()));
			}
		}
		return new Region3D(new Point3D(xMin, yMin, zMin), new Point3D(xMax + 1, yMax + 1, zMax + 1));
	}

	private void getNeighbouringPortalBlocks(ILocation location, Map<Integer, Map<Integer, Map<Integer, Boolean>>> portalMap)
	{
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		if (!portalMap.containsKey(x))
			portalMap.put(x, new HashMap<>());
		if (!portalMap.get(x).containsKey(y))
			portalMap.get(x).put(y, new HashMap<>());
		if (!portalMap.get(x).get(y).containsKey(z))
			portalMap.get(x).get(y).put(z, true);
		else
			return;

		ILocation neighbour = location.clone();
		neighbour.offset(-1, 0, 0);
		if (neighbour.getBlock().is(Item.Unavailable.Portal))
			getNeighbouringPortalBlocks(neighbour, portalMap);
		neighbour.offset(2, 0, 0);
		if (neighbour.getBlock().is(Item.Unavailable.Portal))
			getNeighbouringPortalBlocks(neighbour, portalMap);
		neighbour.offset(-1, -1, 0);
		if (neighbour.getBlock().is(Item.Unavailable.Portal))
			getNeighbouringPortalBlocks(neighbour, portalMap);
		neighbour.offset(0, 2, 0);
		if (neighbour.getBlock().is(Item.Unavailable.Portal))
			getNeighbouringPortalBlocks(neighbour, portalMap);
		neighbour.offset(0, -1, -1);
		if (neighbour.getBlock().is(Item.Unavailable.Portal))
			getNeighbouringPortalBlocks(neighbour, portalMap);
		neighbour.offset(0, 0, 2);
		if (neighbour.getBlock().is(Item.Unavailable.Portal))
			getNeighbouringPortalBlocks(neighbour, portalMap);
	}

	public void updateWarp(PortalWarp warp)
	{
		String worldName = warp.getWorldName();
		if (!portals.containsKey(worldName))
			portals.put(worldName, new HashMap<>());

		repository.updatePortalWarp(warp); // Store changes in the database.
		portals.get(worldName).put(warp.getID(), warp);

		if (abandoned_portals.containsKey(warp.getID()))
		{
			this.console.logInformation("Moved portal %s to new world %s", warp.getID(), worldName);
			abandoned_portals.remove(warp.getID());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void OnPlayerCustomEvent(RunsafeCustomEvent event)
	{
		if (event.getEvent().equals("region.enter"))
		{
			final IPlayer player = event.getPlayer();
			String playerWorld = player.getWorldName();

			if (playerWorld == null)
				return;

			Map<String, String> data = (Map<String, String>) event.getData();
			String regionName = data.get("region");

			if (portals.containsKey(playerWorld))
			{
				for (PortalWarp portal : portals.get(playerWorld).values())
				{
					if (portal.hasEnterRegion() && portal.getEnterRegion().equals(regionName))
					{
						if (portal.canTeleport(player))
							teleportPlayer(portal, player);
						else
							player.sendColouredMessage("&cYou do not have permission to use this portal.");
						return;
					}
				}
			}
		}
	}

	private List<String> netherWorlds = new ArrayList<>();
	private final Map<String, PortalWarp> pending = new ConcurrentHashMap<>();
	private final Map<String, Map<String, PortalWarp>> portals = new HashMap<>();
	private final Map<String, PortalWarp> abandoned_portals = new HashMap<>();
	private final PortalRepository repository;
	private final SmartWarpDrive smartWarpDrive;
	private final IDebug debugger;
	private final IConsole console;
	private final IScheduler scheduler;
	private final IWorldManager worldManager;
	private int netherMaxY;
	private int netherMinY;
}
