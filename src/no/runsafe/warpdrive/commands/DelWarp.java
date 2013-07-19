package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.minecraft.player.RunsafePlayer;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.Map;

public class DelWarp extends PlayerAsyncCommand
{
	public DelWarp(IScheduler scheduler, WarpRepository repository)
	{
		super("delwarp", "Deletes a warp location", "runsafe.warp.delete", scheduler, "name");
		warpRepository = repository;
	}

	@Override
	public String OnAsyncExecute(RunsafePlayer player, Map<String, String> parameters)
	{
		if (warpRepository.DelPublic(parameters.get("name")))
			return String.format("Deleted public warp %s.", parameters.get("name"));
		return String.format("Unable to delete the public warp %s.", parameters.get("name"));
	}

	private final WarpRepository warpRepository;
}
