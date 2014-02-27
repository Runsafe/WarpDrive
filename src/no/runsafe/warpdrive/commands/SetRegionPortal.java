package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OptionalArgument;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.argument.WorldArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.portals.PortalEngine;
import no.runsafe.warpdrive.portals.PortalWarp;

public class SetRegionPortal extends PlayerAsyncCommand
{
	public SetRegionPortal(IScheduler scheduler, PortalEngine engine)
	{
		super("setregionportal", "Hooks up your current location to a region based portal", "runsafe.portal.set",
			scheduler,
			new WorldArgument(true),
			new RequiredArgument("region"),
			new OptionalArgument("permission")
		);
		this.engine = engine;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		IWorld world = parameters.getValue("world");
		if (world == null)
			return null;
		String region = parameters.get("region");
		String portal_name = "region_" + world.getName() + '_' + region;
		PortalWarp warp = engine.getWarp(world, portal_name);
		if (warp != null)
		{
			warp.setLocation(player.getLocation());
			engine.updateWarp(warp);
			return "Region " + region + " in world " + world.getName() + " warp updated!";
		}
		engine.createRegionWarp(world, region, portal_name, player.getLocation(), parameters.get("permission"));
		return "The region " + region + " in world " + world.getName() + " has been hooked up to a new portal!";
	}

	private final PortalEngine engine;
}
