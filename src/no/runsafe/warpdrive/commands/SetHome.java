package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

public class SetHome extends RunsafeAsyncPlayerCommand
{
	public SetHome(IScheduler scheduler, WarpRepository repository)
	{
		super("sethome", scheduler, "name");
		warpRepository = repository;
	}


	@Override
	public String requiredPermission()
	{
		return "runsafe.home.set";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		warpRepository.Persist(player.getName(), getArg("name"), false, player.getLocation());
		return String.format("Current location saved as the home %s.", getArg("name"));
	}

	WarpRepository warpRepository;
}
