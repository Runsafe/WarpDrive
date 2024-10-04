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

public class DelRegionPortal extends PlayerAsyncCommand
{
	public DelRegionPortal(IScheduler scheduler, PortalEngine engine)
	{
		super("delregionportal", "Deletes a region portal.", "runsafe.portal.del",
			scheduler,
			new WorldArgument().require(),
			new RequiredArgument("region")
		);
		this.engine = engine;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		IWorld world = parameters.getRequired("world");
		String region = parameters.getRequired("region");

		String portal_name = "region_" + world.getName() + '_' + region;
		PortalWarp warp = engine.getWarp(world, portal_name);
		if (warp ==  null)
			return String.format("&cRegion warp %s not found.", portal_name);

		engine.deleteRegionWarp(world, portal_name);
		return String.format("&aRegion warp %s deleted.", portal_name);
	}

	private final PortalEngine engine;
}
