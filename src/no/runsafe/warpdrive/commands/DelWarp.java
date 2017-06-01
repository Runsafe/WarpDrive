package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.database.WarpRepository;

public class DelWarp extends PlayerAsyncCommand
{
	public DelWarp(IScheduler scheduler, WarpRepository repository)
	{
		super(
			"delwarp", "Deletes a warp location", "runsafe.warp.delete", scheduler,
			new RequiredArgument("name")
		);
		warpRepository = repository;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		String warpName = parameters.getValue("name");
		if (warpRepository.DelPublic(warpName))
			return String.format("Deleted public warp %s.", warpName);
		return String.format("Unable to delete the public warp %s.", warpName);
	}

	private final WarpRepository warpRepository;
}
