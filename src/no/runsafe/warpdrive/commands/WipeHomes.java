package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.console.ConsoleAsyncCommand;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.Map;

public class WipeHomes extends ConsoleAsyncCommand
{
	public WipeHomes(IScheduler scheduler, WarpRepository warpRepository)
	{
		super("wipehomes", "Deletes all private warp locations from the given world", scheduler, "world");
		repository = warpRepository;
	}

	@Override
	public String OnAsyncExecute(Map<String, String> parameters)
	{
		String world = parameters.get("world");
		repository.DelAllPrivate(world);
		return String.format("Deleted all homes from world %s", world);
	}

	private final WarpRepository repository;
}
