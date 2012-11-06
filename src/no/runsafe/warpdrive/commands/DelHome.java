package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

public class DelHome extends RunsafeAsyncPlayerCommand
{
	public DelHome(IScheduler scheduler, WarpRepository repository)
	{
		super("delhome", scheduler, "name");
		warpRepository = repository;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.home.delete";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		warpRepository.DelPrivate(player.getName(), getArg("name"));
		return String.format("Home location %s removed.", getArg("name"));
	}

	final WarpRepository warpRepository;
}