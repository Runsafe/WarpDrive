package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.minecraft.player.RunsafePlayer;
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

	private final WarpRepository warpRepository;
}