package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerAsyncCommand;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.HashMap;

public class SetWarp extends PlayerAsyncCommand
{
	public SetWarp(IScheduler scheduler, WarpRepository repository)
	{
		super("setwarp", "Saves your location as a public warp", "runsafe.warp.set", scheduler, "name");
		warpRepository = repository;
	}

	@Override
	public String OnAsyncExecute(RunsafePlayer player, HashMap<String, String> parameters)
	{
		warpRepository.Persist(player.getName(), parameters.get("name"), true, player.getLocation());
		return String.format("Current location saved as the warp %s.", parameters.get("name"));
	}

	final WarpRepository warpRepository;
}
