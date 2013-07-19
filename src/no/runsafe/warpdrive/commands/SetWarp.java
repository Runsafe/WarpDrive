package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.minecraft.player.RunsafePlayer;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.Map;

public class SetWarp extends PlayerAsyncCommand
{
	public SetWarp(IScheduler scheduler, WarpRepository repository)
	{
		super("setwarp", "Saves your location as a public warp", "runsafe.warp.set", scheduler, "name");
		warpRepository = repository;
	}

	@Override
	public String OnAsyncExecute(RunsafePlayer player, Map<String, String> parameters)
	{
		String name = parameters.get("name").toLowerCase();
		warpRepository.Persist(player.getName(), name, true, player.getLocation());
		return String.format("Current location saved as the warp %s.", name);
	}

	private final WarpRepository warpRepository;
}
