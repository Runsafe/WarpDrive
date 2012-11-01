package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

public class SetWarp extends RunsafeAsyncPlayerCommand
{
	public SetWarp(IScheduler scheduler, WarpRepository repository)
	{
		super("setwarp", scheduler, "name");
		warpRepository = repository;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.warp.set";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		warpRepository.Persist(player.getName(), getArg("name"), true, player.getLocation());
		return String.format("Current location saved as the warp %s.", getArg("name"));
	}

	WarpRepository warpRepository;
}
