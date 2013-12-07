package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.OptionalArgument;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.warpdrive.portals.PortalEngine;
import no.runsafe.warpdrive.portals.PortalType;
import no.runsafe.warpdrive.portals.PortalWarp;

import java.util.Map;

public class SetPortal extends PlayerAsyncCommand
{
	// ToDo: Add support for permissions.
	// ToDo: Add support for radius.

	public SetPortal(IScheduler scheduler, PortalEngine engine)
	{
		super(
			"setportal",
			"Sets your current location as a portal",
			"runsafe.portal.set",
			scheduler,
			new RequiredArgument("portalID"),
			new RequiredArgument("portalType"),
			new OptionalArgument("destination")
		);
		this.engine = engine;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, Map<String, String> parameters)
	{
		RunsafeWorld playerWorld = player.getWorld();
		if (playerWorld == null)
			return "Invalid world."; // Player's world is null, return with an error.

		String portalName = parameters.get("portalID"); // The ID of the portal.
		PortalWarp warp = engine.getWarp(playerWorld, portalName); // Grab the portal warp.
		boolean setDestination = parameters.containsKey("destination"); // Should we be setting it as a destination.
		RunsafeLocation playerLocation = player.getLocation(); // Location of the player.

		if (playerLocation == null)
			return "Invalid location.";

		if (warp != null)
		{
			// The portal exists, edit it.
			if (setDestination)
				warp.setDestination(playerLocation); // Set the warps destination.
			else
				warp.setLocation(playerLocation); // Set the warps location (entry-point).

			engine.updateWarp(warp); // Update the warp.
			return "Warp modified at " + playerLocation.toString();
		}
		else
		{
			RunsafeLocation worldStart = new RunsafeLocation(playerWorld, 0, 0, 0); // Placeholder location.

			// Create the new warp in the engine.
			try
			{
				engine.createWarp(
					portalName,
					setDestination ? worldStart : playerLocation,
					setDestination ? playerLocation : worldStart,
					PortalType.getPortalType(Integer.parseInt(parameters.get("portalType")))
				);
			}
			catch (NullPointerException e)
			{
				return "Error: " + e.getMessage();
			}

			return "Warp created at " + playerLocation.toString();
		}
	}

	private PortalEngine engine;
}
