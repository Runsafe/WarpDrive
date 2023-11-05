package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OptionalArgument;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.argument.WorldArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.portals.PortalEngine;
import no.runsafe.warpdrive.portals.PortalType;
import no.runsafe.warpdrive.portals.PortalWarp;

public class SetPortal extends PlayerAsyncCommand
{
	public SetPortal(IScheduler scheduler, PortalEngine engine)
	{
		super(
			"setportal",
			"Hook your current location to a portal",
			"runsafe.portal.set",
			scheduler,
			new WorldArgument().require(),
			new RequiredArgument("name"),
			new OptionalArgument("permission")
		);
		this.engine = engine;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		IWorld portalWorld = parameters.getRequired("world");
		String portalName = parameters.getRequired("name");
		if (!portalName.matches("[a-zA-Z0-9]*"))
			return "&cInvalid portal name.";

		PortalWarp warp = engine.getWarp(portalWorld, portalName);
		ILocation playerLocation = player.getLocation();

		String permission = parameters.getValue("permission");

		if (playerLocation == null)
			return "&cInvalid location.";

		if (warp == null)
		{
			engine.createWarp(player, portalName, playerLocation, PortalType.NORMAL, permission);
			return "Now walk through the portal to connect to " + playerLocation;
		}
		warp.setDestination(playerLocation);
		warp.setPermission(permission);

		engine.updateWarp(warp);
		return "&aPortal " + portalName + " now connects to " + playerLocation;
	}

	private final PortalEngine engine;
}
