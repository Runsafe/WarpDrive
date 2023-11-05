package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.database.WarpRepository;

public class SetWarp extends PlayerAsyncCommand
{
	public SetWarp(IScheduler scheduler, WarpRepository repository)
	{
		super(
			"setwarp",
			"Saves your location as a public warp",
			"runsafe.warp.set",
			scheduler,
			new RequiredArgument("name").toLowercase()
		);
		warpRepository = repository;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		String name = parameters.getRequired("name");
		if (!name.matches("[a-z0-9_-]*"))
			return "&cInvalid warp name.";

		ILocation playerLocation = player.getLocation();
		if (playerLocation == null)
		{
			return "Unable to get player location";
		}
		warpRepository.Persist(player, name, true, playerLocation);
		return String.format("&aCurrent location saved as the warp %s.", name);
	}

	private final WarpRepository warpRepository;
}
