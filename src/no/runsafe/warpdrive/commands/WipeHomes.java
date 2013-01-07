package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncConsoleCommand;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

public class WipeHomes extends RunsafeAsyncConsoleCommand
{
	public WipeHomes(IScheduler scheduler, WarpRepository warpRepository)
	{
		super("wipehomes", scheduler, "world");
		repository = warpRepository;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.home.wipe";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		String world = getArg("world");
		repository.DelAllPrivate(world);
		return String.format("Deleted all homes from world %s", world);
	}

	private WarpRepository repository;
}
