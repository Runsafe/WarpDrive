package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;

public class DelWarp extends RunsafeAsyncPlayerCommand
{
	public DelWarp(IScheduler scheduler, WarpRepository repository)
	{
		super("delwarp", scheduler, "name");
		warpRepository = repository;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.warp.delete";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		warpRepository.DelPublic(getArg("name"));
		return String.format("Deleted public warp %s.", getArg("name"));
	}

	final WarpRepository warpRepository;
}
