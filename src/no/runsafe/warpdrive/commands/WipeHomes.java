package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.console.ConsoleAsyncCommand;
import no.runsafe.framework.server.RunsafeConsole;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.HashMap;

public class WipeHomes extends ConsoleAsyncCommand
{
	public WipeHomes(IScheduler scheduler, WarpRepository warpRepository)
	{
		super("wipehomes", "Deletes all private warp locations from the given world", scheduler, "world");
		repository = warpRepository;
	}

	@Override
	public String OnAsyncExecute(RunsafeConsole runsafeConsole, HashMap<String, String> parameters)
	{
		String world = parameters.get("world");
		repository.DelAllPrivate(world);
		return String.format("Deleted all homes from world %s", world);
	}

	private final WarpRepository repository;
}
