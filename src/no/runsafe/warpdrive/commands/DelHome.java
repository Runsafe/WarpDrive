package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.minecraft.player.RunsafePlayer;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.Map;

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
	public String OnAsyncExecute(RunsafePlayer player, Map<String, String> parameters)
	{
		if (warpRepository.DelPrivate(player.getName(), parameters.get("name")))
			return String.format("Home location %s removed.", parameters.get("name"));
		return String.format("Unable to remove the home named %s.", parameters.get("name"));
	}

	private final WarpRepository warpRepository;
}