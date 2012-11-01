package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.StaticWarp;
import no.runsafe.warpdrive.database.WarpRepository;

import java.util.logging.Level;

public class Home extends RunsafeAsyncPlayerCommand
{
	public Home(IScheduler scheduler, WarpRepository repository, IOutput output)
	{
		super("home", scheduler, "destination");
		warpRepository = repository;
		console = output;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.home.use";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		RunsafeLocation destination = warpRepository.GetPrivate(player.getName(), getArg("destination"));
		console.outputDebugToConsole(
			String.format(
				"[%s:%.2f,%.2f,%.2f pitch:%.2f yaw:%.2f]",
				getArg("destination"),
				destination.getX(),
				destination.getY(),
				destination.getZ(),
				destination.getPitch(),
				destination.getY()
			),
			Level.FINE
		);
		if (strings.length > 0 && strings[0].equals("-f"))
		{
			player.teleport(destination);
			return String.format("Doing unsafe teleport to home %s", getArg("destination"));
		}
		StaticWarp.safePlayerTeleport(destination, player, false);
		return "";
	}

	WarpRepository warpRepository;
	IOutput console;
}
