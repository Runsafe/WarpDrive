package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.StaticWarp;
import no.runsafe.warpdrive.database.WarpRepository;

public class Home extends RunsafeAsyncPlayerCommand
{
	public Home(IScheduler scheduler, WarpRepository repository)
	{
		super("home", scheduler, "destination");
		warpRepository = repository;
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		RunsafeLocation destination = warpRepository.GetPublic(getArg("destination"));
		if (strings.length > 0 && strings[0].equals("-f"))
			player.teleport(destination);
		else
			StaticWarp.safePlayerTeleport(destination, player);
		return "";
	}

	WarpRepository warpRepository;
}
