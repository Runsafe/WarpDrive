package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.WorldArgument;
import no.runsafe.framework.api.command.console.ConsoleAsyncCommand;
import no.runsafe.warpdrive.database.WarpRepository;

public class WipeHomes extends ConsoleAsyncCommand
{
	public WipeHomes(IScheduler scheduler, WarpRepository warpRepository)
	{
		super(
			"wipehomes", "Deletes all private warp locations from the given world", scheduler,
			new WorldArgument()
		);
		repository = warpRepository;
	}

	@Override
	public String OnAsyncExecute(IArgumentList parameters)
	{
		IWorld world = parameters.getValue("world");
		if (world == null)
			return null;
		repository.DelAllPrivate(world.getName());
		return String.format("Deleted all homes from world %s", world.getName());
	}

	private final WarpRepository repository;
}
