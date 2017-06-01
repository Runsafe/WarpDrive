package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.database.WarpRepository;

public class DelHome extends PlayerAsyncCommand
{
	public DelHome(IScheduler scheduler, WarpRepository repository)
	{
		super(
			"delhome", "Deletes a home location", "runsafe.home.delete", scheduler,
			new RequiredArgument("name")
		);
		warpRepository = repository;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		String homeName = parameters.getValue("name");
		if (warpRepository.DelPrivate(player, homeName))
			return String.format("Home location %s removed.", homeName);
		return String.format("Unable to remove the home named %s.", homeName);
	}

	private final WarpRepository warpRepository;
}