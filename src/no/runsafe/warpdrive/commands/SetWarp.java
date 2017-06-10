package no.runsafe.warpdrive.commands;

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
			"setwarp", "Saves your location as a public warp", "runsafe.warp.set", scheduler,
			new RequiredArgument("name")
		);
		warpRepository = repository;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		String name = ((String)parameters.getValue("name")).toLowerCase();
		warpRepository.Persist(player, name, true, player.getLocation());
		return String.format("&aCurrent location saved as the warp %s.", name);
	}

	private final WarpRepository warpRepository;
}
