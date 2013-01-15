package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerAsyncCommand;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.HashMap;

public class DelHome extends PlayerAsyncCommand
{
	public DelHome(IScheduler scheduler, WarpRepository repository)
	{
		super("delhome", "Deletes a home location", "runsafe.home.delete", scheduler, "name");
		warpRepository = repository;
	}

	@Override
	public String OnAsyncExecute(RunsafePlayer player, HashMap<String, String> parameters)
	{
		warpRepository.DelPrivate(player.getName(), parameters.get("name"));
		return String.format("Home location %s removed.", parameters.get("name"));
	}

	final WarpRepository warpRepository;
}